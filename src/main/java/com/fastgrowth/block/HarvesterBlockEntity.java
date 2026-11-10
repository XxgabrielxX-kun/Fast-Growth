package com.fastgrowth.block;

import com.fastgrowth.Config;
import com.fastgrowth.energy.MachineEnergyStorage;
import com.fastgrowth.item.MagnetUpgradeItem;
import com.fastgrowth.item.UpgradeItem;
import com.fastgrowth.menu.HarvesterMenu;
import com.fastgrowth.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Colhedor de Plantas: colhe plantas maduras sem destruir o que esta plantado (a planta volta ao primeiro estagio,
 * como se a semente continuasse no bloco arado). Sem o upgrade de Ima, os itens caem no chao; com ele, vao para o
 * inventario da maquina (que hoppers e canos podem esvaziar).
 */
public class HarvesterBlockEntity extends BlockEntity implements MenuProvider {
    public static final int RANGE_SLOT = 0;
    public static final int MAGNET_SLOT = 1;
    public static final int SPEED_SLOT = 2;
    public static final int UPGRADE_SLOTS = 3;
    public static final int OUTPUT_SLOTS = 18;

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_NO_ENERGY = 1;
    public static final int STATUS_STANDBY = 2;
    public static final int STATUS_FULL = 3;

    /** Valores sincronizados com a GUI (mesmo esquema do Acelerador, ver GrowthAcceleratorBlockEntity). */
    public static final int DATA_COUNT = 9;

    private enum HarvestResult { DONE, FULL }

    // Declarado antes da bateria, que o usa na lambda.
    private boolean energyDirty = false;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(
            () -> getTier().harvesterEnergyCapacity(),
            () -> getTier().harvesterEnergyMaxReceive(),
            () -> energyDirty = true);

    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.empty();

    /** Slot 0 = upgrades de alcance (o nivel e a quantidade de itens), slot 1 = Ima. */
    private final ItemStackHandler upgrades = new ItemStackHandler(UPGRADE_SLOTS) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == MAGNET_SLOT) {
                return stack.getItem() instanceof MagnetUpgradeItem;
            }
            return stack.getItem() instanceof UpgradeItem upgrade
                    && upgrade.getType() == (slot == SPEED_SLOT ? UpgradeItem.Type.SPEED : UpgradeItem.Type.RANGE)
                    && upgrade.getTier() == getTier();
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot == MAGNET_SLOT) return 1;
            return slot == SPEED_SLOT ? getTier().maxSpeedLevel() : getTier().maxRangeLevel();
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            plantCacheValid = false; // o raio pode ter mudado
        }
    };

    /** Itens colhidos (so recebe com o Ima instalado). */
    private final ItemStackHandler output = new ItemStackHandler(OUTPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    /** Visao do inventario de saida para hoppers e canos: so da para retirar, nunca colocar. */
    private final IItemHandler outputView = new IItemHandler() {
        @Override
        public int getSlots() {
            return output.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return output.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return output.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return output.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    };

    private LazyOptional<IItemHandler> itemCap = LazyOptional.empty();

    /** 0 = raio, 1 = intervalo, 2/3 = energia, 4/5 = capacidade, 6/7 = consumo, 8 = status. */
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> getRadius();
                case 1 -> getInterval();
                case 2 -> energy.getEnergyStored() & 0xFFFF;
                case 3 -> energy.getEnergyStored() >>> 16;
                case 4 -> energy.getMaxEnergyStored() & 0xFFFF;
                case 5 -> energy.getMaxEnergyStored() >>> 16;
                case 6 -> getDisplayedEnergyUsage() & 0xFFFF;
                case 7 -> getDisplayedEnergyUsage() >>> 16;
                case 8 -> getStatus();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    private int timer = 0;

    // Em espera (sem nada para colher) a maquina nao gasta energia e so confere de tempos em tempos.
    private boolean standby = true;
    private int standbyTimer = 0;
    /** True quando a ultima colheita foi barrada por falta de espaco no inventario de saida (so com Ima). */
    private boolean blocked = false;

    private final List<BlockPos> plantCache = new ArrayList<>();
    private long lastScanTime = Long.MIN_VALUE;
    private boolean plantCacheValid = false;
    private long firstRipeTime = Long.MIN_VALUE;

    public HarvesterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HARVESTER.get(), pos, state);
    }

    // ---------- nivel e upgrades ----------

    public Tier getTier() {
        return getBlockState().getBlock() instanceof HarvesterBlock block ? block.getTier() : Tier.BASIC;
    }

    public ItemStackHandler getUpgrades() {
        return upgrades;
    }

    public ItemStackHandler getOutput() {
        return output;
    }

    public int getRangeLevel() {
        return Math.min(upgrades.getStackInSlot(RANGE_SLOT).getCount(), getTier().maxRangeLevel());
    }

    public int getSpeedLevel() {
        return Math.min(upgrades.getStackInSlot(SPEED_SLOT).getCount(), getTier().maxSpeedLevel());
    }

    public boolean hasMagnet() {
        return !upgrades.getStackInSlot(MAGNET_SLOT).isEmpty();
    }

    /**
     * Raio horizontal da area de colheita. Usa a mesma formula (e a mesma config) do Acelerador de Crescimento do
     * mesmo nivel: com o mesmo numero de upgrades de alcance nos dois, a area e identica.
     */
    public int getRadius() {
        Tier tier = getTier();
        return tier.baseRadius() + getRangeLevel() * tier.radiusPerLevel();
    }

    public int getInterval() {
        return Math.max(1, getTier().harvestInterval() / (1 << getSpeedLevel()));
    }

    /** Instala 1 unidade de upgrade de alcance ou o Ima (clique direito com o item, sem abrir a GUI). */
    public boolean tryInstallUpgrade(ItemStack held) {
        int slot = RANGE_SLOT;
        if (held.getItem() instanceof MagnetUpgradeItem) {
            slot = MAGNET_SLOT;
        } else if (held.getItem() instanceof UpgradeItem up && up.getType() == UpgradeItem.Type.SPEED) {
            slot = SPEED_SLOT;
        }
        ItemStack single = new ItemStack(held.getItem(), 1);
        return upgrades.insertItem(slot, single, false).isEmpty();
    }

    /** Solta upgrades e itens colhidos quando o bloco e quebrado. */
    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < upgrades.getSlots(); i++) {
            Block.popResource(level, pos, upgrades.getStackInSlot(i).copy());
        }
        for (int i = 0; i < output.getSlots(); i++) {
            Block.popResource(level, pos, output.getStackInSlot(i).copy());
        }
    }

    // ---------- energia ----------

    /** Consumo (FE/tick): base + alcance + velocidade + ima. */
    public int getEnergyUsage() {
        Tier tier = getTier();
        return tier.harvesterEnergyBaseUse()
                + getRangeLevel() * tier.harvesterEnergyPerRange()
                + getSpeedLevel() * tier.harvesterEnergyPerSpeed()
                + (hasMagnet() ? tier.harvesterEnergyMagnet() : 0);
    }

    private int getDisplayedEnergyUsage() {
        return Config.REQUIRES_ENERGY.get() ? getEnergyUsage() : 0;
    }

    public int getStatus() {
        if (blocked && hasMagnet()) {
            return STATUS_FULL;
        }
        if (standby) {
            return STATUS_STANDBY;
        }
        if (Config.REQUIRES_ENERGY.get() && energy.getEnergyStored() < getEnergyUsage()) {
            return STATUS_NO_ENERGY;
        }
        return STATUS_RUNNING;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        energyCap = LazyOptional.of(() -> energy);
        itemCap = LazyOptional.of(() -> outputView);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
        itemCap.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCap.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCap.cast();
        }
        return super.getCapability(cap, side);
    }

    /** Marca o chunk para salvar a energia, no maximo 1 vez a cada 16 ticks. */
    private void flushEnergyIfDirty(ServerLevel level) {
        if (energyDirty && (level.getGameTime() & 15L) == 0L) {
            energyDirty = false;
            level.blockEntityChanged(worldPosition);
        }
    }

    // ---------- menu ----------

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new HarvesterMenu(id, inventory, this, data);
    }

    // ---------- tick ----------

    public static void serverTick(Level level, BlockPos pos, BlockState state, HarvesterBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        be.flushEnergyIfDirty(serverLevel);

        if (be.standby) {
            if (++be.standbyTimer >= Config.SCAN_INTERVAL_TICKS.get()) {
                be.standbyTimer = 0;
                be.checkForWork(serverLevel, pos);
            }
            return;
        }

        if (Config.REQUIRES_ENERGY.get() && !be.energy.consume(be.getEnergyUsage())) {
            return;
        }

        if (++be.timer < be.getInterval()) return;
        be.timer = 0;
        be.cycle(serverLevel, pos);
    }

    private void rescan(ServerLevel level, BlockPos origin, int radius) {
        plantCache.clear();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        // dy crescente por coluna: iterar a lista de tras para frente colhe cana/cacto de cima para baixo.
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    p.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (!level.hasChunkAt(p)) continue;
                    if (isHarvestPlant(level.getBlockState(p))) {
                        plantCache.add(p.immutable());
                    }
                }
            }
        }
        lastScanTime = level.getGameTime();
        plantCacheValid = true;
    }

    /** Sai da espera se houver alguma planta pronta para colher (e espaco para guardar, se tiver Ima). */
    private void checkForWork(ServerLevel level, BlockPos origin) {
        rescan(level, origin, getRadius());
        if (hasMagnet() && !outputHasRoom()) {
            return; // cheio: espera esvaziarem
        }
        int ripe = 0;
        int growing = 0;
        for (BlockPos p : plantCache) {
            BlockState st = level.getBlockState(p);
            if (canHarvest(level, p, st)) {
                ripe++;
            } else if (isStillGrowing(st)) {
                growing++;
            }
        }
        if (ripe == 0) {
            firstRipeTime = Long.MIN_VALUE;
            return;
        }
        long now = level.getGameTime();
        if (Config.HARVEST_ALL_AT_ONCE.get() && growing > 0) {
            // Ha plantas ainda crescendo na area: espera todas ficarem prontas para colher a area inteira de uma vez
            // (com um limite de espera, para uma planta travada nao segurar a maquina para sempre).
            if (firstRipeTime == Long.MIN_VALUE) firstRipeTime = now;
            if (now - firstRipeTime < Config.HARVEST_BATCH_MAX_WAIT_TICKS.get()) return;
        }
        firstRipeTime = Long.MIN_VALUE;
        standby = false;
        timer = getInterval() - 1; // colhe ja no proximo tick
    }

    private void cycle(ServerLevel level, BlockPos origin) {
        long now = level.getGameTime();
        if (!plantCacheValid || now - lastScanTime >= Config.SCAN_INTERVAL_TICKS.get()) {
            rescan(level, origin, getRadius());
        }

        boolean magnet = hasMagnet();
        int max = Config.HARVEST_MAX_PER_CYCLE.get();
        int harvested = 0;
        boolean noRoom = false;

        for (int i = plantCache.size() - 1; i >= 0 && harvested < max; i--) {
            BlockPos p = plantCache.get(i);
            if (!level.hasChunkAt(p)) continue;

            BlockState state = level.getBlockState(p);
            if (!isHarvestPlant(state)) {
                plantCache.remove(i);
                continue;
            }
            if (!canHarvest(level, p, state)) continue;

            HarvestResult result = harvest(level, p, state, magnet);
            if (result == HarvestResult.DONE) {
                harvested++;
            } else {
                noRoom = true;
                break;
            }
        }

        blocked = noRoom;
        // Em modo "area inteira": depois de cada colheita volta a esperar a proxima leva (a nao ser que o limite por
        // ciclo tenha sido atingido e ainda sobre planta pronta).
        if (harvested == 0 || (Config.HARVEST_ALL_AT_ONCE.get() && harvested < max)) {
            standby = true;
            standbyTimer = 0;
        }
    }

    // ---------- o que e colhido ----------

    /** Blocos que o colhedor acompanha. */
    private static boolean isHarvestPlant(BlockState state) {
        Block b = state.getBlock();
        return b instanceof CropBlock || b instanceof NetherWartBlock || b instanceof CocoaBlock
                || b instanceof SweetBerryBushBlock || b instanceof SugarCaneBlock || b instanceof CactusBlock
                || b instanceof AttachedStemBlock;
    }

    /** Planta de idade ainda nao completa (cana/cacto/caule nao entram: nao tem "estagio"). */
    private static boolean isStillGrowing(BlockState state) {
        Block b = state.getBlock();
        boolean hasAge = b instanceof CropBlock || b instanceof NetherWartBlock || b instanceof CocoaBlock
                || b instanceof SweetBerryBushBlock;
        return hasAge && !isFullyGrown(state);
    }

    private static boolean isFullyGrown(BlockState state) {
        Block b = state.getBlock();
        if (b instanceof CropBlock crop) return crop.isMaxAge(state);
        if (b instanceof NetherWartBlock) return state.getValue(NetherWartBlock.AGE) >= 3;
        if (b instanceof CocoaBlock) return state.getValue(CocoaBlock.AGE) >= 2;
        if (b instanceof SweetBerryBushBlock) return state.getValue(SweetBerryBushBlock.AGE) >= 3;
        return false;
    }

    private static boolean canHarvest(ServerLevel level, BlockPos pos, BlockState state) {
        Block b = state.getBlock();
        if (b instanceof AttachedStemBlock) {
            // caule com a fruta do lado (melancia/abobora): colhe a fruta, o caule fica
            BlockState fruit = level.getBlockState(pos.relative(state.getValue(HorizontalDirectionalBlock.FACING)));
            return fruit.is(Blocks.MELON) || fruit.is(Blocks.PUMPKIN);
        }
        if (b instanceof SugarCaneBlock || b instanceof CactusBlock) {
            // so o segmento de cima, e nunca o de baixo (a base fica para a planta voltar a crescer)
            return level.getBlockState(pos.below()).is(b) && !level.getBlockState(pos.above()).is(b);
        }
        return isFullyGrown(state);
    }

    /** Estado da planta depois de colhida: volta ao primeiro estagio (amoras voltam ao estagio 1, como no jogo). */
    private static BlockState replantedState(BlockState state) {
        Block b = state.getBlock();
        if (b instanceof CropBlock crop) return crop.getStateForAge(0);
        if (b instanceof NetherWartBlock) return state.setValue(NetherWartBlock.AGE, 0);
        if (b instanceof CocoaBlock) return state.setValue(CocoaBlock.AGE, 0);
        if (b instanceof SweetBerryBushBlock) return state.setValue(SweetBerryBushBlock.AGE, 1);
        return state;
    }

    private HarvestResult harvest(ServerLevel level, BlockPos pos, BlockState state, boolean magnet) {
        Block block = state.getBlock();

        BlockPos target = pos;          // bloco que sera removido, se for o caso
        BlockState targetState = state;
        boolean removeTarget;
        BlockState replant = null;
        ItemStack seed = ItemStack.EMPTY;

        if (block instanceof AttachedStemBlock) {
            target = pos.relative(state.getValue(HorizontalDirectionalBlock.FACING));
            targetState = level.getBlockState(target);
            removeTarget = true;
        } else if (block instanceof SugarCaneBlock || block instanceof CactusBlock) {
            removeTarget = true;
        } else {
            // A planta continua plantada e volta ao inicio. Como no replantio normal, uma semente dos drops
            // "fica no chao": ela e descontada para nao gerar sementes de graca (importante no Mystical Agriculture).
            removeTarget = false;
            replant = replantedState(state);
            if (!(block instanceof SweetBerryBushBlock)) {
                seed = block.getCloneItemStack(level, pos, state);
            }
        }

        List<ItemStack> drops = new ArrayList<>(Block.getDrops(targetState, level, target, null));
        if (!seed.isEmpty()) {
            for (ItemStack drop : drops) {
                if (ItemStack.isSameItem(drop, seed)) {
                    drop.shrink(1);
                    break;
                }
            }
            drops.removeIf(ItemStack::isEmpty);
        }

        if (magnet) {
            for (ItemStack drop : drops) {
                if (!ItemHandlerHelper.insertItemStacked(output, drop.copy(), true).isEmpty()) {
                    return HarvestResult.FULL; // nao cabe: nao colhe, nada se perde
                }
            }
            for (ItemStack drop : drops) {
                ItemStack rest = ItemHandlerHelper.insertItemStacked(output, drop, false);
                if (!rest.isEmpty()) {
                    Block.popResource(level, pos, rest); // sobra rara: varios drops disputando o ultimo espaco
                }
            }
        } else {
            for (ItemStack drop : drops) {
                Block.popResource(level, pos, drop);
            }
        }

        if (removeTarget) {
            level.removeBlock(target, false);
        } else {
            level.setBlock(pos, replant, Block.UPDATE_ALL);
        }
        return HarvestResult.DONE;
    }

    private boolean outputHasRoom() {
        for (int i = 0; i < output.getSlots(); i++) {
            ItemStack stack = output.getStackInSlot(i);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    // ---------- NBT ----------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Upgrades", upgrades.serializeNBT());
        tag.put("Output", output.serializeNBT());
        tag.putInt("Energy", energy.getEnergyStored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        upgrades.deserializeNBT(tag.getCompound("Upgrades"));
        output.deserializeNBT(tag.getCompound("Output"));
        energy.setStored(tag.getInt("Energy"));
        plantCacheValid = false;
    }
}

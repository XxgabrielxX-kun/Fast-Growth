package com.fastgrowth.block;

import com.fastgrowth.Config;
import com.fastgrowth.compat.CropCompat;
import com.fastgrowth.energy.MachineEnergyStorage;
import com.fastgrowth.item.ParticleInhibitorItem;
import com.fastgrowth.item.UpgradeItem;
import com.fastgrowth.menu.GrowthAcceleratorMenu;
import com.fastgrowth.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GrowthAcceleratorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SPEED_SLOT = 0;
    public static final int RANGE_SLOT = 1;
    public static final int INHIBITOR_SLOT = 2;
    public static final int SLOT_COUNT = 3;

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_NO_ENERGY = 1;
    public static final int STATUS_STANDBY = 2;

    /** Quantidade de valores sincronizados com a GUI (ver {@link #data}). */
    public static final int DATA_COUNT = 9;

    // Declarado antes da bateria, que o usa na lambda.
    private boolean energyDirty = false;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(
            () -> getTier().energyCapacity(),
            () -> getTier().energyMaxReceive(),
            () -> energyDirty = true);

    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.empty();

    /**
     * Slot 0 = upgrades de velocidade, slot 1 = upgrades de alcance (o nivel e a quantidade de itens no slot),
     * slot 2 = Inibidor de Particulas. Velocidade e alcance so aceitam upgrades do mesmo nivel da maquina.
     */
    private final ItemStackHandler upgrades = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == INHIBITOR_SLOT) {
                return stack.getItem() instanceof ParticleInhibitorItem;
            }
            if (!(stack.getItem() instanceof UpgradeItem upgrade) || upgrade.getTier() != getTier()) {
                return false;
            }
            return slot == SPEED_SLOT
                    ? upgrade.getType() == UpgradeItem.Type.SPEED
                    : upgrade.getType() == UpgradeItem.Type.RANGE;
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot == INHIBITOR_SLOT) return 1;
            return slot == SPEED_SLOT ? getTier().maxSpeedLevel() : getTier().maxRangeLevel();
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            plantCacheValid = false; // raio pode ter mudado
        }
    };

    /**
     * Dados sincronizados com a GUI. O Minecraft envia cada valor como short (16 bits), entao numeros de energia
     * grandes sao divididos em duas metades:
     * 0 = raio, 1 = intervalo (ticks), 2/3 = energia guardada (baixo/alto), 4/5 = capacidade, 6/7 = consumo, 8 = status.
     */
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

    // Modo de espera: sem plantas crescendo a maquina nao gasta energia e so confere de tempos em tempos.
    // Comeca em espera: na primeira conferencia (poucos segundos) ela sai dele se ja houver plantas para crescer.
    private boolean standby = true;
    private int standbyTimer = 0;

    // Lista guardada de plantas da area: evita varrer o volume inteiro a cada pulso.
    private final List<BlockPos> plantCache = new ArrayList<>();
    private long lastScanTime = Long.MIN_VALUE;
    private boolean plantCacheValid = false;

    public GrowthAcceleratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GROWTH_ACCELERATOR.get(), pos, state);
    }

    // ---------- nivel e upgrades ----------

    public Tier getTier() {
        return getBlockState().getBlock() instanceof GrowthAcceleratorBlock block ? block.getTier() : Tier.BASIC;
    }

    public ItemStackHandler getUpgrades() {
        return upgrades;
    }

    public int getSpeedLevel() {
        return Math.min(upgrades.getStackInSlot(SPEED_SLOT).getCount(), getTier().maxSpeedLevel());
    }

    public int getRangeLevel() {
        return Math.min(upgrades.getStackInSlot(RANGE_SLOT).getCount(), getTier().maxRangeLevel());
    }

    public boolean isMuffled() {
        return !upgrades.getStackInSlot(INHIBITOR_SLOT).isEmpty();
    }

    /** Raio horizontal atual da area afetada. */
    public int getRadius() {
        Tier tier = getTier();
        return tier.baseRadius() + getRangeLevel() * tier.radiusPerLevel();
    }

    /** Intervalo atual (ticks) entre pulsos: cada upgrade de velocidade divide por 2 (minimo 1 tick). */
    public int getInterval() {
        return Math.max(1, getTier().baseInterval() / (1 << getSpeedLevel()));
    }

    /** Instala 1 unidade do upgrade (clique direito com o item, sem abrir a GUI). */
    public boolean tryAddUpgrade(ItemStack held) {
        if (!(held.getItem() instanceof UpgradeItem upgrade)) {
            return false;
        }
        int slot = upgrade.getType() == UpgradeItem.Type.SPEED ? SPEED_SLOT : RANGE_SLOT;
        ItemStack single = new ItemStack(held.getItem(), 1);
        return upgrades.insertItem(slot, single, false).isEmpty();
    }

    /** Instala o Inibidor de Particulas (clique direito com o item, sem abrir a GUI). */
    public boolean tryInstallInhibitor(ItemStack held) {
        ItemStack single = new ItemStack(held.getItem(), 1);
        return upgrades.insertItem(INHIBITOR_SLOT, single, false).isEmpty();
    }

    public void dropUpgrades(Level level, BlockPos pos) {
        for (int i = 0; i < upgrades.getSlots(); i++) {
            ItemStack stack = upgrades.getStackInSlot(i);
            if (!stack.isEmpty()) {
                level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack.copy()));
            }
        }
    }

    // ---------- energia ----------

    /** Consumo (FE/tick) com os upgrades atuais: base + velocidade + alcance. */
    public int getEnergyUsage() {
        Tier tier = getTier();
        return tier.energyBaseUse()
                + getSpeedLevel() * tier.energyPerSpeedLevel()
                + getRangeLevel() * tier.energyPerRangeLevel();
    }

    /** Consumo mostrado na GUI (0 se a config desligou a necessidade de energia). */
    private int getDisplayedEnergyUsage() {
        return Config.REQUIRES_ENERGY.get() ? getEnergyUsage() : 0;
    }

    public int getStatus() {
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
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCap.cast();
        }
        return super.getCapability(cap, side);
    }

    /** Marca o chunk para salvar a energia, no maximo 1 vez a cada 16 ticks (setChanged todo tick seria pesado). */
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
        return new GrowthAcceleratorMenu(id, inventory, this, data);
    }

    // ---------- tick ----------

    public static void serverTick(Level level, BlockPos pos, BlockState state, GrowthAcceleratorBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        be.flushEnergyIfDirty(serverLevel);

        // Em espera: nao gasta energia, so olha de vez em quando se apareceu planta para crescer.
        if (be.standby) {
            if (++be.standbyTimer >= Config.SCAN_INTERVAL_TICKS.get()) {
                be.standbyTimer = 0;
                be.checkForWork(serverLevel, pos);
            }
            return;
        }

        // Sem energia suficiente a maquina para por completo (o tempo do pulso tambem nao avanca).
        if (Config.REQUIRES_ENERGY.get() && !be.energy.consume(be.getEnergyUsage())) {
            return;
        }

        if (++be.timer < be.getInterval()) return;
        be.timer = 0;
        be.pulse(serverLevel, pos);
    }

    private void rescan(ServerLevel level, BlockPos origin, int radius) {
        plantCache.clear();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    p.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (!level.hasChunkAt(p)) continue;
                    if (isPlant(level.getBlockState(p))) {
                        plantCache.add(p.immutable());
                    }
                }
            }
        }
        lastScanTime = level.getGameTime();
        plantCacheValid = true;
    }

    /** Sai do modo de espera se houver alguma planta que ainda precise crescer. */
    private void checkForWork(ServerLevel level, BlockPos origin) {
        rescan(level, origin, getRadius());
        for (BlockPos p : plantCache) {
            BlockState state = level.getBlockState(p);
            if (isPlant(state) && !isFullyGrown(state)) {
                standby = false;
                timer = 0;
                return;
            }
        }
    }

    private void pulse(ServerLevel level, BlockPos origin) {
        long now = level.getGameTime();
        if (!plantCacheValid || now - lastScanTime >= Config.SCAN_INTERVAL_TICKS.get()) {
            rescan(level, origin, getRadius());
        }

        int attempts = 1 + getSpeedLevel();
        int particleBudget = isMuffled() ? 0 : Config.MAX_PARTICLES_PER_PULSE.get();
        int worked = 0;

        for (int i = plantCache.size() - 1; i >= 0; i--) {
            BlockPos p = plantCache.get(i);
            if (!level.hasChunkAt(p)) continue;

            BlockState first = level.getBlockState(p);
            if (!isPlant(first)) {
                plantCache.remove(i);
                continue;
            }
            if (isFullyGrown(first)) continue; // ja madura: nao gasta tentativas

            worked++;
            boolean grew = false;
            for (int a = 0; a < attempts; a++) {
                BlockState before = level.getBlockState(p);
                if (!isPlant(before) || isFullyGrown(before)) break;
                boolean boosted = boost(level, p, before);
                if (boosted || level.getBlockState(p) != before) {
                    grew = true;
                }
            }

            if (grew && particleBudget > 0) {
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 2, 0.25, 0.25, 0.25, 0.0);
                particleBudget--;
            }
        }

        // Nada para crescer: entra em espera (sem consumo) ate aparecer planta nova.
        if (worked == 0) {
            standby = true;
            standbyTimer = 0;
        }
    }

    private static boolean isPlant(BlockState state) {
        var b = state.getBlock();
        return b instanceof CropBlock || b instanceof StemBlock || b instanceof SaplingBlock
                || b instanceof CocoaBlock || b instanceof NetherWartBlock || b instanceof SweetBerryBushBlock
                || b instanceof SugarCaneBlock || b instanceof CactusBlock || b instanceof BambooStalkBlock;
    }

    /** Plantas que chegaram no ultimo estagio e nao tem mais o que crescer (cana, cacto, bambu e caules nunca "acabam"). */
    private static boolean isFullyGrown(BlockState state) {
        Block b = state.getBlock();
        if (b instanceof CropBlock crop) return crop.isMaxAge(state);
        if (b instanceof NetherWartBlock) return state.getValue(NetherWartBlock.AGE) >= 3;
        if (b instanceof CocoaBlock) return state.getValue(CocoaBlock.AGE) >= 2;
        if (b instanceof SweetBerryBushBlock) return state.getValue(SweetBerryBushBlock.AGE) >= 3;
        return false;
    }

    /**
     * Uma tentativa de crescimento. Plantas "bonemealable" recebem farinha de osso; as demais (cana, cacto, bambu,
     * nether wart, amoras, caules maduros) recebem um random tick. Retorna true se a farinha de osso foi aplicada.
     */
    private static boolean boost(ServerLevel level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof BonemealableBlock growable
                && (block instanceof CropBlock || block instanceof StemBlock
                || block instanceof SaplingBlock || block instanceof CocoaBlock)
                && growable.isValidBonemealTarget(level, pos, state, false)) {
            // Mystical Agriculture & cia: isBonemealSuccess = false de proposito; a maquina age como fertilizante
            // e chama performBonemeal direto (as regras de crux/bioma da planta continuam valendo la dentro).
            if (CropCompat.ignoresBonemealSuccess(block)
                    || growable.isBonemealSuccess(level, level.random, pos, state)) {
                growable.performBonemeal(level, level.random, pos, state);
                return true;
            }
            return false;
        }
        state.randomTick(level, pos, level.random);
        return false;
    }

    // ---------- NBT ----------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Upgrades", upgrades.serializeNBT());
        tag.putInt("Energy", energy.getEnergyStored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        // Maquinas salvas antes do slot do inibidor tinham 2 slots: forca o tamanho atual.
        CompoundTag saved = tag.getCompound("Upgrades").copy();
        saved.putInt("Size", SLOT_COUNT);
        upgrades.deserializeNBT(saved);
        energy.setStored(tag.getInt("Energy"));
        plantCacheValid = false;
    }
}

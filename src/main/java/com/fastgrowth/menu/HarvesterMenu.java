package com.fastgrowth.menu;

import com.fastgrowth.block.HarvesterBlock;
import com.fastgrowth.block.HarvesterBlockEntity;
import com.fastgrowth.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class HarvesterMenu extends AbstractContainerMenu {
    /** Upgrades (alcance, ima, velocidade) + inventario de saida. */
    public static final int MACHINE_SLOTS = HarvesterBlockEntity.UPGRADE_SLOTS + HarvesterBlockEntity.OUTPUT_SLOTS;

    public static final int RANGE_SLOT_X = 26;
    public static final int SPEED_SLOT_X = 80;
    public static final int MAGNET_SLOT_X = 134;
    public static final int UPGRADE_SLOT_Y = 28;

    public static final int OUTPUT_Y = 98;
    public static final int PLAYER_INV_Y = 146;
    public static final int HOTBAR_Y = 204;

    private final HarvesterBlockEntity blockEntity;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    /** Slot de saida: o jogador so retira. */
    private static class OutputSlot extends SlotItemHandler {
        OutputSlot(IItemHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    /** Construtor do lado do cliente (le a posicao enviada pelo servidor). */
    public HarvesterMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, inventory.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(HarvesterBlockEntity.DATA_COUNT));
    }

    /** Construtor do lado do servidor. */
    public HarvesterMenu(int id, Inventory inventory, BlockEntity be, ContainerData data) {
        super(ModMenus.HARVESTER.get(), id);
        checkContainerDataCount(data, HarvesterBlockEntity.DATA_COUNT);
        this.blockEntity = (HarvesterBlockEntity) be;
        this.data = data;
        this.access = ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());

        // a ordem importa: 0 = alcance, 1 = ima, 2 = velocidade, 3..20 = saida
        addSlot(new SlotItemHandler(blockEntity.getUpgrades(), HarvesterBlockEntity.RANGE_SLOT,
                RANGE_SLOT_X, UPGRADE_SLOT_Y));
        addSlot(new SlotItemHandler(blockEntity.getUpgrades(), HarvesterBlockEntity.MAGNET_SLOT,
                MAGNET_SLOT_X, UPGRADE_SLOT_Y));
        addSlot(new SlotItemHandler(blockEntity.getUpgrades(), HarvesterBlockEntity.SPEED_SLOT,
                SPEED_SLOT_X, UPGRADE_SLOT_Y));

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new OutputSlot(blockEntity.getOutput(), col + row * 9, 8 + col * 18, OUTPUT_Y + row * 18));
            }
        }

        // inventario do jogador
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, PLAYER_INV_Y + row * 18));
            }
        }
        // hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, HOTBAR_Y));
        }

        addDataSlots(data);
    }

    public int getRadius() {
        return data.get(0);
    }

    public int getIntervalTicks() {
        return data.get(1);
    }

    /** Junta duas metades de 16 bits (o Minecraft sincroniza cada valor como short). */
    private int combine(int lowIndex, int highIndex) {
        return (data.get(highIndex) << 16) | (data.get(lowIndex) & 0xFFFF);
    }

    public int getEnergy() {
        return combine(2, 3);
    }

    public int getEnergyCapacity() {
        return combine(4, 5);
    }

    public int getEnergyUsage() {
        return combine(6, 7);
    }

    public int getStatus() {
        return data.get(8);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < MACHINE_SLOTS) {
            // upgrade ou item colhido -> inventario do jogador
            if (!moveItemStackTo(stack, MACHINE_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // inventario -> slot de upgrade correspondente (a saida nao aceita itens)
            if (!moveItemStackTo(stack, 0, HarvesterBlockEntity.UPGRADE_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, pos) ->
                level.getBlockState(pos).getBlock() instanceof HarvesterBlock
                        && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0,
                true);
    }
}

package com.fastgrowth.menu;

import com.fastgrowth.block.GrowthAcceleratorBlock;
import com.fastgrowth.block.GrowthAcceleratorBlockEntity;
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
import net.minecraftforge.items.SlotItemHandler;

public class GrowthAcceleratorMenu extends AbstractContainerMenu {
    public static final int UPGRADE_SLOTS = GrowthAcceleratorBlockEntity.SLOT_COUNT;

    public static final int SPEED_SLOT_X = 26;
    public static final int INHIBITOR_SLOT_X = 80;
    public static final int RANGE_SLOT_X = 134;
    public static final int UPGRADE_SLOT_Y = 28;

    /** Posicao do inventario do jogador (a GUI tem uma barra de energia entre os upgrades e o inventario). */
    public static final int PLAYER_INV_Y = 108;
    public static final int HOTBAR_Y = 166;

    private final GrowthAcceleratorBlockEntity blockEntity;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    /** Construtor do lado do cliente (le a posicao enviada pelo servidor). */
    public GrowthAcceleratorMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, inventory.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(GrowthAcceleratorBlockEntity.DATA_COUNT));
    }

    /** Construtor do lado do servidor. */
    public GrowthAcceleratorMenu(int id, Inventory inventory, BlockEntity be, ContainerData data) {
        super(ModMenus.GROWTH_ACCELERATOR.get(), id);
        checkContainerDataCount(data, GrowthAcceleratorBlockEntity.DATA_COUNT);
        this.blockEntity = (GrowthAcceleratorBlockEntity) be;
        this.data = data;
        this.access = ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());

        // a ordem dos slots importa: 0 = velocidade, 1 = alcance, 2 = inibidor
        addSlot(new SlotItemHandler(blockEntity.getUpgrades(), GrowthAcceleratorBlockEntity.SPEED_SLOT,
                SPEED_SLOT_X, UPGRADE_SLOT_Y));
        addSlot(new SlotItemHandler(blockEntity.getUpgrades(), GrowthAcceleratorBlockEntity.RANGE_SLOT,
                RANGE_SLOT_X, UPGRADE_SLOT_Y));
        addSlot(new SlotItemHandler(blockEntity.getUpgrades(), GrowthAcceleratorBlockEntity.INHIBITOR_SLOT,
                INHIBITOR_SLOT_X, UPGRADE_SLOT_Y));

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

        if (index < UPGRADE_SLOTS) {
            // upgrade -> inventario do jogador
            if (!moveItemStackTo(stack, UPGRADE_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // inventario -> slot de upgrade correspondente
            if (!moveItemStackTo(stack, 0, UPGRADE_SLOTS, false)) {
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
                level.getBlockState(pos).getBlock() instanceof GrowthAcceleratorBlock
                        && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0,
                true);
    }
}

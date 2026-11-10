package com.fastgrowth.energy;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/** Visao do armazenamento para cabos e funis: so deixa tirar itens, nunca colocar. */
public class OutputOnlyItemHandler implements IItemHandler {
    private final IItemHandler inner;

    public OutputOnlyItemHandler(IItemHandler inner) {
        this.inner = inner;
    }

    @Override
    public int getSlots() {
        return inner.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inner.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack; // recusa tudo
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return inner.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return inner.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return false;
    }
}

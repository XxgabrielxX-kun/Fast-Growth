package com.fastgrowth.energy;

import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.IntSupplier;

/**
 * Bateria interna da maquina: so recebe energia (cabos e geradores nao conseguem tirar).
 * Capacidade e taxa de recebimento sao lidas ao vivo da config, entao ajustes valem sem recriar a maquina.
 */
public class MachineEnergyStorage implements IEnergyStorage {
    private final IntSupplier capacity;
    private final IntSupplier maxReceive;
    private final Runnable onChanged;
    private int energy;

    public MachineEnergyStorage(IntSupplier capacity, IntSupplier maxReceive, Runnable onChanged) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.onChanged = onChanged;
    }

    @Override
    public int receiveEnergy(int amount, boolean simulate) {
        int space = capacity.getAsInt() - energy;
        int received = Math.min(Math.min(space, maxReceive.getAsInt()), amount);
        if (received <= 0) {
            return 0;
        }
        if (!simulate) {
            energy += received;
            onChanged.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int amount, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return Math.min(energy, capacity.getAsInt());
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity.getAsInt();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    /** Gasta a energia da propria maquina. Retorna false (sem gastar nada) se nao houver o suficiente. */
    public boolean consume(int amount) {
        if (amount <= 0) {
            return true;
        }
        if (energy < amount) {
            return false;
        }
        energy -= amount;
        onChanged.run();
        return true;
    }

    public void setStored(int value) {
        energy = Math.max(0, value);
    }
}

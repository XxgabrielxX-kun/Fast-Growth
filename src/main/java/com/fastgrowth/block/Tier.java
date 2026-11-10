package com.fastgrowth.block;

import com.fastgrowth.Config;

/** Nivel da maquina. Cada nivel tem seus proprios valores de balanceamento na config. */
public enum Tier {
    BASIC,
    ADVANCED;

    // ---------- area e velocidade (Acelerador; o alcance do Colhedor usa os mesmos valores) ----------

    public int baseInterval() {
        return this == BASIC ? Config.BASE_INTERVAL.get() : Config.ADV_BASE_INTERVAL.get();
    }

    public int baseRadius() {
        return this == BASIC ? Config.BASE_RADIUS.get() : Config.ADV_BASE_RADIUS.get();
    }

    public int radiusPerLevel() {
        return this == BASIC ? Config.RADIUS_PER_LEVEL.get() : Config.ADV_RADIUS_PER_LEVEL.get();
    }

    public int maxSpeedLevel() {
        return this == BASIC ? Config.MAX_SPEED_LEVEL.get() : Config.ADV_MAX_SPEED_LEVEL.get();
    }

    public int maxRangeLevel() {
        return this == BASIC ? Config.MAX_RANGE_LEVEL.get() : Config.ADV_MAX_RANGE_LEVEL.get();
    }

    // ---------- energia (FE) do Acelerador ----------

    public int energyCapacity() {
        return this == BASIC ? Config.ENERGY_CAPACITY.get() : Config.ADV_ENERGY_CAPACITY.get();
    }

    public int energyMaxReceive() {
        return this == BASIC ? Config.ENERGY_MAX_RECEIVE.get() : Config.ADV_ENERGY_MAX_RECEIVE.get();
    }

    public int energyBaseUse() {
        return this == BASIC ? Config.ENERGY_BASE_USE.get() : Config.ADV_ENERGY_BASE_USE.get();
    }

    public int energyPerSpeedLevel() {
        return this == BASIC ? Config.ENERGY_PER_SPEED.get() : Config.ADV_ENERGY_PER_SPEED.get();
    }

    public int energyPerRangeLevel() {
        return this == BASIC ? Config.ENERGY_PER_RANGE.get() : Config.ADV_ENERGY_PER_RANGE.get();
    }

    /** Consumo (FE/tick) do Acelerador com todos os upgrades instalados. */
    public int maxEnergyUse() {
        return energyBaseUse()
                + maxSpeedLevel() * energyPerSpeedLevel()
                + maxRangeLevel() * energyPerRangeLevel();
    }

    // ---------- Colhedor de Plantas ----------

    public int harvestInterval() {
        return this == BASIC ? Config.H_INTERVAL.get() : Config.H_ADV_INTERVAL.get();
    }

    public int harvesterEnergyCapacity() {
        return this == BASIC ? Config.H_ENERGY_CAPACITY.get() : Config.H_ADV_ENERGY_CAPACITY.get();
    }

    public int harvesterEnergyMaxReceive() {
        return this == BASIC ? Config.H_ENERGY_MAX_RECEIVE.get() : Config.H_ADV_ENERGY_MAX_RECEIVE.get();
    }

    public int harvesterEnergyBaseUse() {
        return this == BASIC ? Config.H_ENERGY_BASE_USE.get() : Config.H_ADV_ENERGY_BASE_USE.get();
    }

    public int harvesterEnergyPerRange() {
        return this == BASIC ? Config.H_ENERGY_PER_RANGE.get() : Config.H_ADV_ENERGY_PER_RANGE.get();
    }

    public int harvesterEnergyPerSpeed() {
        return this == BASIC ? Config.H_ENERGY_PER_SPEED.get() : Config.H_ADV_ENERGY_PER_SPEED.get();
    }

    public int harvesterEnergyMagnet() {
        return this == BASIC ? Config.H_ENERGY_MAGNET.get() : Config.H_ADV_ENERGY_MAGNET.get();
    }

    /** Consumo (FE/tick) do Colhedor com todos os upgrades de alcance e o ima instalados. */
    public int maxHarvesterEnergyUse() {
        return harvesterEnergyBaseUse()
                + maxRangeLevel() * harvesterEnergyPerRange()
                + maxSpeedLevel() * harvesterEnergyPerSpeed()
                + harvesterEnergyMagnet();
    }
}

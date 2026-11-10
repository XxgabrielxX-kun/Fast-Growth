package com.fastgrowth;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // ---------- Geral / desempenho ----------

    public static final ForgeConfigSpec.BooleanValue REQUIRES_ENERGY = BUILDER
            .comment("Se false, as maquinas funcionam sem consumir energia (util para testes).")
            .define("requiresEnergy", true);

    public static final ForgeConfigSpec.IntValue MAX_PARTICLES_PER_PULSE = BUILDER
            .comment("Maximo de particulas de crescimento por pulso, por maquina (0 desliga todas). O Inibidor de Particulas desliga tudo naquela maquina.")
            .defineInRange("maxParticlesPerPulse", 12, 0, 200);

    public static final ForgeConfigSpec.IntValue SCAN_INTERVAL_TICKS = BUILDER
            .comment("De quanto em quanto tempo (ticks) a maquina procura plantas novas na area. Entre as buscas ela usa uma lista guardada, o que reduz muito o lag.")
            .defineInRange("plantScanIntervalTicks", 40, 5, 600);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BONEMEAL_BYPASS_NAMESPACES = BUILDER
            .comment("Ids de mods cujas plantas crescem pela maquina mesmo quando o bloco recusa farinha de osso (isBonemealSuccess = false).",
                    "Necessario para o Mystical Agriculture: la so o Mystical Fertilizer faz a planta crescer assim, e a maquina age como um fertilizante automatico.",
                    "As regras proprias de cada planta (bloco crux, bioma exigido) continuam valendo.")
            .defineListAllowEmpty("bonemealBypassNamespaces",
                    List.of("mysticalagriculture", "mysticalagradditions"), o -> o instanceof String);

    // ---------- Acelerador basico ----------

    public static final ForgeConfigSpec.IntValue BASE_INTERVAL = BUILDER
            .comment("[Basico] Intervalo base (em ticks, 20 ticks = 1 segundo) entre cada pulso de crescimento sem upgrades. Cada upgrade de velocidade divide isso por 2.")
            .defineInRange("baseIntervalTicks", 1200, 1, 12000);

    public static final ForgeConfigSpec.IntValue BASE_RADIUS = BUILDER
            .comment("[Basico] Raio horizontal base da area afetada (em blocos). 1 = 3x3 ao redor da maquina.")
            .defineInRange("baseRadius", 1, 1, 24);

    public static final ForgeConfigSpec.IntValue RADIUS_PER_LEVEL = BUILDER
            .comment("[Basico] Blocos de raio adicionados por upgrade de alcance.")
            .defineInRange("radiusPerRangeLevel", 2, 1, 8);

    public static final ForgeConfigSpec.IntValue MAX_SPEED_LEVEL = BUILDER
            .comment("[Basico] Maximo de upgrades de velocidade por maquina (ate 16).")
            .defineInRange("maxSpeedLevel", 4, 1, 16);

    public static final ForgeConfigSpec.IntValue MAX_RANGE_LEVEL = BUILDER
            .comment("[Basico] Maximo de upgrades de alcance por maquina (ate 16).")
            .defineInRange("maxRangeLevel", 4, 1, 16);

    public static final ForgeConfigSpec.IntValue ENERGY_CAPACITY = BUILDER
            .comment("[Basico] Capacidade de energia (FE) da maquina.")
            .defineInRange("energyCapacity", 100_000, 1_000, 2_000_000_000);

    public static final ForgeConfigSpec.IntValue ENERGY_MAX_RECEIVE = BUILDER
            .comment("[Basico] Maximo de FE que a maquina aceita receber por tick.")
            .defineInRange("energyMaxReceivePerTick", 1_000, 1, 100_000_000);

    public static final ForgeConfigSpec.IntValue ENERGY_BASE_USE = BUILDER
            .comment("[Basico] Consumo (FE/tick) sem nenhum upgrade, enquanto houver plantas crescendo.")
            .defineInRange("energyBaseUsePerTick", 10, 0, 1_000_000);

    public static final ForgeConfigSpec.IntValue ENERGY_PER_SPEED = BUILDER
            .comment("[Basico] FE/tick a mais por cada upgrade de velocidade instalado.")
            .defineInRange("energyPerSpeedLevelPerTick", 30, 0, 1_000_000);

    public static final ForgeConfigSpec.IntValue ENERGY_PER_RANGE = BUILDER
            .comment("[Basico] FE/tick a mais por cada upgrade de alcance instalado.")
            .defineInRange("energyPerRangeLevelPerTick", 15, 0, 1_000_000);

    // ---------- Acelerador avancado ----------

    public static final ForgeConfigSpec.IntValue ADV_BASE_INTERVAL;
    public static final ForgeConfigSpec.IntValue ADV_BASE_RADIUS;
    public static final ForgeConfigSpec.IntValue ADV_RADIUS_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue ADV_MAX_SPEED_LEVEL;
    public static final ForgeConfigSpec.IntValue ADV_MAX_RANGE_LEVEL;
    public static final ForgeConfigSpec.IntValue ADV_ENERGY_CAPACITY;
    public static final ForgeConfigSpec.IntValue ADV_ENERGY_MAX_RECEIVE;
    public static final ForgeConfigSpec.IntValue ADV_ENERGY_BASE_USE;
    public static final ForgeConfigSpec.IntValue ADV_ENERGY_PER_SPEED;
    public static final ForgeConfigSpec.IntValue ADV_ENERGY_PER_RANGE;

    static {
        BUILDER.push("advanced");

        ADV_BASE_INTERVAL = BUILDER
                .comment("[Avancado] Intervalo base (em ticks) entre cada pulso sem upgrades. Cada upgrade de velocidade divide isso por 2 (minimo de 1 tick).")
                .defineInRange("baseIntervalTicks", 400, 1, 12000);

        ADV_BASE_RADIUS = BUILDER
                .comment("[Avancado] Raio horizontal base da area afetada (3 = 7x7 ao redor da maquina).")
                .defineInRange("baseRadius", 3, 1, 24);

        ADV_RADIUS_PER_LEVEL = BUILDER
                .comment("[Avancado] Blocos de raio adicionados por upgrade de alcance avancado.")
                .defineInRange("radiusPerRangeLevel", 3, 1, 8);

        ADV_MAX_SPEED_LEVEL = BUILDER
                .comment("[Avancado] Maximo de upgrades de velocidade avancados por maquina (ate 16).")
                .defineInRange("maxSpeedLevel", 8, 1, 16);

        ADV_MAX_RANGE_LEVEL = BUILDER
                .comment("[Avancado] Maximo de upgrades de alcance avancados por maquina (ate 16).")
                .defineInRange("maxRangeLevel", 6, 1, 16);

        ADV_ENERGY_CAPACITY = BUILDER
                .comment("[Avancado] Capacidade de energia (FE) da maquina.")
                .defineInRange("energyCapacity", 5_000_000, 1_000, 2_000_000_000);

        ADV_ENERGY_MAX_RECEIVE = BUILDER
                .comment("[Avancado] Maximo de FE que a maquina aceita receber por tick.")
                .defineInRange("energyMaxReceivePerTick", 20_000, 1, 100_000_000);

        ADV_ENERGY_BASE_USE = BUILDER
                .comment("[Avancado] Consumo (FE/tick) sem nenhum upgrade, enquanto houver plantas crescendo.")
                .defineInRange("energyBaseUsePerTick", 100, 0, 1_000_000);

        ADV_ENERGY_PER_SPEED = BUILDER
                .comment("[Avancado] FE/tick a mais por cada upgrade de velocidade instalado.")
                .defineInRange("energyPerSpeedLevelPerTick", 250, 0, 1_000_000);

        ADV_ENERGY_PER_RANGE = BUILDER
                .comment("[Avancado] FE/tick a mais por cada upgrade de alcance instalado.")
                .defineInRange("energyPerRangeLevelPerTick", 150, 0, 1_000_000);

        BUILDER.pop();
    }

    // ---------- Coletor de plantas ----------
    // O raio e os niveis de alcance do Coletor seguem os do Acelerador do mesmo nivel (valores acima).

    public static final ForgeConfigSpec.IntValue HARV_INTERVAL;
    public static final ForgeConfigSpec.IntValue HARV_MAX_PER_PULSE;
    public static final ForgeConfigSpec.IntValue HARV_ENERGY_CAPACITY;
    public static final ForgeConfigSpec.IntValue HARV_ENERGY_MAX_RECEIVE;
    public static final ForgeConfigSpec.IntValue HARV_ENERGY_BASE_USE;
    public static final ForgeConfigSpec.IntValue HARV_ENERGY_PER_RANGE;
    public static final ForgeConfigSpec.IntValue HARV_ENERGY_MAGNET;

    public static final ForgeConfigSpec.IntValue ADV_HARV_INTERVAL;
    public static final ForgeConfigSpec.IntValue ADV_HARV_MAX_PER_PULSE;
    public static final ForgeConfigSpec.IntValue ADV_HARV_ENERGY_CAPACITY;
    public static final ForgeConfigSpec.IntValue ADV_HARV_ENERGY_MAX_RECEIVE;
    public static final ForgeConfigSpec.IntValue ADV_HARV_ENERGY_BASE_USE;
    public static final ForgeConfigSpec.IntValue ADV_HARV_ENERGY_PER_RANGE;
    public static final ForgeConfigSpec.IntValue ADV_HARV_ENERGY_MAGNET;

    static {
        BUILDER.push("harvester");

        HARV_INTERVAL = BUILDER
                .comment("[Coletor] De quantos em quantos ticks ele colhe as plantas maduras da area (20 ticks = 1 segundo).")
                .defineInRange("harvestIntervalTicks", 40, 1, 12000);
        HARV_MAX_PER_PULSE = BUILDER
                .comment("[Coletor] Maximo de plantas colhidas por vez (evita pico de lag em fazendas enormes).")
                .defineInRange("maxHarvestsPerPulse", 32, 1, 10_000);
        HARV_ENERGY_CAPACITY = BUILDER
                .comment("[Coletor] Capacidade de energia (FE).")
                .defineInRange("energyCapacity", 50_000, 1_000, 2_000_000_000);
        HARV_ENERGY_MAX_RECEIVE = BUILDER
                .comment("[Coletor] Maximo de FE que aceita receber por tick.")
                .defineInRange("energyMaxReceivePerTick", 500, 1, 100_000_000);
        HARV_ENERGY_BASE_USE = BUILDER
                .comment("[Coletor] Consumo (FE/tick) sem upgrades, enquanto houver o que colher.")
                .defineInRange("energyBaseUsePerTick", 5, 0, 1_000_000);
        HARV_ENERGY_PER_RANGE = BUILDER
                .comment("[Coletor] FE/tick a mais por cada upgrade de alcance instalado.")
                .defineInRange("energyPerRangeLevelPerTick", 10, 0, 1_000_000);
        HARV_ENERGY_MAGNET = BUILDER
                .comment("[Coletor] FE/tick a mais quando o Upgrade de Ima esta instalado.")
                .defineInRange("energyMagnetPerTick", 20, 0, 1_000_000);

        BUILDER.push("advanced");

        ADV_HARV_INTERVAL = BUILDER
                .comment("[Coletor Avancado] De quantos em quantos ticks ele colhe as plantas maduras da area.")
                .defineInRange("harvestIntervalTicks", 10, 1, 12000);
        ADV_HARV_MAX_PER_PULSE = BUILDER
                .comment("[Coletor Avancado] Maximo de plantas colhidas por vez.")
                .defineInRange("maxHarvestsPerPulse", 256, 1, 10_000);
        ADV_HARV_ENERGY_CAPACITY = BUILDER
                .comment("[Coletor Avancado] Capacidade de energia (FE).")
                .defineInRange("energyCapacity", 1_000_000, 1_000, 2_000_000_000);
        ADV_HARV_ENERGY_MAX_RECEIVE = BUILDER
                .comment("[Coletor Avancado] Maximo de FE que aceita receber por tick.")
                .defineInRange("energyMaxReceivePerTick", 10_000, 1, 100_000_000);
        ADV_HARV_ENERGY_BASE_USE = BUILDER
                .comment("[Coletor Avancado] Consumo (FE/tick) sem upgrades, enquanto houver o que colher.")
                .defineInRange("energyBaseUsePerTick", 50, 0, 1_000_000);
        ADV_HARV_ENERGY_PER_RANGE = BUILDER
                .comment("[Coletor Avancado] FE/tick a mais por cada upgrade de alcance instalado.")
                .defineInRange("energyPerRangeLevelPerTick", 60, 0, 1_000_000);
        ADV_HARV_ENERGY_MAGNET = BUILDER
                .comment("[Coletor Avancado] FE/tick a mais quando o Upgrade de Ima esta instalado.")
                .defineInRange("energyMagnetPerTick", 100, 0, 1_000_000);

        BUILDER.pop();
        BUILDER.pop();
    }

    // ---------- Colhedor de Plantas ----------
    // O raio e o maximo de upgrades de alcance do Colhedor sao os mesmos do Acelerador do mesmo nivel
    // (o alcance acompanha: com N upgrades de alcance nos dois, a area e a mesma).

    public static final ForgeConfigSpec.IntValue HARVEST_MAX_PER_CYCLE;

    public static final ForgeConfigSpec.BooleanValue HARVEST_ALL_AT_ONCE;
    public static final ForgeConfigSpec.IntValue HARVEST_BATCH_MAX_WAIT_TICKS;

    public static final ForgeConfigSpec.IntValue H_INTERVAL;
    public static final ForgeConfigSpec.IntValue H_ENERGY_CAPACITY;
    public static final ForgeConfigSpec.IntValue H_ENERGY_MAX_RECEIVE;
    public static final ForgeConfigSpec.IntValue H_ENERGY_BASE_USE;
    public static final ForgeConfigSpec.IntValue H_ENERGY_PER_RANGE;
    public static final ForgeConfigSpec.IntValue H_ENERGY_PER_SPEED;
    public static final ForgeConfigSpec.IntValue H_ENERGY_MAGNET;

    public static final ForgeConfigSpec.IntValue H_ADV_INTERVAL;
    public static final ForgeConfigSpec.IntValue H_ADV_ENERGY_CAPACITY;
    public static final ForgeConfigSpec.IntValue H_ADV_ENERGY_MAX_RECEIVE;
    public static final ForgeConfigSpec.IntValue H_ADV_ENERGY_BASE_USE;
    public static final ForgeConfigSpec.IntValue H_ADV_ENERGY_PER_RANGE;
    public static final ForgeConfigSpec.IntValue H_ADV_ENERGY_PER_SPEED;
    public static final ForgeConfigSpec.IntValue H_ADV_ENERGY_MAGNET;

    static {
        BUILDER.push("harvester");

        HARVEST_MAX_PER_CYCLE = BUILDER
                .comment("[Colhedor] Maximo de plantas colhidas por ciclo, por maquina (evita lag em fazendas gigantes; o resto fica para o proximo ciclo).")
                .defineInRange("maxHarvestsPerCycle", 256, 1, 10000);

        HARVEST_ALL_AT_ONCE = BUILDER
                .comment("[Colhedor] Se true, o Colhedor espera todas as plantas da area ficarem prontas e colhe tudo de uma vez.",
                        "Se false, colhe cada planta assim que ela amadurece.")
                .define("harvestAllAtOnce", true);

        HARVEST_BATCH_MAX_WAIT_TICKS = BUILDER
                .comment("[Colhedor] Tempo maximo (ticks) esperando as plantas restantes ficarem prontas antes de colher so as prontas.")
                .defineInRange("batchMaxWaitTicks", 1200, 0, 72000);

        H_INTERVAL = BUILDER
                .comment("[Colhedor basico] Intervalo (em ticks, 20 = 1 segundo) entre cada ciclo de colheita.")
                .defineInRange("harvestIntervalTicks", 40, 1, 1200);

        H_ENERGY_CAPACITY = BUILDER
                .comment("[Colhedor basico] Capacidade de energia (FE).")
                .defineInRange("energyCapacity", 50_000, 1_000, 2_000_000_000);

        H_ENERGY_MAX_RECEIVE = BUILDER
                .comment("[Colhedor basico] Maximo de FE que a maquina aceita receber por tick.")
                .defineInRange("energyMaxReceivePerTick", 500, 1, 100_000_000);

        H_ENERGY_BASE_USE = BUILDER
                .comment("[Colhedor basico] Consumo (FE/tick) sem upgrades, enquanto houver planta madura para colher.")
                .defineInRange("energyBaseUsePerTick", 5, 0, 1_000_000);

        H_ENERGY_PER_RANGE = BUILDER
                .comment("[Colhedor basico] FE/tick a mais por cada upgrade de alcance instalado.")
                .defineInRange("energyPerRangeLevelPerTick", 10, 0, 1_000_000);

        H_ENERGY_PER_SPEED = BUILDER
                .comment("[Colhedor basico] FE/tick a mais por cada upgrade de velocidade instalado (cada um reduz o intervalo pela metade).")
                .defineInRange("energyPerSpeedLevelPerTick", 20, 0, 1_000_000);

        H_ENERGY_MAGNET = BUILDER
                .comment("[Colhedor basico] FE/tick a mais com o upgrade de Ima instalado.")
                .defineInRange("energyMagnetPerTick", 15, 0, 1_000_000);

        BUILDER.push("advanced");

        H_ADV_INTERVAL = BUILDER
                .comment("[Colhedor avancado] Intervalo (em ticks) entre cada ciclo de colheita.")
                .defineInRange("harvestIntervalTicks", 10, 1, 1200);

        H_ADV_ENERGY_CAPACITY = BUILDER
                .comment("[Colhedor avancado] Capacidade de energia (FE).")
                .defineInRange("energyCapacity", 1_000_000, 1_000, 2_000_000_000);

        H_ADV_ENERGY_MAX_RECEIVE = BUILDER
                .comment("[Colhedor avancado] Maximo de FE que a maquina aceita receber por tick.")
                .defineInRange("energyMaxReceivePerTick", 10_000, 1, 100_000_000);

        H_ADV_ENERGY_BASE_USE = BUILDER
                .comment("[Colhedor avancado] Consumo (FE/tick) sem upgrades, enquanto houver planta madura para colher.")
                .defineInRange("energyBaseUsePerTick", 50, 0, 1_000_000);

        H_ADV_ENERGY_PER_RANGE = BUILDER
                .comment("[Colhedor avancado] FE/tick a mais por cada upgrade de alcance instalado.")
                .defineInRange("energyPerRangeLevelPerTick", 100, 0, 1_000_000);

        H_ADV_ENERGY_PER_SPEED = BUILDER
                .comment("[Colhedor avancado] FE/tick a mais por cada upgrade de velocidade instalado.")
                .defineInRange("energyPerSpeedLevelPerTick", 200, 0, 1_000_000);

        H_ADV_ENERGY_MAGNET = BUILDER
                .comment("[Colhedor avancado] FE/tick a mais com o upgrade de Ima instalado.")
                .defineInRange("energyMagnetPerTick", 150, 0, 1_000_000);

        BUILDER.pop();
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}

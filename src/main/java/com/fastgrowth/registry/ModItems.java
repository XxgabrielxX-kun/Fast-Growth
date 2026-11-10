package com.fastgrowth.registry;

import com.fastgrowth.FastGrowth;
import com.fastgrowth.block.Tier;
import com.fastgrowth.item.MagnetUpgradeItem;
import com.fastgrowth.item.ParticleInhibitorItem;
import com.fastgrowth.item.UpgradeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FastGrowth.MODID);

    // ---------- basico ----------

    public static final RegistryObject<Item> GROWTH_ACCELERATOR = ITEMS.register("growth_accelerator",
            () -> new BlockItem(ModBlocks.GROWTH_ACCELERATOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> SPEED_UPGRADE = ITEMS.register("speed_upgrade",
            () -> new UpgradeItem(UpgradeItem.Type.SPEED, Tier.BASIC, new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> RANGE_UPGRADE = ITEMS.register("range_upgrade",
            () -> new UpgradeItem(UpgradeItem.Type.RANGE, Tier.BASIC, new Item.Properties().stacksTo(16)));

    // ---------- inibidor de particulas (qualquer nivel) ----------

    public static final RegistryObject<Item> PARTICLE_INHIBITOR = ITEMS.register("particle_inhibitor",
            () -> new ParticleInhibitorItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));

    // ---------- avancado ----------

    public static final RegistryObject<Item> ADVANCED_GROWTH_ACCELERATOR = ITEMS.register("advanced_growth_accelerator",
            () -> new BlockItem(ModBlocks.ADVANCED_GROWTH_ACCELERATOR.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> ADVANCED_SPEED_UPGRADE = ITEMS.register("advanced_speed_upgrade",
            () -> new UpgradeItem(UpgradeItem.Type.SPEED, Tier.ADVANCED,
                    new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> ADVANCED_RANGE_UPGRADE = ITEMS.register("advanced_range_upgrade",
            () -> new UpgradeItem(UpgradeItem.Type.RANGE, Tier.ADVANCED,
                    new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));

    // ---------- Colhedor de Plantas ----------
    // Os upgrades de alcance acima (basico e avancado) tambem servem no Colhedor do mesmo nivel.

    public static final RegistryObject<Item> PLANT_HARVESTER = ITEMS.register("plant_harvester",
            () -> new BlockItem(ModBlocks.PLANT_HARVESTER.get(), new Item.Properties()));

    public static final RegistryObject<Item> ADVANCED_PLANT_HARVESTER = ITEMS.register("advanced_plant_harvester",
            () -> new BlockItem(ModBlocks.ADVANCED_PLANT_HARVESTER.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> MAGNET_UPGRADE = ITEMS.register("magnet_upgrade",
            () -> new MagnetUpgradeItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
}

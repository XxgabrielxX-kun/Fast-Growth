package com.fastgrowth.registry;

import com.fastgrowth.FastGrowth;
import com.fastgrowth.block.GrowthAcceleratorBlockEntity;
import com.fastgrowth.block.HarvesterBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FastGrowth.MODID);

    /** Um unico tipo de block entity serve para os dois niveis do acelerador. */
    public static final RegistryObject<BlockEntityType<GrowthAcceleratorBlockEntity>> GROWTH_ACCELERATOR =
            BLOCK_ENTITIES.register("growth_accelerator",
                    () -> BlockEntityType.Builder.of(GrowthAcceleratorBlockEntity::new,
                            ModBlocks.GROWTH_ACCELERATOR.get(),
                            ModBlocks.ADVANCED_GROWTH_ACCELERATOR.get()).build(null));

    /** Um unico tipo de block entity serve para os dois niveis do colhedor. */
    public static final RegistryObject<BlockEntityType<HarvesterBlockEntity>> HARVESTER =
            BLOCK_ENTITIES.register("plant_harvester",
                    () -> BlockEntityType.Builder.of(HarvesterBlockEntity::new,
                            ModBlocks.PLANT_HARVESTER.get(),
                            ModBlocks.ADVANCED_PLANT_HARVESTER.get()).build(null));
}

package com.fastgrowth.registry;

import com.fastgrowth.FastGrowth;
import com.fastgrowth.block.GrowthAcceleratorBlock;
import com.fastgrowth.block.HarvesterBlock;
import com.fastgrowth.block.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, FastGrowth.MODID);

    public static final RegistryObject<Block> GROWTH_ACCELERATOR = BLOCKS.register("growth_accelerator",
            () -> new GrowthAcceleratorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(3.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .lightLevel(state -> 7), Tier.BASIC));

    public static final RegistryObject<Block> ADVANCED_GROWTH_ACCELERATOR = BLOCKS.register("advanced_growth_accelerator",
            () -> new GrowthAcceleratorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(5.0f, 12.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.NETHERITE_BLOCK)
                    .lightLevel(state -> 12), Tier.ADVANCED));

    public static final RegistryObject<Block> PLANT_HARVESTER = BLOCKS.register("plant_harvester",
            () -> new HarvesterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(3.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL), Tier.BASIC));

    public static final RegistryObject<Block> ADVANCED_PLANT_HARVESTER = BLOCKS.register("advanced_plant_harvester",
            () -> new HarvesterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(5.0f, 12.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.NETHERITE_BLOCK), Tier.ADVANCED));
}

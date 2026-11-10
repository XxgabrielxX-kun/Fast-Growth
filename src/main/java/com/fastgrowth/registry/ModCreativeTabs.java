package com.fastgrowth.registry;

import com.fastgrowth.FastGrowth;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FastGrowth.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + FastGrowth.MODID))
                    .icon(() -> new ItemStack(ModItems.GROWTH_ACCELERATOR.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.GROWTH_ACCELERATOR.get());
                        output.accept(ModItems.SPEED_UPGRADE.get());
                        output.accept(ModItems.RANGE_UPGRADE.get());
                        output.accept(ModItems.PARTICLE_INHIBITOR.get());
                        output.accept(ModItems.ADVANCED_GROWTH_ACCELERATOR.get());
                        output.accept(ModItems.ADVANCED_SPEED_UPGRADE.get());
                        output.accept(ModItems.ADVANCED_RANGE_UPGRADE.get());
                        output.accept(ModItems.PLANT_HARVESTER.get());
                        output.accept(ModItems.ADVANCED_PLANT_HARVESTER.get());
                        output.accept(ModItems.MAGNET_UPGRADE.get());
                    })
                    .build());
}

package com.fastgrowth.registry;

import com.fastgrowth.FastGrowth;
import com.fastgrowth.menu.GrowthAcceleratorMenu;
import com.fastgrowth.menu.HarvesterMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, FastGrowth.MODID);

    public static final RegistryObject<MenuType<GrowthAcceleratorMenu>> GROWTH_ACCELERATOR =
            MENUS.register("growth_accelerator",
                    () -> IForgeMenuType.create(GrowthAcceleratorMenu::new));

    public static final RegistryObject<MenuType<HarvesterMenu>> HARVESTER =
            MENUS.register("plant_harvester",
                    () -> IForgeMenuType.create(HarvesterMenu::new));
}

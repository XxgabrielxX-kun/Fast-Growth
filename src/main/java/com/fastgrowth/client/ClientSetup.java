package com.fastgrowth.client;

import com.fastgrowth.FastGrowth;
import com.fastgrowth.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = FastGrowth.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.GROWTH_ACCELERATOR.get(), GrowthAcceleratorScreen::new);
            MenuScreens.register(ModMenus.HARVESTER.get(), HarvesterScreen::new);
        });
    }
}

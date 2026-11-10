package com.fastgrowth;

import com.fastgrowth.compat.CropCompat;
import com.fastgrowth.registry.ModBlockEntities;
import com.fastgrowth.registry.ModBlocks;
import com.fastgrowth.registry.ModCreativeTabs;
import com.fastgrowth.registry.ModItems;
import com.fastgrowth.registry.ModMenus;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FastGrowth.MODID)
public class FastGrowth {
    public static final String MODID = "fastgrowth";

    public FastGrowth() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModBlockEntities.BLOCK_ENTITIES.register(bus);
        ModMenus.MENUS.register(bus);
        ModCreativeTabs.TABS.register(bus);

        // Se a config for editada com o jogo aberto, a lista de mods compativeis e lida de novo.
        bus.addListener((ModConfigEvent.Reloading event) -> CropCompat.clearCache());

        // Nome de arquivo novo (v3) para nao herdar os valores antigos das configs anteriores.
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, "fastgrowth-v3.toml");
    }
}

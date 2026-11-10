package com.fastgrowth.compat;

import com.fastgrowth.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Compatibilidade com plantas de outros mods (principalmente o Mystical Agriculture).
 * Nao usa nenhuma classe de outro mod: so olha o id (namespace) do bloco, entao nao ha dependencia obrigatoria.
 */
public final class CropCompat {
    private static final Map<Block, Boolean> BYPASS_CACHE = new ConcurrentHashMap<>();

    private CropCompat() {
    }

    /**
     * True se a planta deve crescer pela maquina mesmo que o bloco recuse farinha de osso.
     * As plantas do Mystical Agriculture devolvem isBonemealSuccess = false de proposito; so o Mystical Fertilizer
     * (que chama performBonemeal direto) as faz crescer. A maquina faz o mesmo.
     */
    public static boolean ignoresBonemealSuccess(Block block) {
        return BYPASS_CACHE.computeIfAbsent(block, b -> {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(b);
            return id != null && Config.BONEMEAL_BYPASS_NAMESPACES.get().contains(id.getNamespace());
        });
    }

    public static void clearCache() {
        BYPASS_CACHE.clear();
    }
}

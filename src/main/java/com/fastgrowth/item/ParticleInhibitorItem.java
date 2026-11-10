package com.fastgrowth.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Desliga as particulas de crescimento da maquina (reduz lag). Funciona em qualquer nivel de acelerador. */
public class ParticleInhibitorItem extends Item {

    public ParticleInhibitorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.fastgrowth.inhibitor").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.fastgrowth.inhibitor_scope").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.fastgrowth.how_to_install").withStyle(ChatFormatting.DARK_GREEN));
    }
}

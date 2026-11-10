package com.fastgrowth.item;

import com.fastgrowth.block.Tier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Item de upgrade. Cada upgrade so funciona na maquina do mesmo nivel (basico ou avancado). */
public class UpgradeItem extends Item {
    public enum Type { SPEED, RANGE }

    private final Type type;
    private final Tier tier;

    public UpgradeItem(Type type, Tier tier, Properties properties) {
        super(properties);
        this.type = type;
        this.tier = tier;
    }

    public Type getType() {
        return type;
    }

    public Tier getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int max = type == Type.SPEED ? tier.maxSpeedLevel() : tier.maxRangeLevel();
        tooltip.add(Component.translatable("tooltip.fastgrowth." + type.name().toLowerCase(), max)
                .withStyle(ChatFormatting.GRAY));
        if (type == Type.SPEED) {
            tooltip.add(Component.translatable("tooltip.fastgrowth.energy_cost_range",
                            tier.energyPerSpeedLevel(), tier.harvesterEnergyPerSpeed())
                    .withStyle(ChatFormatting.RED));
        } else {
            // o upgrade de alcance serve no Acelerador e no Colhedor do mesmo nivel
            tooltip.add(Component.translatable("tooltip.fastgrowth.energy_cost_range",
                            tier.energyPerRangeLevel(), tier.harvesterEnergyPerRange())
                    .withStyle(ChatFormatting.RED));
        }
        tooltip.add(Component.translatable("tooltip.fastgrowth.tier_" + tier.name().toLowerCase())
                .withStyle(tier == Tier.ADVANCED ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.fastgrowth.how_to_install")
                .withStyle(ChatFormatting.DARK_GREEN));
    }
}

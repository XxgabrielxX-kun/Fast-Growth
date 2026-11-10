package com.fastgrowth.client;

import com.fastgrowth.FastGrowth;
import com.fastgrowth.block.HarvesterBlockEntity;
import com.fastgrowth.menu.HarvesterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HarvesterScreen extends AbstractContainerScreen<HarvesterMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FastGrowth.MODID, "textures/gui/plant_harvester.png");

    // Barra de energia (moldura desenhada na textura; o preenchimento e desenhado aqui).
    private static final int BAR_X = 8;
    private static final int BAR_Y = 71;
    private static final int BAR_W = 160;
    private static final int BAR_H = 10;

    public HarvesterScreen(HarvesterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 228;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (imageWidth - font.width(title)) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelY = HarvesterMenu.PLAYER_INV_Y - 12;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(BAR_X - 1, BAR_Y - 1, BAR_W + 2, BAR_H + 2, mouseX, mouseY)) {
            NumberFormat nf = NumberFormat.getIntegerInstance();
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatable("gui.fastgrowth.energy_tooltip",
                            nf.format(menu.getEnergy()), nf.format(menu.getEnergyCapacity())),
                    Component.translatable("gui.fastgrowth.energy_use_tooltip", nf.format(menu.getEnergyUsage()))
            ), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int capacity = menu.getEnergyCapacity();
        if (capacity > 0) {
            int filled = (int) ((long) BAR_W * Math.min(menu.getEnergy(), capacity) / capacity);
            if (filled > 0) {
                int x0 = leftPos + BAR_X;
                int y0 = topPos + BAR_Y;
                graphics.fill(x0, y0, x0 + filled, y0 + BAR_H, 0xFFC8322A);
                graphics.fill(x0, y0, x0 + filled, y0 + 2, 0xFFEF6A5A); // brilho no topo
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);

        // alcance
        Slot rangeSlot = menu.getSlot(HarvesterBlockEntity.RANGE_SLOT);
        int rangeCenter = HarvesterMenu.RANGE_SLOT_X + 8;
        Component rangeName = Component.translatable("gui.fastgrowth.range");
        graphics.drawString(font, rangeName, rangeCenter - font.width(rangeName) / 2, 17, 0x404040, false);
        Component rangeLevel = Component.translatable("gui.fastgrowth.level",
                rangeSlot.getItem().getCount(), rangeSlot.getMaxStackSize());
        graphics.drawString(font, rangeLevel, rangeCenter - font.width(rangeLevel) / 2, 48, 0x404040, false);

        // velocidade
        Slot speedSlot = menu.getSlot(HarvesterBlockEntity.SPEED_SLOT);
        int speedCenter = HarvesterMenu.SPEED_SLOT_X + 8;
        Component speedName = Component.translatable("gui.fastgrowth.speed");
        graphics.drawString(font, speedName, speedCenter - font.width(speedName) / 2, 17, 0x404040, false);
        Component speedLevel = Component.translatable("gui.fastgrowth.level",
                speedSlot.getItem().getCount(), speedSlot.getMaxStackSize());
        graphics.drawString(font, speedLevel, speedCenter - font.width(speedLevel) / 2, 48, 0x404040, false);

        // ima
        int magnetCenter = HarvesterMenu.MAGNET_SLOT_X + 8;
        Component magnetName = Component.translatable("gui.fastgrowth.magnet");
        graphics.drawString(font, magnetName, magnetCenter - font.width(magnetName) / 2, 17, 0x404040, false);
        boolean magnetOn = !menu.getSlot(HarvesterBlockEntity.MAGNET_SLOT).getItem().isEmpty();
        Component magnetState = Component.translatable(magnetOn
                ? "gui.fastgrowth.magnet_on" : "gui.fastgrowth.magnet_off");
        graphics.drawString(font, magnetState, magnetCenter - font.width(magnetState) / 2, 48, 0x404040, false);

        float seconds = menu.getIntervalTicks() / 20f;
        Component status = Component.translatable("gui.fastgrowth.harvest_status",
                menu.getRadius(), String.format(Locale.ROOT, "%.2f", seconds));
        graphics.drawString(font, status, (imageWidth - font.width(status)) / 2, 60, 0x404040, false);

        // energia: guardada/capacidade na esquerda, consumo ou estado na direita
        Component stored = Component.translatable("gui.fastgrowth.energy_stored",
                compact(menu.getEnergy()), compact(menu.getEnergyCapacity()));
        graphics.drawString(font, stored, BAR_X, 84, 0x404040, false);

        Component right;
        int rightColor = 0x404040;
        switch (menu.getStatus()) {
            case HarvesterBlockEntity.STATUS_NO_ENERGY -> {
                right = Component.translatable("gui.fastgrowth.state_no_energy");
                rightColor = 0xAA0000;
            }
            case HarvesterBlockEntity.STATUS_FULL -> {
                right = Component.translatable("gui.fastgrowth.state_full");
                rightColor = 0xAA0000;
            }
            case HarvesterBlockEntity.STATUS_STANDBY ->
                    right = Component.translatable("gui.fastgrowth.state_standby");
            default -> right = menu.getEnergyUsage() > 0
                    ? Component.translatable("gui.fastgrowth.energy_use", compact(menu.getEnergyUsage()))
                    : Component.translatable("gui.fastgrowth.energy_free");
        }
        graphics.drawString(font, right, BAR_X + BAR_W - font.width(right), 84, rightColor, false);
    }

    /** 12500 -> "12.5k", 5000000 -> "5M" (curto para caber na GUI; o valor exato aparece no tooltip da barra). */
    private static String compact(int value) {
        if (value >= 1_000_000) return trim(value / 1_000_000.0) + "M";
        if (value >= 1_000) return trim(value / 1_000.0) + "k";
        return Integer.toString(value);
    }

    private static String trim(double v) {
        String s = v >= 100 ? String.format(Locale.ROOT, "%.0f", v)
                : v >= 10 ? String.format(Locale.ROOT, "%.1f", v)
                : String.format(Locale.ROOT, "%.2f", v);
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return s;
    }
}

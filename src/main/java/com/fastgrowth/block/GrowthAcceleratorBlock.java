package com.fastgrowth.block;

import com.fastgrowth.item.ParticleInhibitorItem;
import com.fastgrowth.item.UpgradeItem;
import com.fastgrowth.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GrowthAcceleratorBlock extends BaseEntityBlock {
    private final Tier tier;

    public GrowthAcceleratorBlock(Properties properties, Tier tier) {
        super(properties);
        this.tier = tier;
    }

    public Tier getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.fastgrowth.machine_energy",
                tier.energyBaseUse(), tier.maxEnergyUse()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GrowthAcceleratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.GROWTH_ACCELERATOR.get(),
                GrowthAcceleratorBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof GrowthAcceleratorBlockEntity be)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        // Segurando o Inibidor de Particulas: instala direto.
        if (held.getItem() instanceof ParticleInhibitorItem) {
            if (be.tryInstallInhibitor(held)) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                player.displayClientMessage(Component.translatable("message.fastgrowth.inhibitor_installed"), true);
            } else {
                player.displayClientMessage(Component.translatable("message.fastgrowth.inhibitor_already"), true);
            }
            return InteractionResult.CONSUME;
        }

        // Segurando um upgrade: instala direto, sem abrir a interface.
        if (held.getItem() instanceof UpgradeItem upgrade) {
            if (upgrade.getTier() != tier) {
                player.displayClientMessage(Component.translatable("message.fastgrowth.wrong_tier"), true);
            } else if (be.tryAddUpgrade(held)) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, 1.2f);
                player.displayClientMessage(Component.translatable("message.fastgrowth.upgrade_installed"), true);
            } else {
                player.displayClientMessage(Component.translatable("message.fastgrowth.upgrade_max"), true);
            }
            return InteractionResult.CONSUME;
        }

        // Qualquer outro item (ou mao vazia): abre a interface.
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, be, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof GrowthAcceleratorBlockEntity be) {
                be.dropUpgrades(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}

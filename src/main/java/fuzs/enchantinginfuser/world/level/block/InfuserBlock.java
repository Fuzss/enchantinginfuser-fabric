package fuzs.enchantinginfuser.world.level.block;

import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.network.message.S2CInfuserDataMessage;
import fuzs.enchantinginfuser.registry.ModRegistry;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

@SuppressWarnings("deprecation")
public class InfuserBlock extends EnchantmentTableBlock {
    public InfuserBlock(Properties p_52953_) {
        super(p_52953_);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new InfuserBlockEntity(pPos, pState);
    }

    protected InfuserType getInfuserType() {
        return InfuserType.NORMAL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, ModRegistry.INFUSER_BLOCK_ENTITY_TYPE, EnchantmentTableBlockEntity::bookAnimationTick) : null;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.getBlockEntity(pPos) instanceof InfuserBlockEntity blockEntity) {
            if (pLevel.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));
            if (pPlayer.containerMenu instanceof InfuserMenu menu) {
                // items might still be in inventory slots, so this needs to update so that enchantment buttons are shown
                menu.slotsChanged(blockEntity);
                final int power = menu.setEnchantingPower(pLevel, pPos);
                EnchantingInfuser.NETWORK.sendTo(new S2CInfuserDataMessage(pPlayer.containerMenu.containerId, power, this.getInfuserType()), (ServerPlayer) pPlayer);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    @Nullable
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        if (pLevel.getBlockEntity(pPos) instanceof InfuserBlockEntity blockentity) {
            Component component = blockentity.getDisplayName();
            return new SimpleMenuProvider((p_52959_, p_52960_, p_52961_) -> {
                if (blockentity.canOpen(p_52961_)) {
                    return new InfuserMenu(p_52959_, p_52960_, blockentity, ContainerLevelAccess.create(pLevel, pPos), this.getInfuserType());
                }
                return null;
            }, component);
        } else {
            return null;
        }
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand) {
        super.animateTick(pState, pLevel, pPos, pRand);
        for(int i = -2; i <= 2; ++i) {
            for(int j = -2; j <= 2; ++j) {
                if (i > -2 && i < 2 && j == -1) {
                    j = 2;
                }
                if (pRand.nextInt(16) == 0) {
                    for(int k = 0; k <= 1; ++k) {
                        BlockPos blockpos = pPos.offset(i, k, j);
                        if (pLevel.getBlockState(blockpos).is(Blocks.BOOKSHELF)) {
                            if (!InfuserMenu.isBlockEmpty(pLevel, pPos.offset(i / 2, 0, j / 2))) {
                                break;
                            }
                            pLevel.addParticle(ParticleTypes.ENCHANT, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 2.0D, (double)pPos.getZ() + 0.5D, (double)((float)i + pRand.nextFloat()) - 0.5D, (double)((float)k - pRand.nextFloat() - 1.0F), (double)((float)j + pRand.nextFloat()) - 0.5D);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (worldIn.getBlockEntity(pos) instanceof InfuserBlockEntity blockEntity) {
                Containers.dropContents(worldIn, pos, blockEntity);
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        if (worldIn.getBlockEntity(pos) instanceof InfuserBlockEntity blockEntity) {
            if (!blockEntity.getItem(0).isEmpty()) {
                return 15;
            }
        }
        return 0;
    }

    public enum InfuserType {
        NORMAL, ADVANCED;

        public boolean isAdvanced() {
            return this == ADVANCED;
        }
    }
}

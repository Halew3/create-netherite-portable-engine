package ru.vladislav117.netheriteportableengine.content;

import com.simibubi.create.foundation.utility.BlockHelper;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlock;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlockEntity;
import dev.simulated_team.simulated.service.SimItemService;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import ru.vladislav117.netheriteportableengine.CreateNetheritePortableEngine;

public class NetheritePortableEngineBlock extends PortableEngineBlock {
    public NetheritePortableEngineBlock(Properties properties, DyeColor color) {
        super(properties, color);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        DyeColor dye = SimItemService.getDyeColor(heldItem);
        if (dye == null) {
            return super.useItemOn(heldItem, state, level, pos, player, hand, hit);
        }
        // Handle this before Simulated replaces the block with an ordinary engine.
        if (!level.isClientSide) {
            BlockState dyedState = BlockHelper.copyProperties(state,
                    CreateNetheritePortableEngine.DYED_ENGINES.get(dye).get().defaultBlockState());
            level.setBlockAndUpdate(pos, dyedState);
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0f,
                    1.1f - level.random.nextFloat() * .2f);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() instanceof NetheritePortableEngineBlock) {
            return;
        }

        PortableEngineBlockEntity be = (PortableEngineBlockEntity) level.getBlockEntity(pos);
        if (be != null && !be.inventory.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), be.inventory.getItem(0));
        }

        level.removeBlockEntity(pos);
    }

    @Override
    public Class<PortableEngineBlockEntity> getBlockEntityClass() {
        return PortableEngineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PortableEngineBlockEntity> getBlockEntityType() {
        return CreateNetheritePortableEngine.NETHERITE_PORTABLE_ENGINE_BE.get();
    }
}

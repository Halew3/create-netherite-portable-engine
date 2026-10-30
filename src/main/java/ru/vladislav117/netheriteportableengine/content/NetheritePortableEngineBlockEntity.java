package ru.vladislav117.netheriteportableengine.content;

import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class NetheritePortableEngineBlockEntity extends PortableEngineBlockEntity {
    public static final int FUEL_CONSUMPTION_MULTIPLIER = 16;

    public NetheritePortableEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private static int analogPower(int burnTime) {
        return burnTime > 0 ? Math.min(burnTime / 200, 14) + 1 : 0;
    }

    @Override
    public void tick() {
        int previousBurnTime = getCurrentBurnTime();
        if (level != null && !isVirtual() && !isCurrentFuelInfinite() && previousBurnTime > 0) {
            // Simulated consumes the final tick, handles refuelling, and updates heat/rotation.
            // Leave at least one tick so it still sees this as a burning engine this tick.
            setCurrentBurnTime(Math.max(1, previousBurnTime - (FUEL_CONSUMPTION_MULTIPLIER - 1)));
        }
        super.tick();
        if (level != null && !level.isClientSide && !isVirtual()
                && analogPower(previousBurnTime) != analogPower(getCurrentBurnTime())) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }
}

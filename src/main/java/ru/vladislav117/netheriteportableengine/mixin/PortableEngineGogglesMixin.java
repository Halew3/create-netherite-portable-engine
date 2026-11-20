package ru.vladislav117.netheriteportableengine.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import ru.vladislav117.netheriteportableengine.content.NetheritePortableEngineBlockEntity;

@Mixin(value = PortableEngineBlockEntity.class, remap = false)
abstract class PortableEngineGogglesMixin {
    @Unique
    private static final int NETHERITE_TICKS_PER_SECOND = 20;

    @ModifyExpressionValue(method = "addToGoggleTooltip", at = @At(value = "INVOKE",
            target = "Ldev/simulated_team/simulated/content/blocks/portable_engine/PortableEngineBlockEntity;getCurrentBurnTime()I"))
    private int netheriteportableengine$displayCurrentBurnTime(int burnTime) {
        if ((Object) this instanceof NetheritePortableEngineBlockEntity engine) {
            return netheriteportableengine$roundForDisplay(engine.getCurrentRunningTicks());
        }
        return burnTime;
    }

    @ModifyExpressionValue(method = "addToGoggleTooltip", at = @At(value = "INVOKE",
            target = "Ldev/simulated_team/simulated/content/blocks/portable_engine/PortableEngineBlockEntity;getTotalBurnTime()I"))
    private int netheriteportableengine$displayTotalBurnTime(int burnTime) {
        if ((Object) this instanceof NetheritePortableEngineBlockEntity engine) {
            return netheriteportableengine$roundForDisplay(engine.getTotalRunningTicks());
        }
        return burnTime;
    }

    @Unique
    private static int netheriteportableengine$roundForDisplay(long runningTicks) {
        // Simulated divides by 20 and leaves zero seconds blank. Show the last partial second as 1s.
        long seconds = Math.ceilDiv(runningTicks, NETHERITE_TICKS_PER_SECOND);
        return (int) Math.min(Integer.MAX_VALUE, seconds * NETHERITE_TICKS_PER_SECOND);
    }
}

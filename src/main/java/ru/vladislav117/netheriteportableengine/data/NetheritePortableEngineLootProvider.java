package ru.vladislav117.netheriteportableengine.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import ru.vladislav117.netheriteportableengine.CreateNetheritePortableEngine;

import java.util.Set;

public class NetheritePortableEngineLootProvider extends BlockLootSubProvider {
    protected NetheritePortableEngineLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
    }

    @Override
    protected void generate() {
        CreateNetheritePortableEngine.allEngines().forEach(this::dropSelf);
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return CreateNetheritePortableEngine.allEngines().map(block -> (Block) block).toList();
    }
}

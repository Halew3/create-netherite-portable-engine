package ru.vladislav117.netheriteportableengine;

import com.simibubi.create.api.stress.BlockStressValues;
import dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyTypes;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlockEntity;
import dev.simulated_team.simulated.service.SimItemService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import ru.vladislav117.netheriteportableengine.content.NetheritePortableEngineBlockEntity;
import ru.vladislav117.netheriteportableengine.content.recipe.PortableEngineDyeingRecipe;

import java.util.List;

@GameTestHolder(CreateNetheritePortableEngine.ID)
@PrefixGameTestTemplate(false)
public class EngineGameTests {
    private static final BlockPos POS = new BlockPos(1, 1, 1);

    @GameTest(template = "empty")
    public static void fuelBurnsEightTimesAsFast(GameTestHelper helper) {
        CreateNetheritePortableEngine.allEngines().forEach(block -> {
            helper.setBlock(POS, block.defaultBlockState());
            NetheritePortableEngineBlockEntity engine = helper.getBlockEntity(POS);
            for (ItemStack fuel : List.of(new ItemStack(Items.COAL, 3), new ItemStack(Items.STICK, 3),
                    new ItemStack(Items.LAVA_BUCKET), new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:blaze_cake")), 3))) {
                engine.setCurrentBurnTime(0);
                engine.inventory.slot.setStack(fuel.copy());
                int standardDuration = SimItemService.INSTANCE.getBurnTime(fuel);
                helper.assertTrue(standardDuration > 0, "Test fuel must be accepted");
                engine.tick();
                helper.assertTrue(engine.getCurrentBurnTime() == standardDuration, "Fuel should be loaded normally");
                boolean superheated = engine.isSuperHeated();
                engine.tick();
                helper.assertTrue(engine.getCurrentBurnTime() == Math.max(0, standardDuration - 8), "Fuel must drain by eight per tick");
                helper.assertTrue(engine.isSuperHeated() == superheated, "Fuel heat must be preserved");
                if (fuel.is(Items.LAVA_BUCKET)) {
                    helper.assertTrue(engine.inventory.getItem(0).is(Items.BUCKET), "Lava must leave its bucket");
                }
            }
            engine.inventory.clearContent();
            engine.setCurrentBurnTime(100);
            for (int tick = 0; tick < 13; tick++) engine.tick();
            helper.assertTrue(engine.getCurrentBurnTime() == 0, "100 fuel ticks (including cake) must expire after 13 ticks at 8x drain");
            engine.setCurrentBurnTime(PortableEngineBlockEntity.INFINITE_THRESHOLD);
            engine.tick();
            helper.assertTrue(engine.isCurrentFuelInfinite(), "Creative fuel must stay infinite");
            helper.setBlock(POS, net.minecraft.world.level.block.Blocks.AIR);
        });
        // A normal Simulated engine must retain its original fuel consumption.
        helper.setBlock(POS, BuiltInRegistries.BLOCK.get(ResourceLocation.parse("simulated:red_portable_engine")).defaultBlockState());
        PortableEngineBlockEntity regular = helper.getBlockEntity(POS);
        regular.setCurrentBurnTime(100);
        regular.tick();
        helper.assertTrue(regular.getCurrentBurnTime() == 99, "Ordinary engines must remain unchanged");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void gogglesTimesTrackActualTicks(GameTestHelper helper) {
        helper.setBlock(POS, CreateNetheritePortableEngine.NETHERITE_PORTABLE_ENGINE.get().defaultBlockState());
        NetheritePortableEngineBlockEntity engine = helper.getBlockEntity(POS);
        engine.inventory.slot.setStack(new ItemStack(Items.COAL, 2));
        engine.tick();
        helper.assertTrue(engine.getCurrentRunningTicks() == 200, "One coal must show ten seconds of burn time");
        helper.assertTrue(engine.getTotalRunningTicks() == 400, "Two coal must show twenty seconds of burn time");

        for (int tick = 0; tick < 20; tick++) engine.tick();
        helper.assertTrue(engine.getCurrentRunningTicks() == 180, "The current fuel must count down one second");
        helper.assertTrue(engine.getTotalRunningTicks() == 380, "Total fuel must count down one second");

        engine.inventory.slot.setStack(new ItemStack(Items.STICK, 3));
        engine.setCurrentBurnTime(1);
        helper.assertTrue(engine.getCurrentRunningTicks() == 1, "The last partial fuel tick still takes a full game tick");
        helper.assertTrue(engine.getTotalRunningTicks() == 40, "Round each queued stick up to its full thirteen ticks");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void craftingDyePreservesNetheriteAndComponents(GameTestHelper helper) {
        var recipe = new PortableEngineDyeingRecipe(CraftingBookCategory.MISC);
        CreateNetheritePortableEngine.allEngines().forEach(block -> {
            for (DyeColor color : DyeColor.values()) {
                ItemStack engine = new ItemStack(block, 4);
                engine.set(DataComponents.CUSTOM_NAME, Component.literal("Keep my engine"));
                CraftingInput input = CraftingInput.of(2, 1, List.of(engine, new ItemStack(DyeItem.byColor(color))));
                helper.assertTrue(recipe.matches(input, helper.getLevel()), "Every netherite color must accept dyes");
                ItemStack result = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
                        .orElseThrow().value().assemble(input, helper.getLevel().registryAccess());
                helper.assertTrue(result.is(CreateNetheritePortableEngine.DYED_ENGINES.get(color).get().asItem()), "Dye recipe must retain netherite tier");
                helper.assertTrue(result.getCount() == 1, "Crafting must produce exactly one engine");
                helper.assertTrue(Component.literal("Keep my engine").equals(result.get(DataComponents.CUSTOM_NAME)), "Crafting must preserve components");
            }
        });
        CraftingInput ordinary = CraftingInput.of(2, 1, List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("simulated:red_portable_engine"))), new ItemStack(Items.BLUE_DYE)));
        helper.assertTrue(recipe.assemble(ordinary, helper.getLevel().registryAccess()).is(BuiltInRegistries.ITEM.get(ResourceLocation.parse("simulated:blue_portable_engine"))), "Ordinary dyeing must still work");
        helper.assertTrue(!recipe.matches(CraftingInput.of(3, 1, List.of(CreateNetheritePortableEngine.NETHERITE_PORTABLE_ENGINE_ITEM.toStack(), new ItemStack(Items.BLUE_DYE), new ItemStack(Items.RED_DYE))), helper.getLevel()), "Two dyes must be rejected");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void placedDyePreservesEngineState(GameTestHelper helper) {
        helper.setBlock(POS, CreateNetheritePortableEngine.NETHERITE_PORTABLE_ENGINE.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)
                .setValue(BlockStateProperties.LIT, true));
        NetheritePortableEngineBlockEntity engine = helper.getBlockEntity(POS);
        engine.setCurrentBurnTime(1200);
        engine.setSuperHeated(true);
        engine.inventory.slot.setStack(new ItemStack(Items.COAL, 17));
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (DyeColor color : DyeColor.values()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(DyeItem.byColor(color)));
            helper.useBlock(POS, player);
            helper.assertTrue(helper.getBlockEntity(POS) == engine, "Recoloring must keep the block entity");
            var state = helper.getBlockState(POS);
            helper.assertTrue(state.is(CreateNetheritePortableEngine.DYED_ENGINES.get(color).get()), "Placed engine must use requested color");
            helper.assertTrue(state.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.EAST, "Facing must survive dyeing");
            helper.assertTrue(state.getValue(BlockStateProperties.LIT) && engine.isSuperHeated(), "Heat must survive dyeing");
            helper.assertTrue(engine.getCurrentBurnTime() == 1200 && engine.inventory.getItem(0).getCount() == 17, "Fuel must survive dyeing");
            var drops = Block.getDrops(state, helper.getLevel(), helper.absolutePos(POS), engine);
            helper.assertTrue(drops.size() == 1 && drops.getFirst().is(state.getBlock().asItem()), "Mining must preserve dye color");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void allEngineStatesHaveMass16AndOriginalPower(GameTestHelper helper) {
        CreateNetheritePortableEngine.allEngines().forEach(block -> {
            helper.assertTrue(BlockStressValues.getCapacity(block) == 256.0, "Stress capacity must remain 256");
            for (var state : block.getStateDefinition().getPossibleStates()) {
                double mass = ((Number) ((BlockStateExtension) state).sable$getProperty(PhysicsBlockPropertyTypes.getPropertyType(ResourceLocation.parse("sable:mass")))).doubleValue();
                helper.assertTrue(mass == 16.0, "Every color, facing, and heat state must have mass 16; got " + mass);
                helper.assertTrue(CreateNetheritePortableEngine.NETHERITE_PORTABLE_ENGINE_BE.get().isValid(state), "All colors must share the custom block entity");
            }
        });
        helper.succeed();
    }
}

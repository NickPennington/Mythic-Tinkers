package derekahedron.mythictinkers.tinkers.modifiers.modules;

import derekahedron.mythictinkers.tinkers.hooks.DestroySpeedModifierHook;
import derekahedron.mythictinkers.tinkers.hooks.MTModifierHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.block.BlockPredicate;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BreakSpeedContext;
import slimeknights.tconstruct.library.modifiers.hook.mining.BreakSpeedModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public record MaximumDestroySpeedModule(float destroySpeed, IJsonPredicate<BlockState> block) implements ModifierModule, BreakSpeedModifierHook, DestroySpeedModifierHook {
    public static final List<ModuleHook<?>> DEFAULT_HOOKS =
            HookProvider.defaultHooks(ModifierHooks.BREAK_SPEED, MTModifierHooks.DESTROY_SPEED);
    public static final RecordLoadable<MaximumDestroySpeedModule> LOADER =
            RecordLoadable.create(
                    FloatLoadable.FROM_ZERO.requiredField("break_speed", MaximumDestroySpeedModule::destroySpeed),
                    BlockPredicate.LOADER.defaultField("block", MaximumDestroySpeedModule::block),
                    MaximumDestroySpeedModule::new);

    @Override
    public void onBreakSpeed(
            IToolStackView tool, ModifierEntry modifier, PlayerEvent.BreakSpeed event,
            Direction direction, boolean isEffective, float speed) {}

    @Override
    public float modifyBreakSpeed(
            IToolStackView tool, ModifierEntry modifier, BreakSpeedContext context, float speed) {
        BlockPos pos = context.pos();
        if (pos == null) return speed;
        if (context.isEffective()) return speed;

        float destroySpeed = context.state().getDestroySpeed(context.player().level(), pos);

        for (ModifierEntry modifierEntry : tool.getModifiers()) {
            destroySpeed = modifierEntry.getHook(MTModifierHooks.DESTROY_SPEED).onDestroySpeed(
                    tool, modifier, context.player(), context.state(), destroySpeed, context.isEffective(), context.player().level(), pos);
        }

        if (this.destroySpeed > destroySpeed) return speed;
        if (!block.matches(context.state())) return speed;

        return speed * destroySpeed / this.destroySpeed;
    }

    @Override
    public float onDestroySpeed(
            IToolStackView tool, ModifierEntry modifier, Player player, BlockState state,
            float baseSpeed, boolean correctForDrops, BlockGetter level, BlockPos pos) {
        return (correctForDrops && baseSpeed > destroySpeed && block.matches(state))
                ? destroySpeed
                : baseSpeed;
    }

    @Override
    public RecordLoadable<MaximumDestroySpeedModule> getLoader() {
        return LOADER;
    }

    @Override
    public List<ModuleHook<?>> getDefaultHooks() {
        return DEFAULT_HOOKS;
    }
}

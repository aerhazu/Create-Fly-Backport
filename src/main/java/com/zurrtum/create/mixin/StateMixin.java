package com.zurrtum.create.mixin;

import com.zurrtum.create.foundation.storage.StateExtensions;
import net.minecraft.state.State;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(State.class)
public abstract class StateMixin implements StateExtensions {
}

package com.zurrtum.create.mixin;

import com.zurrtum.create.foundation.storage.NbtCompoundExtensions;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NbtCompound.class)
public abstract class NbtCompoundMixin implements NbtCompoundExtensions {
}

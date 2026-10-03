package com.zurrtum.create.mixin;

import com.zurrtum.create.foundation.storage.NbtListExtensions;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NbtList.class)
public abstract class NbtListMixin implements NbtListExtensions {
}

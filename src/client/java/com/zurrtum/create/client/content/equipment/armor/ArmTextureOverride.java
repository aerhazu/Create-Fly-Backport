package com.zurrtum.create.client.content.equipment.armor;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

// Shared between HeldItemRendererMixin and PlayerEntityRendererMixin: mixin-added members must be
// private, so cross-mixin state can't live as a field/method on either mixin class directly.
public class ArmTextureOverride {
    @Nullable
    public static Identifier current;
}

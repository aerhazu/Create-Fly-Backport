package com.zurrtum.create.foundation.storage;

import net.minecraft.state.State;
import net.minecraft.state.property.Property;

/**
 * Backport shim injected onto {@code State} (see loom:injected_interfaces in fabric.mod.json) to restore the
 * default-value overload of {@code get} that Mojang added after 1.21.1.
 */
public interface StateExtensions {
    private State<?, ?> self() {
        return (State<?, ?>) (Object) this;
    }

    default <T extends Comparable<T>> T get(Property<T> property, T fallback) {
        State<?, ?> self = self();
        return self.contains(property) ? self.get(property) : fallback;
    }
}

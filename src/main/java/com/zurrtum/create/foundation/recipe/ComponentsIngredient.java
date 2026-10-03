package com.zurrtum.create.foundation.recipe;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Backport shim: 1.21.1's {@link Ingredient} is a final class with no custom-ingredient extension point
 * (unlike the later Fabric Custom Ingredient API this codebase was originally written against), so this can
 * no longer extend Ingredient. It stands alone as a {@link Predicate}, checked directly in Java by the
 * sequenced-assembly step recipes rather than round-tripped through JSON: the "target"/"ingredient" field
 * written for a step matches only the transitional item's base item (a real, parseable Ingredient), and this
 * predicate is attached to the parsed recipe object afterward to additionally require the exact
 * assembly-progress stage, so players can't apply a step's item to a transitional item at the wrong stage.
 */
public class ComponentsIngredient implements Predicate<ItemStack> {
    private final Ingredient base;
    private final ComponentChanges components;

    public ComponentsIngredient(Ingredient base, ComponentChanges components) {
        if (components.isEmpty()) {
            throw new IllegalArgumentException("ComponentIngredient must have at least one defined component");
        }

        this.base = base;
        this.components = components;
    }

    public boolean isEmpty() {
        return base.isEmpty();
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!base.test(stack))
            return false;

        // None strict matching
        for (Map.Entry<ComponentType<?>, Optional<?>> entry : components.entrySet()) {
            final ComponentType<?> type = entry.getKey();
            final Optional<?> value = entry.getValue();

            if (value.isPresent()) {
                // Expect the stack to contain a matching component
                if (!stack.contains(type)) {
                    return false;
                }

                if (!Objects.equals(value.get(), stack.get(type))) {
                    return false;
                }
            } else {
                // Expect the target stack to not contain this component
                if (stack.contains(type)) {
                    return false;
                }
            }
        }

        return true;
    }

    public Ingredient getBase() {
        return base;
    }

    @Nullable
    public ComponentChanges getComponents() {
        return components;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ComponentsIngredient that = (ComponentsIngredient) o;
        return base.equals(that.base) && components.equals(that.components);
    }

}

package com.zurrtum.create.foundation.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.*;

import java.util.Optional;

public class IngredientTextContent implements TextContent {
    private static final Codec<Ingredient> INGREDIENT_CODEC = Ingredient.DISALLOW_EMPTY_CODEC;
    public static final MapCodec<IngredientTextContent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        INGREDIENT_CODEC.optionalFieldOf("ingredient").forGetter(i -> Optional.ofNullable(i.ingredient)),
        TextCodecs.CODEC.optionalFieldOf("name").forGetter(i -> Optional.ofNullable(i.name))
    ).apply(instance, IngredientTextContent::new));

    public Ingredient ingredient;
    public Text name;
    public static final Type<IngredientTextContent> TYPE = new Type<>(CODEC, "translatable");

    @Override
    public Type<?> getType() {
        return TYPE;
    }

    public IngredientTextContent(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public IngredientTextContent(Optional<Ingredient> ingredient, Optional<Text> name) {
        name.ifPresentOrElse(value -> this.name = value, () -> this.ingredient = ingredient.orElse(null));
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
        if (name != null) {
            return name.visit(visitor);
        }
        return findName().flatMap(text -> text.visit(visitor));
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
        if (name != null) {
            return name.visit(visitor, style);
        }
        return findName().flatMap(text -> text.visit(visitor, style));
    }

    private Optional<Text> findName() {
        if (ingredient != null) {
            ItemStack[] stacks = ingredient.getMatchingStacks();
            if (stacks.length > 0) {
                name = stacks[0].getName();
                ingredient = null;
                return Optional.of(name);
            }
        }
        return Optional.empty();
    }

    public Optional<Text> getName() {
        if (name != null) {
            return Optional.of(name);
        }
        return findName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IngredientTextContent other) {
            Optional<Text> name = getName();
            Optional<Text> otherName = other.getName();
            if (name.isPresent() && otherName.isPresent()) {
                return name.get().equals(otherName.get());
            } else {
                return true;
            }
        }
        return false;
    }
}

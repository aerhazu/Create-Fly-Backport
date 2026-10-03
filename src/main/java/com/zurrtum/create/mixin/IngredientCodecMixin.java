package com.zurrtum.create.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

// Backports the bare-string ingredient shorthand ("create:cogwheel" / "#c:plates/gold") that this
// codebase's recipe JSON files rely on throughout. 1.21.1's Ingredient is a final class with no
// custom-ingredient extension point (unlike the later Fabric Custom Ingredient API this codebase
// was originally written against), and its vanilla codec only accepts {"item":...}/{"tag":...}
// objects or arrays of them, so this adds the shorthand as a codec alternative instead.
//
// fabric-recipe-api-v1 (fabric-recipe-api-v1.mixins.json:ingredient.IngredientMixin) also injects at
// createCodec's RETURN, at the default priority (1000), to add its own custom-ingredient dispatch codec,
// by wrapping whatever createCodec already returned in another Codec.either(...). Two default-priority
// @Inject(at = "RETURN") handlers from different mods' configs targeting the same instruction don't have
// a guaranteed relative order in Fabric Loader - which one runs first (and so which one's Codec.either(...)
// ends up as the outer layer, wrapping the other's via cir.getReturnValue()) was observed to flip between
// otherwise-identical launches, and landing on the wrong side of that wrapping breaks the shorthand
// fallback for certain inputs. A lower priority here forces this mixin to always apply before (i.e. be
// wrapped by) fabric-recipe-api-v1's, which was verified to give a deterministic, correct result across
// repeated launches, whereas the reverse order (or leaving priority unset) was not.
@Mixin(value = Ingredient.class, priority = 500)
public class IngredientCodecMixin {
    @Inject(method = "createCodec(Z)Lcom/mojang/serialization/Codec;", at = @At("RETURN"), cancellable = true)
    private static void create$allowStringShorthand(boolean allowEmpty, CallbackInfoReturnable<Codec<Ingredient>> cir) {
        Codec<Ingredient> arrayShorthand = Codec.STRING.listOf()
            .comapFlatMap(IngredientCodecMixin::create$parseArrayShorthand, ingredient -> List.of());
        Codec<Ingredient> shorthand = Codec.STRING.comapFlatMap(IngredientCodecMixin::create$parseShorthand, ingredient -> "");
        cir.setReturnValue(Codec.withAlternative(Codec.withAlternative(cir.getReturnValue(), arrayShorthand), shorthand));
    }

    private static DataResult<Ingredient> create$parseShorthand(String value) {
        boolean tag = value.startsWith("#");
        Identifier id = Identifier.tryParse(tag ? value.substring(1) : value);
        if (id == null) {
            return DataResult.error(() -> "Invalid identifier: " + value);
        }
        if (tag) {
            return DataResult.success(Ingredient.fromTag(TagKey.of(RegistryKeys.ITEM, id)));
        }
        return Registries.ITEM.getOrEmpty(id)
            .map(item -> DataResult.success(Ingredient.ofItems(item)))
            .orElseGet(() -> DataResult.error(() -> "Unknown item: " + value));
    }

    // Backports the array-of-bare-strings ingredient shorthand (e.g. ["minecraft:horn_coral", "minecraft:brain_coral"])
    // that some of this codebase's recipe JSON files use, by parsing each entry with the same logic as the single
    // bare-string shorthand above and unioning the resulting matching stacks into one Ingredient.
    private static DataResult<Ingredient> create$parseArrayShorthand(List<String> values) {
        if (values.isEmpty()) {
            return DataResult.error(() -> "Item array cannot be empty, at least one item must be defined");
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (String value : values) {
            DataResult<Ingredient> result = create$parseShorthand(value);
            var error = result.error();
            if (error.isPresent()) {
                String message = error.get().message();
                return DataResult.error(() -> message);
            }
            result.result().ifPresent(ingredient -> stacks.addAll(List.of(ingredient.getMatchingStacks())));
        }
        return DataResult.success(Ingredient.ofStacks(stacks.stream()));
    }
}

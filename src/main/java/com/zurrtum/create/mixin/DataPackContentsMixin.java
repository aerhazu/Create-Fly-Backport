package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.mixer.PotionRecipe;
import com.zurrtum.create.foundation.recipe.RecipeFinder;
import com.zurrtum.create.foundation.recipe.trie.RecipeTrieFinder;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

@Mixin(DataPackContents.class)
public class DataPackContentsMixin {
    @ModifyReturnValue(method = "getContents()Ljava/util/List;", at = @At("RETURN"))
    private List<ResourceReloader> add(List<ResourceReloader> original) {
        List<ResourceReloader> list = new ArrayList<>(original);
        list.add(RecipeFinder.LISTENER);
        list.add(RecipeTrieFinder.LISTENER);
        list.add(BeltHelper.LISTENER);
        list.add(AllConfigs.LISTENER);
        return Collections.unmodifiableList(list);
    }

    @Inject(method = "method_58296(Lnet/minecraft/resource/featuretoggle/FeatureSet;Lnet/minecraft/server/command/CommandManager$RegistrationEnvironment;ILnet/minecraft/resource/ResourceManager;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Lnet/minecraft/registry/CombinedDynamicRegistries;)Ljava/util/concurrent/CompletionStage;", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/SimpleResourceReload;start(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/resource/ResourceReload;"))
    private static void onReload(
        FeatureSet featureSet,
        CommandManager.RegistrationEnvironment registrationEnvironment,
        int i,
        ResourceManager resourceManager,
        Executor executor,
        Executor executor2,
        CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries,
        CallbackInfoReturnable<CompletionStage<DataPackContents>> cir
    ) {
        PotionRecipe.data = new PotionRecipe.ReloadData(combinedDynamicRegistries.getCombinedRegistryManager(), featureSet);
    }
}

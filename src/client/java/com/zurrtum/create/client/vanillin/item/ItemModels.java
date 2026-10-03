package com.zurrtum.create.client.vanillin.item;

import com.zurrtum.create.client.flywheel.api.material.Transparency;
import com.zurrtum.create.client.flywheel.api.model.Mesh;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.lib.material.Materials;
import com.zurrtum.create.client.flywheel.lib.material.SimpleMaterial;
import com.zurrtum.create.client.flywheel.lib.model.ModelUtil;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import com.zurrtum.create.client.flywheel.lib.model.baked.BakedItemModelBufferer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ItemChunkLayerSortedListBuilder;
import com.zurrtum.create.client.flywheel.lib.model.baked.MeshHelper;
import com.zurrtum.create.client.flywheel.lib.util.RendererReloadCache;
import com.zurrtum.create.client.vanillin.Vanillin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;

public class ItemModels {
    public static final TagKey<Item> NO_INSTANCING = TagKey.of(RegistryKeys.ITEM, Vanillin.rl("no_instancing"));
    private static final Model EMPTY_MODEL = new SimpleModel(List.of());
    private static final RendererReloadCache<BakedModelKey, Model> MODEL_CACHE = new RendererReloadCache<>(key -> bakeModel(
        key.world(),
        key.stack(),
        key.displayContext()
    ));

    /**
     * 1.21.1 has no per-frame item render state to introspect for "is this model animated" the way later
     * versions do; the {@code NO_INSTANCING} tag is the mechanism for excluding items that render poorly
     * (or crash) when their quads are captured once and replayed by Flywheel instead of rendered live.
     */
    public static boolean isSupported(ItemStack stack, ModelTransformationMode context) {
        return !stack.isIn(NO_INSTANCING);
    }

    public static BakedModel getModel(ItemStack stack) {
        return MinecraftClient.getInstance().getItemRenderer().getModels().getModel(stack);
    }

    public static Model get(World world, ItemStack itemStack, ModelTransformationMode displayContext) {
        if (itemStack.isEmpty()) {
            return EMPTY_MODEL;
        }
        ClientWorld clientWorld = world instanceof ClientWorld ? (ClientWorld) world : null;
        return MODEL_CACHE.get(new BakedModelKey(clientWorld, itemStack, displayContext));
    }

    public static Model bakeModel(ClientWorld world, ItemStack itemStack, ModelTransformationMode displayContext) {
        var builder = ItemChunkLayerSortedListBuilder.<Model.ConfiguredMesh>getThreadLocal();
        BakedItemModelBufferer.bufferItemStack(
            itemStack, world, displayContext, (renderType, shaded, data) -> {
                var material = ModelUtil.getItemMaterial(renderType);
                if (material == null) {
                    material = Materials.TRANSLUCENT_ENTITY;
                }
                if (itemStack.getItem() instanceof BlockItem && material.transparency() == Transparency.TRANSLUCENT) {
                    material = SimpleMaterial.builderOf(material).transparency(Transparency.ORDER_INDEPENDENT).build();
                }
                Mesh mesh = MeshHelper.blockVerticesToMesh(data, "source=ItemModels,ItemStack=" + itemStack + ",renderType=" + renderType);
                builder.add(renderType, new Model.ConfiguredMesh(material, mesh));
            }, (renderType, material, mesh, translucent) -> {
                if (translucent && itemStack.getItem() instanceof BlockItem && material.transparency() == Transparency.TRANSLUCENT) {
                    material = SimpleMaterial.builderOf(material).transparency(Transparency.ORDER_INDEPENDENT).build();
                }
                builder.add(renderType, new Model.ConfiguredMesh(material, mesh));
            }
        );
        return new SimpleModel(builder.build());
    }

    public record BakedModelKey(ClientWorld world, ItemStack stack, ModelTransformationMode displayContext) {
        @Override
        public int hashCode() {
            return Objects.hash(world, ItemStack.hashCode(stack), displayContext);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof BakedModelKey(ClientWorld otherWorld, ItemStack otherStack, ModelTransformationMode otherDisplayContext))) {
                return false;
            }
            boolean stackEqual = stack == otherStack || ItemStack.areItemsAndComponentsEqual(stack, otherStack);
            return world == otherWorld && stackEqual && displayContext == otherDisplayContext;
        }
    }
}

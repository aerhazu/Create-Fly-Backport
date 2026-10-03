package com.zurrtum.create.client.content.equipment.potatoCannon;

import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileRenderMode;
import com.zurrtum.create.client.AllPotatoProjectileTransforms;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoProjectileEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class PotatoProjectileRenderer extends EntityRenderer<PotatoProjectileEntity> {

    public PotatoProjectileRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(PotatoProjectileEntity entity) {
        return null;
    }

    @Override
    public void render(PotatoProjectileEntity entity, float yaw, float tickProgress, MatrixStack ms, VertexConsumerProvider buffer, int light) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty())
            return;

        MinecraftClient mc = MinecraftClient.getInstance();
        Box box = entity.getBoundingBox();
        float translateY = (float) (box.getLengthY() / 2 - 1 / 8f);
        PotatoProjectileRenderMode mode = entity.getRenderMode();
        PotatoProjectileTransform<PotatoProjectileRenderMode> transformer = AllPotatoProjectileTransforms.get(mode);
        PotatoProjectileState state = new PotatoProjectileState();
        state.mode = mode;
        state.pt = tickProgress;
        state.box = box;
        state.camera = mc.getCameraEntity();
        state.velocity = entity.getVelocity();
        state.age = entity.age;
        state.hash = System.identityHashCode(entity) * 31;

        ms.push();
        ms.translate(0, translateY, 0);
        if (transformer != null) {
            transformer.transform(mode, ms, state);
        }

        mc.getItemRenderer().renderItem(stack, ModelTransformationMode.GROUND, light, OverlayTexture.DEFAULT_UV, ms, buffer, entity.getWorld(), 0);
        ms.pop();
    }

    public static class PotatoProjectileState {
        public PotatoProjectileRenderMode mode;
        public float pt;
        public Box box;
        public Entity camera;
        public Vec3d velocity;
        public int age;
        public int hash;
    }
}

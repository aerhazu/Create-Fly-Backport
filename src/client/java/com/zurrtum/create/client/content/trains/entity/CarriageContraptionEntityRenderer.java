package com.zurrtum.create.client.content.trains.entity;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.AllBogeyStyleRenders;
import com.zurrtum.create.client.content.contraptions.render.ClientContraption;
import com.zurrtum.create.client.content.contraptions.render.OrientedContraptionEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class CarriageContraptionEntityRenderer extends OrientedContraptionEntityRenderer<CarriageContraptionEntity> {

    public CarriageContraptionEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(CarriageContraptionEntity entity, Frustum clippingHelper, double cameraX, double cameraY, double cameraZ) {
        Carriage carriage = entity.getCarriage();
        if (carriage != null)
            for (CarriageBogey bogey : carriage.bogeys)
                if (bogey != null)
                    bogey.couplingAnchors.replace(v -> null);
        return super.shouldRender(entity, clippingHelper, cameraX, cameraY, cameraZ);
    }

    @Override
    protected ClientContraption createClientContraption(Contraption contraption) {
        return new CarriageClientContraption((CarriageContraption) contraption);
    }

    @Override
    public void render(CarriageContraptionEntity entity, float entityYaw, float partialTicks, MatrixStack ms, VertexConsumerProvider buffers, int overlay) {
        boolean pass = !entity.validForRender || entity.firstPositionUpdate;
        if (pass)
            return;

        super.render(entity, entityYaw, partialTicks, ms, buffers, overlay);

        Carriage carriage = entity.getCarriage();
        if (carriage == null)
            return;

        Couple<CarriageBogey> bogeys = carriage.bogeys;
        int bogeySpacing = carriage.bogeySpacing;
        Vec3d position = entity.getLerpedPos(partialTicks);
        float viewYRot = entity.getViewYRot(partialTicks);
        float viewXRot = entity.getViewXRot(partialTicks);
        BlockPos leadingPos = BlockPos.ORIGIN.offset(entity.getInitialOrientation().rotateYCounterclockwise(), bogeySpacing);
        Vec3d cameraPos = entity.getClientCameraPosVec(partialTicks);
        Couple<Float> yaw = Couple.create(null, null);
        Couple<Float> pitch = Couple.create(null, null);
        yaw.replaceWithParams((f, b) -> b == null ? null : b.yaw.getValue(partialTicks), bogeys);
        pitch.replaceWithParams((f, b) -> b == null ? null : b.pitch.getValue(partialTicks), bogeys);

        World world = entity.getWorld();
        ClientContraption clientContraption = getOrCreateClientContraptionLazy(entity.getContraption());

        bogeys.forEachWithContext((bogey, first) -> {
            if (bogey == null)
                return;

            BlockPos bogeyPos = bogey.isLeading ? BlockPos.ORIGIN : leadingPos;

            float bogeyYaw = yaw.get(first);
            float bogeyPitch = pitch.get(first);
            if (!VisualizationManager.supportsVisualization(world) && !clientContraption.getContraption().isHiddenInPortal(bogeyPos)) {

                ms.push();
                translateBogey(ms, bogey, bogeySpacing, viewYRot, viewXRot, bogeyYaw, bogeyPitch);

                int light = getBogeyLightCoords(world, bogey, () -> cameraPos);

                AllBogeyStyleRenders.render(
                    bogey.getStyle(),
                    bogey.getSize(),
                    partialTicks,
                    ms,
                    buffers,
                    light,
                    overlay,
                    bogey.wheelAngle.getValue(partialTicks),
                    bogey.bogeyData,
                    true
                );

                ms.pop();
            }

            bogey.updateCouplingAnchor(position, viewXRot, viewYRot, bogeySpacing, bogeyYaw, bogeyPitch, bogey.isLeading);
            if (bogeys.getSecond() == null) {
                bogey.updateCouplingAnchor(position, viewXRot, viewYRot, bogeySpacing, bogeyYaw, bogeyPitch, !bogey.isLeading);
            }
        });
    }

    public static void translateBogey(MatrixStack ms, CarriageBogey bogey, int bogeySpacing, float viewYRot, float viewXRot, float yaw, float pitch) {
        boolean selfUpsideDown = bogey.isUpsideDown();
        boolean leadingUpsideDown = bogey.carriage.leadingBogey().isUpsideDown();
        TransformStack.of(ms).rotateYDegrees(viewYRot + 90).rotateXDegrees(-viewXRot).rotateYDegrees(180)
            .translate(0, 0, bogey.isLeading ? 0 : -bogeySpacing).rotateYDegrees(-180).rotateXDegrees(viewXRot).rotateYDegrees(-viewYRot - 90)
            .rotateYDegrees(yaw).rotateXDegrees(pitch).translate(0, .5f, 0).rotateZDegrees(selfUpsideDown ? 180 : 0)
            .translateY(selfUpsideDown != leadingUpsideDown ? 2 : 0);
    }

    public static int getBogeyLightCoords(World world, CarriageBogey bogey, Supplier<Vec3d> cameraPos) {
        var anchorPosition = bogey.getAnchorPosition();
        var lightPos = BlockPos.ofFloored(anchorPosition == null ? cameraPos.get() : anchorPosition);
        return LightmapTextureManager.pack(world.getLightLevel(LightType.BLOCK, lightPos), world.getLightLevel(LightType.SKY, lightPos));
    }

}

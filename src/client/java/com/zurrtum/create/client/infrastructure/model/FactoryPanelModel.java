package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.model.BakedQuadHelper;
import com.zurrtum.create.client.foundation.model.SimpleQuadBakedModel;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.zurrtum.create.content.logistics.factoryBoard.PanelSlot;
import com.zurrtum.create.content.logistics.factoryBoard.ServerFactoryPanelBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.Arrays;

public class FactoryPanelModel extends PositionAwareBakedModel {
    public FactoryPanelModel(BakedModel model) {
        super(model);
    }

    @Override
    protected void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, SimpleQuadBakedModel.Builder builder) {
        copyPlainQuads(model, state, random, builder);
        boolean ponder = world instanceof PonderLevel;
        for (PanelSlot slot : PanelSlot.values()) {
            ServerFactoryPanelBehaviour behaviour = ServerFactoryPanelBehaviour.at(world, new FactoryPanelPosition(pos, slot));
            if (behaviour == null)
                continue;
            addPanel(builder, state, slot, behaviour, ponder);
        }
    }

    public void addPanel(SimpleQuadBakedModel.Builder builder, BlockState state, PanelSlot slot, ServerFactoryPanelBehaviour behaviour, boolean ponder) {
        PartialModel factoryPanel;
        if (behaviour.panelBE().restocker) {
            factoryPanel = behaviour.count == 0 ? AllPartialModels.FACTORY_PANEL_RESTOCKER : AllPartialModels.FACTORY_PANEL_RESTOCKER_WITH_BULB;
        } else {
            factoryPanel = behaviour.count == 0 ? AllPartialModels.FACTORY_PANEL : AllPartialModels.FACTORY_PANEL_WITH_BULB;
        }

        float xRot = MathHelper.DEGREES_PER_RADIAN * FactoryPanelBlock.getXRot(state);
        float yRot = MathHelper.DEGREES_PER_RADIAN * FactoryPanelBlock.getYRot(state);

        SimpleQuadBakedModel model = factoryPanel.get();
        for (BakedQuad bakedQuad : model.getAllQuads()) {
            int[] vertices = bakedQuad.getVertexData();
            int[] transformedVertices = Arrays.copyOf(vertices, vertices.length);

            Vec3d quadNormal = Vec3d.of(bakedQuad.getFace().getVector());
            quadNormal = VecHelper.rotate(quadNormal, 180, Axis.Y);
            quadNormal = VecHelper.rotate(quadNormal, xRot + 90, Axis.X);
            quadNormal = VecHelper.rotate(quadNormal, yRot, Axis.Y);

            for (int i = 0; i < vertices.length / BakedQuadHelper.VERTEX_STRIDE; i++) {
                Vec3d vertex = BakedQuadHelper.getXYZ(vertices, i);

                vertex = vertex.add(slot.xOffset * .5, 0, slot.yOffset * .5);
                vertex = VecHelper.rotateCentered(vertex, 180, Axis.Y);
                vertex = VecHelper.rotateCentered(vertex, xRot + 90, Axis.X);
                vertex = VecHelper.rotateCentered(vertex, yRot, Axis.Y);

                BakedQuadHelper.setXYZ(transformedVertices, i, vertex);
                BakedQuadHelper.setNormalXYZ(transformedVertices, i, new Vec3d(0, 1, 0));
            }

            Direction newNormal = Direction.fromVector(
                (int) Math.round(quadNormal.x),
                (int) Math.round(quadNormal.y),
                (int) Math.round(quadNormal.z)
            );
            BakedQuad quad = new BakedQuad(
                transformedVertices,
                bakedQuad.getColorIndex(),
                newNormal,
                bakedQuad.getSprite(),
                !ponder && bakedQuad.hasShade()
            );
            NormalsBakedQuad.markNormals(quad);
            builder.add(quad);
        }
    }
}

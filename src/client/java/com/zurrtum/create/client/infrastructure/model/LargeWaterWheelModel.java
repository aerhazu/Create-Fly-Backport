package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.content.kinetics.waterwheel.LargeWaterWheelBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public class LargeWaterWheelModel extends EmptyModel {
    public LargeWaterWheelModel(BakedModel model) {
        super(model);
    }

    @Override
    public Sprite particleSpriteWithInfo(BlockRenderView world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof LargeWaterWheelBlockEntity blockEntity) {
            return MinecraftClient.getInstance().getBlockRenderManager().getModel(blockEntity.material).getParticleSprite();
        } else {
            return model.getParticleSprite();
        }
    }
}

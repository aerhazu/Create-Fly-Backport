package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.content.kinetics.waterwheel.LargeWaterWheelBlockEntity;
import com.zurrtum.create.content.kinetics.waterwheel.WaterWheelStructuralBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public class WaterWheelStructuralModel extends EmptyModel {
    public WaterWheelStructuralModel(BakedModel model) {
        super(model);
    }

    public static WaterWheelStructuralModel single(BakedModel model) {
        return new WaterWheelStructuralModel(model);
    }

    @Override
    public Sprite particleSpriteWithInfo(BlockRenderView world, BlockPos pos, BlockState state) {
        BlockPos master = WaterWheelStructuralBlock.getMaster(world, pos, state);
        if (world.getBlockEntity(master) instanceof LargeWaterWheelBlockEntity blockEntity) {
            return MinecraftClient.getInstance().getBlockRenderManager().getModel(blockEntity.material).getParticleSprite();
        }
        return getParticleSprite();
    }
}

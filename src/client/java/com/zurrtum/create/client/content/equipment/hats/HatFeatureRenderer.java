package com.zurrtum.create.client.content.equipment.hats;

import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.trains.schedule.hat.TrainHatInfo;
import com.zurrtum.create.client.content.trains.schedule.hat.TrainHatInfoReloadListener;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.contraptions.actors.seat.SeatEntity;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlock;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class HatFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
    public HatFeatureRenderer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Override
    public void render(
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        T entity,
        float limbAngle,
        float limbDistance,
        float tickDelta,
        float animationProgress,
        float headYaw,
        float headPitch
    ) {
        PartialModel hat = getHat(entity);
        if (hat == null)
            return;

        TrainHatInfo info = TrainHatInfoReloadListener.getHatInfoFor(entity);

        M entityModel = getContextModel();
        ms.push();

        var msr = TransformStack.of(ms);
        List<ModelPart> partsToHead;
        if (entityModel instanceof ModelWithHead model) {
            partsToHead = TrainHatInfo.getAdjustedPart(info, model.getHead(), "");
        } else {
            ms.pop();
            return;
        }

        if (!partsToHead.isEmpty()) {
            partsToHead.forEach(part -> part.rotate(ms));

            ModelPart lastChild = partsToHead.get(partsToHead.size() - 1);
            if (!lastChild.isEmpty()) {
                ModelPart.Cuboid cube = lastChild.cuboids.get(MathHelper.clamp(info.cubeIndex(), 0, lastChild.cuboids.size() - 1));
                ms.translate(info.offset().getX() / 16.0F, (cube.minY - cube.maxY + info.offset().getY()) / 16.0F, info.offset().getZ() / 16.0F);
                float max = Math.max(cube.maxX - cube.minX, cube.maxZ - cube.minZ) / 8.0F * info.scale();
                ms.scale(max, max, max);
            }

            ms.scale(1, -1, -1);
            ms.translate(0, -2.25F / 16.0F, 0);
            msr.rotateXDegrees(-8.5F);
            BlockState air = Blocks.AIR.getDefaultState();
            CachedBuffers.partial(hat, air).disableDiffuse().light(light).renderInto(ms, buffer.getBuffer(TexturedRenderLayers.getEntityCutout()));
        }

        ms.pop();
    }

    private static PartialModel getHat(LivingEntity entity) {
        if (entity.hasVehicle()) {
            ItemStack stack = entity.getEquippedStack(EquipmentSlot.HEAD);
            Entity vehicle = entity.getVehicle();
            if (stack.isEmpty() && vehicle instanceof CarriageContraptionEntity cce && (cce.hasSchedule() || entity instanceof PlayerEntity) && cce.getContraption() instanceof CarriageContraption cc) {
                BlockPos seatOf = cc.getSeatOf(entity.getUuid());
                if (seatOf != null && cc.conductorSeats.get(seatOf) != null) {
                    return AllPartialModels.TRAIN_HAT;
                }
            }
            if (vehicle instanceof SeatEntity) {
                World level = entity.getWorld();
                BlockPos pos = entity.getBlockPos();
                boolean find = false;
                Find:
                for (Direction d : Iterate.horizontalDirections) {
                    for (int y : Iterate.zeroAndOne) {
                        if (!(level.getBlockState(pos.offset(d).up(y)).getBlock() instanceof StockTickerBlock))
                            continue;
                        if (find) {
                            find = false;
                            break Find;
                        }
                        find = true;
                    }
                }
                if (find) {
                    return AllPartialModels.LOGISTICS_HAT;
                }
            }
        } else if (entity instanceof ParrotEntity parrot && entity.getEquippedStack(EquipmentSlot.HEAD)
            .isEmpty() && AllSynchedDatas.PARROT_TRAIN_HAT.get(parrot)) {
            return AllPartialModels.TRAIN_HAT;
        }
        return null;
    }
}

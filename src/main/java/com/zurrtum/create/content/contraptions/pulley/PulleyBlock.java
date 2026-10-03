package com.zurrtum.create.content.contraptions.pulley;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class PulleyBlock extends HorizontalAxisKineticBlock implements IBE<PulleyBlockEntity> {

    public PulleyBlock(Settings properties) {
        super(properties);
    }

    private static void onRopeBroken(World world, BlockPos pulleyPos) {
        BlockEntity be = world.getBlockEntity(pulleyPos);
        if (be instanceof PulleyBlockEntity pulley) {
            pulley.initialOffset = 0;
            pulley.onLengthBroken();
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onStateReplaced(state, worldIn, pos, newState, isMoving);
        if (worldIn.isClient)
            return;
        BlockState below = worldIn.getBlockState(pos.down());
        if (below.getBlock() instanceof RopeBlockBase)
            worldIn.breakBlock(pos.down(), true);
    }

    @Override
    protected ItemActionResult onUseWithItem(
        ItemStack stack,
        BlockState state,
        World level,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hitResult
    ) {
        if (!player.canModifyBlocks())
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (player.isSneaking())
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (stack.isEmpty()) {
            withBlockEntityDo(level, pos, be -> be.assembleNextTick = true);
            return ItemActionResult.SUCCESS;
        }
        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public Class<PulleyBlockEntity> getBlockEntityClass() {
        return PulleyBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PulleyBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.ROPE_PULLEY;
    }

    private static class RopeBlockBase extends Block implements Waterloggable {

        public RopeBlockBase(Settings properties) {
            super(properties);
            setDefaultState(getDefaultState().with(Properties.WATERLOGGED, false));
        }

        @Override
        protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
            return false;
        }

        @Override
        public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
            return AllItems.ROPE_PULLEY.getDefaultStack();
        }

        @Override
        public void onStateReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
            if (isMoving) {
                return;
            }
            boolean onBroken = !state.contains(Properties.WATERLOGGED);
            if (!onBroken) {
                BlockState currentState = worldIn.getBlockState(pos);
                onBroken = !currentState.contains(Properties.WATERLOGGED) || state.get(Properties.WATERLOGGED) == currentState.get(Properties.WATERLOGGED);
            }
            if (onBroken) {
                onRopeBroken(worldIn, pos.up());
                if (!worldIn.isClient) {
                    BlockState above = worldIn.getBlockState(pos.up());
                    BlockState below = worldIn.getBlockState(pos.down());
                    if (above.getBlock() instanceof RopeBlockBase)
                        worldIn.breakBlock(pos.up(), true);
                    if (below.getBlock() instanceof RopeBlockBase)
                        worldIn.breakBlock(pos.down(), true);
                }
            }
        }


        @Override
        public FluidState getFluidState(BlockState state) {
            return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : Fluids.EMPTY.getDefaultState();
        }

        @Override
        protected void appendProperties(Builder<Block, BlockState> builder) {
            builder.add(Properties.WATERLOGGED);
            super.appendProperties(builder);
        }

        @Override
        public BlockState getStateForNeighborUpdate(
        BlockState state,
        Direction direction,
        BlockState neighbourState,
        WorldAccess world,
        BlockPos pos,
        BlockPos neighbourPos
    ) {
            if (state.get(Properties.WATERLOGGED))
                world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
            return state;
        }

        @Override
        public BlockState getPlacementState(ItemPlacementContext context) {
            FluidState FluidState = context.getWorld().getFluidState(context.getBlockPos());
            return super.getPlacementState(context).with(Properties.WATERLOGGED, Boolean.valueOf(FluidState.getFluid() == Fluids.WATER));
        }

    }

    public static class MagnetBlock extends RopeBlockBase {

        public MagnetBlock(Settings properties) {
            super(properties);
        }

        @Override
        public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
            return AllShapes.PULLEY_MAGNET;
        }

    }

    public static class RopeBlock extends RopeBlockBase {

        public RopeBlock(Settings properties) {
            super(properties);
        }

        @Override
        public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
            return AllShapes.FOUR_VOXEL_POLE.get(Direction.UP);
        }
    }

}

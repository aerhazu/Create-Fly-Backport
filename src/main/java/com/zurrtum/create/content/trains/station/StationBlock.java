package com.zurrtum.create.content.trains.station;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.logistics.depot.SharedDepotBlockMethods;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class StationBlock extends Block implements IBE<StationBlockEntity>, ItemInventoryProvider<StationBlockEntity>, IWrenchable, ProperWaterloggedBlock {

    public static final BooleanProperty ASSEMBLING = BooleanProperty.of("assembling");

    public StationBlock(Settings p_54120_) {
        super(p_54120_);
        setDefaultState(getDefaultState().with(ASSEMBLING, false).with(WATERLOGGED, false));
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, StationBlockEntity blockEntity, Direction context) {
        return blockEntity.depotBehaviour.itemHandler;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder.add(ASSEMBLING, WATERLOGGED));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        return withWater(super.getPlacementState(pContext), pContext);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
        BlockState pState,
        Direction pDirection,
        BlockState pNeighborState,
        WorldAccess pLevel,
        BlockPos pCurrentPos,
        BlockPos pNeighborPos
    ) {
        updateWater(pLevel, pState, pCurrentPos);
        return pState;
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.onPlaced(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public boolean hasComparatorOutput(BlockState pState) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState pState, World pLevel, BlockPos pPos) {
        return getBlockEntityOptional(pLevel, pPos).map(ste -> ste.trainPresent ? 15 : 0).orElse(0);
    }

    @Override
    public void onEntityLand(BlockView worldIn, Entity entityIn) {
        super.onEntityLand(worldIn, entityIn);
        SharedDepotBlockMethods.onLanded(worldIn, entityIn);
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
        if (player == null || player.isSneaking())
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (stack.isOf(AllItems.WRENCH))
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (stack.getItem() == Items.FILLED_MAP) {
            return onBlockEntityUseItemOn(
                level, pos, station -> {
                    if (level.isClient)
                        return ItemActionResult.SUCCESS;

                    if (station.getStation() == null || station.getStation().getId() == null)
                        return ItemActionResult.FAIL;

                    MapState savedData = FilledMapItem.getMapState(stack, level);
                    if (!(savedData instanceof StationMapData stationMapData))
                        return ItemActionResult.FAIL;

                    if (!stationMapData.create$toggleStation(level, pos, station))
                        return ItemActionResult.FAIL;

                    return ItemActionResult.SUCCESS;
                }
            );
        }

        ActionResult result = onBlockEntityUse(
            level, pos, station -> {
                ItemStack autoSchedule = station.getAutoSchedule();
                if (autoSchedule.isEmpty())
                    return ActionResult.PASS;
                if (level.isClient)
                    return ActionResult.SUCCESS;
                player.getInventory().offerOrDrop(autoSchedule.copy());
                station.depotBehaviour.removeHeldItem();
                station.notifyUpdate();
                player.getWorld().playSound(
                    null,
                    player.getBlockPos(),
                    SoundEvents.ENTITY_ITEM_PICKUP,
                    SoundCategory.PLAYERS,
                    .2f,
                    1f + player.getWorld().random.nextFloat()
                );
                return ActionResult.SUCCESS;
            }
        );

        if (result == ActionResult.PASS)
            AllClientHandle.INSTANCE.openStationScreen(level, pos, player);
        return ItemActionResult.SUCCESS;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos, ShapeContext pContext) {
        return AllShapes.STATION;
    }

    @Override
    public Class<StationBlockEntity> getBlockEntityClass() {
        return StationBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends StationBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.TRACK_STATION;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType pathComputationType) {
        return false;
    }

}

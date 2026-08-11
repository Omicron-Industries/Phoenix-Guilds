package net.phoenixvine.guilds.content.flag;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenixvine.guilds.GuildAPI;
import net.phoenixvine.guilds.client.GuildFlagEditorScreen;
import net.phoenixvine.guilds.data.Guild;
import net.phoenixvine.guilds.data.GuildManager;
import net.phoenixvine.guilds.event.GuildEvents;

import java.util.Optional;
import java.util.UUID;

public class GuildFlagBlock extends Block implements EntityBlock {

    public static final net.minecraft.world.level.block.state.properties.EnumProperty<DoubleBlockHalf> HALF = net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF;

    private static final VoxelShape SHAPE_LOWER = Block.box(7, 0, 7, 9, 16, 9);
    private static final VoxelShape SHAPE_UPPER = Block.box(7, 0, 7, 9, 16, 9);

    public GuildFlagBlock() {
        super(Block.Properties.of().mapColor(MapColor.WOOD).strength(1.0f).noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? SHAPE_LOWER : SHAPE_UPPER;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {

        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
        }
        BlockState belowState = level.getBlockState(below);
        return belowState.is(this) && belowState.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
                                  BlockPos currentPos, BlockPos facingPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (facing.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (facing == Direction.UP)) {
            return facingState.is(this) && facingState.getValue(HALF) != half ? state : Blocks.AIR.defaultBlockState();
        }
        return half == DoubleBlockHalf.LOWER && facing == Direction.DOWN && !state.canSurvive(level, currentPos) ?
                Blocks.AIR.defaultBlockState() :
                super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new GuildFlagBlockEntity(pos, state) : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
        if (level.isClientSide() || !(placer instanceof Player player)) return;
        if (level.getBlockEntity(pos) instanceof GuildFlagBlockEntity entity) {
            entity.setGuildId(GuildAPI.getGuildIdOrPlayerFallback(player.getUUID()));

            entity.setFacing(player.getDirection());
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        if (!(level.getBlockEntity(lowerPos) instanceof GuildFlagBlockEntity entity) || entity.getGuildId() == null) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        ItemStack held = player.getItemInHand(hand);

        if (!held.isEmpty() && held.is(this.asItem())) {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        Component.literal("§cYou cannot put a flag on a flag! Flag-ception averted."), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (held.isEmpty()) {
            if (level.isClientSide()) {

                net.phoenixvine.guilds.client.ClientAccess.openFlagEditor(entity.getGuildId());
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            applyHeldItemAsFlag(serverPlayer, entity.getGuildId(), held);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void applyHeldItemAsFlag(ServerPlayer player, UUID guildId, ItemStack held) {
        GuildManager mgr = GuildManager.get(player.getServer().overworld());
        Optional<Guild> guildOpt = mgr.getGuildById(guildId);
        if (guildOpt.isEmpty()) {
            player.displayClientMessage(Component.literal("§cThat flag's guild no longer exists."), true);
            return;
        }
        Guild guild = guildOpt.get();
        if (!guild.isMember(player.getUUID())) {
            player.displayClientMessage(Component.literal("§cYou're not a member of this flag's guild."), true);
            return;
        }

        Item heldItem = held.getItem();
        String iconId = heldItem instanceof net.minecraft.world.item.BlockItem blockItem &&
                blockItem.getBlock() != Blocks.AIR ?
                        "block:" + ForgeRegistries.BLOCKS.getKey(blockItem.getBlock()) :
                        "item:" + ForgeRegistries.ITEMS.getKey(heldItem);
        GuildEvents.handleSetFlag(player, mgr, guildId, false, iconId, null, guild.getFlagWidth(),
                guild.getFlagHeight());
    }
}

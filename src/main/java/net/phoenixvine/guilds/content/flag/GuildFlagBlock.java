package net.phoenixvine.guilds.content.flag;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

/**
 * A physical, right-clickable representation of a guild's flag — two blocks tall, like a banner,
 * using the same {@code DoubleBlockHalf} LOWER/UPPER structure vanilla's {@code DoorBlock} uses
 * (verified against decompiled {@code DoorBlock.java}: {@code setPlacedBy} places the UPPER half
 * automatically, {@code updateShape} turns either half to air the instant the other half stops
 * matching — the same auto-propagating break behavior, reused directly rather than reinvented).
 * The {@link GuildFlagBlockEntity} — and so the guild-ownership data — lives only on the LOWER
 * half; {@code use()} resolves up through {@link #HALF} to find it regardless of which half was
 * actually clicked.
 * <p>
 * Two interactions (verified against decompiled {@code BlockBehaviour.java} for the real 1.20.1
 * method shape — this version has a single {@code use()}, not the later {@code
 * useWithoutItem()} split):
 * <ul>
 * <li>Holding an item — instantly applies it as the flag's new icon (vanilla-banner-style, no
 * GUI), via the exact same {@link GuildEvents#handleSetFlag} path the editor screen's Save button
 * uses.</li>
 * <li>Empty hand — opens {@link GuildFlagEditorScreen} client-side. Runs on both sides (standard
 * vanilla {@code use()} pattern) — the client branch opens the screen locally; the server branch
 * is a no-op beyond returning a consumed result.</li>
 * </ul>
 */
public class GuildFlagBlock extends Block implements EntityBlock {

    public static final net.minecraft.world.level.block.state.properties.EnumProperty<DoubleBlockHalf> HALF = net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF;

    // Plain 2x2-thick post, matching the simplified visual model exactly — the old shape had an
    // extra wide "foot plate" box for a decorative base the model no longer has (removed for
    // being stretched/blurry at its actual texture resolution; see guild_flag.json's own credit
    // line), which would otherwise leave a collision box wider than anything actually rendered.
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
        // The real model (guild_flag.json — a plain post, see its own credit line for why) spans
        // the whole two-block height in one continuous mesh — y=0..32 in model space — anchored
        // at the LOWER half's own origin, bleeding upward into the UPPER half's space the way
        // Minecraft always lets block geometry extend past its own cell. UPPER only exists for
        // collision/placement bookkeeping (see the class doc), so it renders nothing itself —
        // using the model there too would draw the whole post a second time, offset up by
        // another full block.
        // The flag's own dynamic per-guild icon/drawing is drawn separately in world-space by
        // GuildFlagBlockEntityRenderer (fixed RENDER_Y/RENDER_Z constants there, independent of
        // this model) — the static model itself has no geometry at that location at all anymore;
        // an earlier version had a placeholder "flagface" element there, but with the dynamic
        // renderer now reliably drawing every frame, that placeholder was just an extra thing to
        // texture (and the one that carried a checkerboard baked directly into the source PNG).
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

    /** Mirrors {@code DoorBlock}'s own break-propagation: a half whose partner no longer matches turns to air. */
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
        // Guild ownership data lives on the LOWER half only — the UPPER half is purely visual/shape.
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new GuildFlagBlockEntity(pos, state) : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
        if (level.isClientSide() || !(placer instanceof Player player)) return;
        if (level.getBlockEntity(pos) instanceof GuildFlagBlockEntity entity) {
            entity.setGuildId(GuildAPI.getGuildIdOrPlayerFallback(player.getUUID()));
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
        if (held.isEmpty()) {
            if (level.isClientSide()) {
                // By calling a separate class, the server's JVM never
                // attempts to resolve the Minecraft class.
                net.phoenixvine.guilds.client.ClientAccess.openFlagEditor();
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            applyHeldItemAsFlag(serverPlayer, entity.getGuildId(), held);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /**
     * {@link GuildEvents#handleSetFlag} resolves its target guild via the acting player's own
     * current guild ({@code mgr.getGuildFor(player)}), matching how the editor screen's Save
     * button always edits "my own guild's flag" — that's the wrong lookup here, since the block
     * being clicked has its own fixed {@code guildId} that may differ from whichever guild the
     * player happens to be in right now (or none at all). Checking membership in {@code guildId}
     * specifically first, before delegating into {@code handleSetFlag}, keeps that method's
     * simpler "my own guild" contract intact while still preventing a player from retexturing a
     * flag that isn't their own guild's.
     */
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
        String iconId = "item:" + ForgeRegistries.ITEMS.getKey(held.getItem());
        GuildEvents.handleSetFlag(player, mgr, false, iconId, null, guild.getFlagWidth(), guild.getFlagHeight());
    }
}

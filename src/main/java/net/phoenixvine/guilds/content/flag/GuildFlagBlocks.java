package net.phoenixvine.guilds.content.flag;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.phoenixvine.guilds.PhoenixGuilds;

public final class GuildFlagBlocks {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PhoenixGuilds.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PhoenixGuilds.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister
            .create(Registries.BLOCK_ENTITY_TYPE, PhoenixGuilds.MOD_ID);

    public static final DeferredBlock<Block> GUILD_FLAG = BLOCKS.register("guild_flag", GuildFlagBlock::new);
    public static final DeferredItem<Item> GUILD_FLAG_ITEM = ITEMS.register("guild_flag",
            () -> new GuildFlagBlockItem(GUILD_FLAG.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GuildFlagBlockEntity>> GUILD_FLAG_ENTITY = BLOCK_ENTITY_TYPES
            .register("guild_flag",
                    () -> BlockEntityType.Builder.of(GuildFlagBlockEntity::new, GUILD_FLAG.get()).build(null));

    private GuildFlagBlocks() {}

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
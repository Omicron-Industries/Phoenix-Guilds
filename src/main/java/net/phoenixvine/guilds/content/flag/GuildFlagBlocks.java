package net.phoenixvine.guilds.content.flag;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.phoenixvine.guilds.PhoenixGuilds;

/**
 * The first block/item/{@code BlockEntityType} Phoenix-Guilds has ever registered — standard
 * Forge {@code DeferredRegister} boilerplate, since nothing in this mod (or any sibling mod in
 * this family) had a reason to do this before the flag block. Wired onto the mod event bus from
 * {@link PhoenixGuilds}'s constructor, same as every other Forge mod's block/item registration.
 */
public final class GuildFlagBlocks {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            PhoenixGuilds.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            PhoenixGuilds.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITY_TYPES, PhoenixGuilds.MOD_ID);

    public static final RegistryObject<Block> GUILD_FLAG = BLOCKS.register("guild_flag", GuildFlagBlock::new);
    public static final RegistryObject<Item> GUILD_FLAG_ITEM = ITEMS.register("guild_flag",
            () -> new BlockItem(GUILD_FLAG.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<GuildFlagBlockEntity>> GUILD_FLAG_ENTITY = BLOCK_ENTITY_TYPES
            .register("guild_flag",
                    () -> BlockEntityType.Builder.of(GuildFlagBlockEntity::new, GUILD_FLAG.get()).build(null));

    private GuildFlagBlocks() {}

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}

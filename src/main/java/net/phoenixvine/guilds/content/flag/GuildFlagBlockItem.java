package net.phoenixvine.guilds.content.flag;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class GuildFlagBlockItem extends BlockItem {

    public GuildFlagBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.phoenix_guilds.guild_flag.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(
                Component.translatable("item.phoenix_guilds.guild_flag.credit").withStyle(ChatFormatting.DARK_GRAY));
    }
}
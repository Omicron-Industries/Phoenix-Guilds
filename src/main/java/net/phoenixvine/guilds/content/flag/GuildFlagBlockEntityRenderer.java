package net.phoenixvine.guilds.content.flag;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.phoenixvine.guilds.client.ClientGuildFlagCache;
import net.phoenixvine.guilds.client.GuildFlagContentRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GuildFlagBlockEntityRenderer implements BlockEntityRenderer<GuildFlagBlockEntity> {

    public GuildFlagBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(GuildFlagBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        UUID guildId = blockEntity.getGuildId();
        if (guildId == null) return;

        ClientGuildFlagCache.FlagData flag = ClientGuildFlagCache.getOrRequest(guildId);
        if (flag == null) return;

        GuildFlagContentRenderer.render(guildId, flag.useDrawing(), flag.iconId(), flag.pixelData(), flag.width(),
                flag.height(), blockEntity.getFacing(), poseStack, buffer, packedLight, packedOverlay,
                blockEntity.getLevel());
    }
    @Override
    public @NotNull AABB getRenderBoundingBox(GuildFlagBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).expandTowards(0, 1, -1);
    }
}

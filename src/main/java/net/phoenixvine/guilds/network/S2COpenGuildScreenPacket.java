package net.phoenixvine.guilds.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.phoenixvine.guilds.client.ClientPacketHandler;

import java.util.function.Supplier;

public class S2COpenGuildScreenPacket {

    public S2COpenGuildScreenPacket() {}

    public S2COpenGuildScreenPacket(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandler.openGuildScreen()));
        ctx.get().setPacketHandled(true);
    }
}

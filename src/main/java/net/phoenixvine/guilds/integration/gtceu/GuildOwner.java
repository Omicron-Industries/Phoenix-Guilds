package net.phoenixvine.guilds.integration.gtceu;

import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.common.machine.owner.PlayerOwner;

import net.minecraft.network.chat.Component;
import net.phoenixvine.guilds.GuildAPI;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class GuildOwner extends PlayerOwner {

    private static final Component DISPLAY_NAME = Component.translatable("gtceu.ownership.name.phoenix_guilds");

    public GuildOwner(UUID playerUUID) {
        super(playerUUID);
    }

    @UnmodifiableView
    @Override
    public @NotNull Set<UUID> getMembers() {
        Optional<UUID> guildId = GuildAPI.getGuildId(playerUUID);
        return guildId.map(GuildAPI::getGuildMembersById).orElseGet(super::getMembers);
    }

    @Override
    public boolean isPlayerInTeam(UUID other) {
        return GuildAPI.areGuildmates(playerUUID, other) || super.isPlayerInTeam(other);
    }

    @Override
    public boolean isPlayerFriendly(UUID other) {
        return GuildAPI.areGuildmates(playerUUID, other) || GuildAPI.areAllied(playerUUID, other) ||
                super.isPlayerFriendly(other);
    }

    @Override
    public UUID getUUID() {
        return GuildAPI.getGuildId(playerUUID).orElseGet(super::getUUID);
    }

    @Override
    public String getName() {
        return GuildAPI.getGuildId(playerUUID).flatMap(GuildAPI::getGuildNameById).orElseGet(super::getName);
    }

    @Override
    public Component getTypeDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof GuildOwner && super.equals(object);
    }
}

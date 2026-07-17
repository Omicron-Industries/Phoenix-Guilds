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

/**
 * {@link MachineOwner} type backed by Guilds membership. Extends {@link PlayerOwner} — GTCEu's
 * own default owner type — rather than {@code MachineOwner} directly: {@code MachineOwner} is
 * {@code sealed permits PlayerOwner, FTBOwner, ArgonautsOwner}, so a genuinely external class
 * cannot extend it, but {@code PlayerOwner} is deliberately declared {@code non-sealed} for
 * exactly this kind of further subclassing. Every override here falls back to {@code
 * PlayerOwner}'s own individual-player behavior when the machine's owner isn't currently in a
 * guild, so an ungildeded player's machines behave identically to vanilla GTCEu — this class only
 * ever adds team access, never removes the individual-owner baseline. Mirrors {@code FTBOwner}'s
 * own shape (team-name/team-id-when-teamed, individual-identity-otherwise) for consistency with
 * how GTCEu's other real team integrations already present themselves.
 */
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

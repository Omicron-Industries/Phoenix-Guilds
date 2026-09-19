package net.phoenixvine.guilds.utils;

import net.phoenixvine.guilds.data.Guild;

@FunctionalInterface
public interface GuildContextAction {

    void execute(Guild guild);
}

package net.phoenixvine.guilds.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GuildRankTest {

    @Test
    void orderingIsCorrect() {
        assertTrue(GuildRank.OWNER.isAtLeast(GuildRank.OWNER));
        assertTrue(GuildRank.OWNER.isAtLeast(GuildRank.OFFICER));
        assertTrue(GuildRank.OWNER.isAtLeast(GuildRank.MEMBER));

        assertTrue(GuildRank.OFFICER.isAtLeast(GuildRank.OFFICER));
        assertTrue(GuildRank.OFFICER.isAtLeast(GuildRank.MEMBER));
        assertFalse(GuildRank.OFFICER.isAtLeast(GuildRank.OWNER));

        assertFalse(GuildRank.MEMBER.isAtLeast(GuildRank.OFFICER));
        assertFalse(GuildRank.MEMBER.isAtLeast(GuildRank.OWNER));
        assertTrue(GuildRank.MEMBER.isAtLeast(GuildRank.MEMBER));
    }

    @Test
    void displayAndLabelAreNonNull() {
        for (GuildRank rank : GuildRank.values()) {
            assertNotNull(rank.display());
            assertNotNull(rank.label());
            assertFalse(rank.display().isBlank());
            assertFalse(rank.label().isBlank());
        }
    }
}

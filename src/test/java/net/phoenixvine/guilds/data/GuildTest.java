package net.phoenixvine.guilds.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GuildTest {

    static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    static final UUID MEMBER_A  = UUID.fromString("00000000-0000-0000-0000-000000000002");
    static final UUID MEMBER_B  = UUID.fromString("00000000-0000-0000-0000-000000000003");
    static final UUID OUTSIDER  = UUID.fromString("00000000-0000-0000-0000-000000000099");
    static final UUID GUILD_ID  = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    Guild guild;

    @BeforeEach
    void setUp() {
        guild = new Guild(GUILD_ID, "TestGuild", OWNER_ID);
    }

    @Test
    void ownerIsAutoMember() {
        assertTrue(guild.isMember(OWNER_ID));
    }

    @Test
    void ownerRankIsOwner() {
        assertEquals(GuildRank.OWNER, guild.getRank(OWNER_ID));
    }

    @Test
    void outsiderIsNotMember() {
        assertFalse(guild.isMember(OUTSIDER));
    }

    @Test
    void initialFlagDataIsCorrectLength() {
        assertEquals(Guild.FLAG_PIXEL_DATA_LENGTH, guild.getFlagPixelData().length());
    }

    @Test
    void initialFlagSizeIsDefault() {
        assertEquals(Guild.DEFAULT_FLAG_SIZE, guild.getFlagWidth());
        assertEquals(Guild.DEFAULT_FLAG_SIZE, guild.getFlagHeight());
    }

    @Nested
    class Members {

        @Test
        void addAndContains() {
            guild.addMember(MEMBER_A);
            assertTrue(guild.isMember(MEMBER_A));
            assertEquals(GuildRank.MEMBER, guild.getRank(MEMBER_A));
        }

        @Test
        void removeMember() {
            guild.addMember(MEMBER_A);
            guild.removeMember(MEMBER_A);
            assertFalse(guild.isMember(MEMBER_A));
        }

        @Test
        void fullAt30() {
            for (int i = 0; i < Guild.MAX_MEMBERS - 1; i++) {
                guild.addMember(UUID.randomUUID());
            }
            assertTrue(guild.isFull());
        }

        @Test
        void notFullBeforeLimit() {
            guild.addMember(MEMBER_A);
            assertFalse(guild.isFull());
        }

        @Test
        void addMemberDoesNotDowngradeExistingOfficer() {
            guild.addMember(MEMBER_A);
            guild.getMemberRanks().put(MEMBER_A, GuildRank.OFFICER);
            guild.addMember(MEMBER_A); 
            assertEquals(GuildRank.OFFICER, guild.getRank(MEMBER_A));
        }
    }

    @Nested
    class Ranks {

        @Test
        void hasRankChecksAtLeast() {
            guild.addMember(MEMBER_A);
            assertTrue(guild.hasRank(OWNER_ID, GuildRank.OFFICER));
            assertFalse(guild.hasRank(MEMBER_A, GuildRank.OFFICER));
        }

        @Test
        void setOwnerDemotesPreviousOwnerToOfficer() {
            guild.addMember(MEMBER_A);
            guild.setOwner(MEMBER_A);
            assertEquals(GuildRank.OWNER, guild.getRank(MEMBER_A));
            assertEquals(GuildRank.OFFICER, guild.getRank(OWNER_ID));
        }

        @Test
        void unknownPlayerDefaultsToMember() {
            assertEquals(GuildRank.MEMBER, guild.getRank(OUTSIDER));
        }
    }

    @Nested
    class Allies {

        final UUID OTHER_GUILD = UUID.randomUUID();

        @Test
        void addAndCheck() {
            guild.addAlly(OTHER_GUILD);
            assertTrue(guild.isAlly(OTHER_GUILD));
        }

        @Test
        void remove() {
            guild.addAlly(OTHER_GUILD);
            guild.removeAlly(OTHER_GUILD);
            assertFalse(guild.isAlly(OTHER_GUILD));
        }

        @Test
        void pendingOutgoing() {
            guild.addPendingOutgoing(OTHER_GUILD);
            assertTrue(guild.hasPendingOutgoing(OTHER_GUILD));
            guild.removePendingOutgoing(OTHER_GUILD);
            assertFalse(guild.hasPendingOutgoing(OTHER_GUILD));
        }
    }

    @Nested
    class Flag {

        @Test
        void setFlagStoresValues() {
            String pixelData = "abcdef".repeat(Guild.MAX_FLAG_SIZE * Guild.MAX_FLAG_SIZE);
            guild.setFlag(true, "block:minecraft:stone", pixelData, 32, 32);
            assertTrue(guild.isFlagUseDrawing());
            assertEquals("block:minecraft:stone", guild.getFlagIconId());
            assertEquals(pixelData.toLowerCase(), guild.getFlagPixelData());
            assertEquals(32, guild.getFlagWidth());
            assertEquals(32, guild.getFlagHeight());
        }

        @Test
        void setFlagClampsWidth() {
            guild.setFlag(false, "", validPixelData(), -1, 999);
            assertEquals(Guild.MIN_FLAG_SIZE, guild.getFlagWidth());
            assertEquals(Guild.MAX_FLAG_SIZE, guild.getFlagHeight());
        }

        @Test
        void setFlagRejectsWrongLengthPixelData() {
            String original = guild.getFlagPixelData();
            guild.setFlag(true, null, "tooshort", 16, 16);
            assertEquals(original, guild.getFlagPixelData());
        }

        @Test
        void setFlagNullIconIdIsIgnored() {
            guild.setFlag(false, "block:minecraft:grass_block", validPixelData(), 16, 16);
            guild.setFlag(false, null, validPixelData(), 16, 16);
            assertEquals("block:minecraft:grass_block", guild.getFlagIconId());
        }

        @Test
        void pixelDataIsLowercased() {
            String upper = "ABCDEF".repeat(Guild.MAX_FLAG_SIZE * Guild.MAX_FLAG_SIZE);
            guild.setFlag(false, null, upper, 16, 16);
            assertEquals(upper.toLowerCase(), guild.getFlagPixelData());
        }

        private String validPixelData() {
            return "ffffff".repeat(Guild.MAX_FLAG_SIZE * Guild.MAX_FLAG_SIZE);
        }
    }

    @Nested
    class Log {

        @Test
        void logsAreStoredMostRecentFirst() {
            guild.addLog("first");
            guild.addLog("second");
            assertEquals("second", guild.getLog().peekFirst().message());
        }

        @Test
        void logCapsAt25() {
            for (int i = 0; i < 30; i++) guild.addLog("msg" + i);
            assertEquals(25, guild.getLog().size());
        }

        @Test
        void oldestEntryDroppedWhenFull() {
            guild.addLog("oldest");
            for (int i = 0; i < 25; i++) guild.addLog("filler" + i);
            boolean found = guild.getLog().stream().anyMatch(e -> e.message().equals("oldest"));
            assertFalse(found);
        }
    }

    @Nested
    class Wiki {

        @Test
        void setAndGet() {
            assertTrue(guild.setWikiPage("Intro", "Welcome!"));
            assertEquals("Welcome!", guild.getWikiPages().get("Intro"));
        }

        @Test
        void updateExistingPage() {
            guild.setWikiPage("Intro", "v1");
            guild.setWikiPage("Intro", "v2");
            assertEquals("v2", guild.getWikiPages().get("Intro"));
        }

        @Test
        void deletePage() {
            guild.setWikiPage("Intro", "content");
            assertTrue(guild.deleteWikiPage("Intro"));
            assertFalse(guild.getWikiPages().containsKey("Intro"));
        }

        @Test
        void deleteMissingPageReturnsFalse() {
            assertFalse(guild.deleteWikiPage("nonexistent"));
        }

        @Test
        void wikiFullAt20() {
            for (int i = 0; i < 20; i++) guild.setWikiPage("page" + i, "content");
            assertTrue(guild.wikiFull());
            assertFalse(guild.setWikiPage("overflow", "blocked"));
        }

        @Test
        void updatePageWhenFullSucceeds() {
            for (int i = 0; i < 20; i++) guild.setWikiPage("page" + i, "content");
            assertTrue(guild.setWikiPage("page0", "updated"));
        }
    }

    @Test
    void flagPixelDataLengthMatchesFormula() {
        assertEquals(Guild.MAX_FLAG_SIZE * Guild.MAX_FLAG_SIZE * 6, Guild.FLAG_PIXEL_DATA_LENGTH);
    }

    @Test
    void minSizeIsLessThanMax() {
        assertTrue(Guild.MIN_FLAG_SIZE < Guild.MAX_FLAG_SIZE);
        assertTrue(Guild.DEFAULT_FLAG_SIZE >= Guild.MIN_FLAG_SIZE);
        assertTrue(Guild.DEFAULT_FLAG_SIZE <= Guild.MAX_FLAG_SIZE);
    }
}

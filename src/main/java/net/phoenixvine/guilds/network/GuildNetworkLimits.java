package net.phoenixvine.guilds.network;

/**
 * Shared string-length caps for every {@code readUtf}/{@code writeUtf} pair across this package's
 * packets. Previously these were bare numeric literals duplicated at both the encode and decode
 * call site for each field — currently consistent, but nothing enforced that, so a future edit
 * to one side and not the other would silently desync client/server framing (the reader and
 * writer disagreeing on a field's max length corrupts everything after it in the same packet).
 * Naming them once and referencing the constant on both sides, the same way {@code
 * Guild.FLAG_PIXEL_DATA_LENGTH} already was, closes that risk outright.
 */
public final class GuildNetworkLimits {

    public static final int NAME_MAX = 64;
    public static final int MOTD_MAX = 128;
    public static final int DESCRIPTION_MAX = 256;
    public static final int MEMBER_NAME_MAX = 32;
    public static final int RANK_MAX = 16;
    public static final int WIKI_TITLE_MAX = 64;
    public static final int WIKI_CONTENT_MAX = 512;
    public static final int LOG_MESSAGE_MAX = 256;
    public static final int ACTION_ARG_MAX = 600;
    public static final int ICON_ID_MAX = 256;

    private GuildNetworkLimits() {}
}

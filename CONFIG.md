# Configuration & data

Phoenix Guilds splits its persistence across two different mechanisms —
worth knowing since they behave differently:

## Guild data (memberships, ranks, ownership) — genuinely per-world

`data/GuildManager.java` extends vanilla's `SavedData`, so actual guild data
lives inside the world's own save folder, the same place vanilla stores
things like map data or the Wither's kill count:

- **Singleplayer world**: `saves/<world name>/data/<guild data file>.dat`
- **Dedicated server**: `<server root>/world/data/<guild data file>.dat`

This is real per-world data, not a config file — it does not use Forge's
`serverconfig` mechanism, but the effect is the same (isolated per world).

## Client-side theme — global, not per-world

`client/GuildTheme.java` persists to the game instance's shared `config/`
folder and applies the same way across every world/server:

- `config/phoenix_guilds_themes.json`

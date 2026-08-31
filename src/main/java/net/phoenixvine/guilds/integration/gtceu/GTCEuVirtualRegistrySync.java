package net.phoenixvine.guilds.integration.gtceu;

import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEnderRegistry;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;

import net.minecraft.server.level.ServerLevel;
import net.phoenixvine.guilds.PhoenixGuilds;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public final class GTCEuVirtualRegistrySync {

    private static final EntryTypes<?>[] KNOWN_TYPES = {
            EntryTypes.ENDER_FLUID, EntryTypes.ENDER_ITEM, EntryTypes.ENDER_REDSTONE,
    };

    private GTCEuVirtualRegistrySync() {}

    public static void migrateToGuild(ServerLevel level, UUID from, UUID to) {
        if (from.equals(to)) return;
        try {
            VirtualEnderRegistry registry = VirtualEnderRegistry.get(level);
            for (EntryTypes<?> type : KNOWN_TYPES) {
                migrateType(registry, from, to, type);
            }
        } catch (Throwable t) {
            PhoenixGuilds.LOGGER.error("Failed to migrate GTCEu virtual registry entries from {} to {}. Any " +
                    "existing Ender Link Cover networks may need manual reconfiguring.", from, to, t);
        }
    }

    private static <T extends VirtualEntry> void migrateType(VirtualEnderRegistry registry, UUID from, UUID to,
                                                             @NotNull EntryTypes<T> type) {
        Map<String, VirtualEntry> entriesMap;
        try {
            entriesMap = registry.getEntries(from, type);
        } catch (NullPointerException e) {
            
            return;
        }

        if (entriesMap == null || entriesMap.isEmpty()) return;

        for (String name : new ArrayList<>(entriesMap.keySet())) {
            if (registry.hasEntry(to, type, name)) {
                PhoenixGuilds.LOGGER.warn("GTCEu virtual entry '{}' already exists under {}. Leaving the one " +
                        "from {} in place; it'll need a manual rename to reconnect.", name, to, from);
                continue;
            }
            T entry = registry.getEntry(from, type, name);
            if (entry == null) continue;

            registry.addEntry(to, name, entry);
            registry.forceDeleteEntry(from, type, name);
        }
    }
}
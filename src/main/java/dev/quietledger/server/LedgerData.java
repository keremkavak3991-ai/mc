package dev.quietledger.server;

import dev.quietledger.core.AnomalyCatalog;
import dev.quietledger.core.Journal;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** One server-wide ledger, stored with the overworld; maps are bounded on read and write. */
public final class LedgerData extends SavedData {
    private static final int MAX_PLAYERS = 1024;
    private long activeTicks;
    private long lastOpportunity;
    private long events;
    private boolean suppressed;
    private final Map<AnomalyCatalog.Kind, Long> lastEvents = new EnumMap<>(AnomalyCatalog.Kind.class);
    private final Map<UUID, Long> lastPlayers = new HashMap<>();
    private final Map<UUID, Integer> journals = new HashMap<>();

    public static LedgerData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(LedgerData::new, LedgerData::load), "quietledger_progress");
    }
    public static LedgerData load(CompoundTag tag, HolderLookup.Provider lookup) {
        LedgerData d = new LedgerData();
        d.activeTicks = Math.max(0, tag.getLong("activeTicks"));
        d.lastOpportunity = Math.max(0, Math.min(d.activeTicks, tag.getLong("lastOpportunity")));
        d.events = Math.max(0, tag.getLong("events"));
        d.suppressed = tag.getBoolean("suppressed");
        CompoundTag kinds = tag.getCompound("lastEvents");
        for (var kind : AnomalyCatalog.Kind.values()) {
            if (kinds.contains(kind.name())) d.lastEvents.put(kind, Math.max(0, Math.min(d.activeTicks, kinds.getLong(kind.name()))));
        }
        CompoundTag players = tag.getCompound("lastPlayers");
        for (String key : players.getAllKeys()) {
            if (d.lastPlayers.size() >= MAX_PLAYERS) break;
            try { d.lastPlayers.put(UUID.fromString(key), Math.max(0, Math.min(d.activeTicks, players.getLong(key)))); }
            catch (IllegalArgumentException ignored) { /* Ignore malformed save entries. */ }
        }
        CompoundTag savedJournals = tag.getCompound("journals");
        for (String key : savedJournals.getAllKeys()) {
            if (d.journals.size() >= MAX_PLAYERS) break;
            try { d.journals.put(UUID.fromString(key), savedJournals.getInt(key)); }
            catch (IllegalArgumentException ignored) { /* Ignore malformed UUIDs. */ }
        }
        return d;
    }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup) {
        tag.putLong("activeTicks", activeTicks);
        tag.putLong("lastOpportunity", lastOpportunity);
        tag.putLong("events", events);
        tag.putBoolean("suppressed", suppressed);
        CompoundTag kinds = new CompoundTag();
        lastEvents.forEach((k, v) -> kinds.putLong(k.name(), v));
        tag.put("lastEvents", kinds);
        CompoundTag players = new CompoundTag();
        lastPlayers.forEach((k, v) -> players.putLong(k.toString(), v));
        tag.put("lastPlayers", players);
        CompoundTag savedJournals = new CompoundTag();
        journals.forEach((k, v) -> savedJournals.putInt(k.toString(), v));
        tag.put("journals", savedJournals);
        return tag;
    }
    public void advance(long ticks) { activeTicks = Math.min(Long.MAX_VALUE - ticks, activeTicks) + ticks; setDirty(); }
    public void opportunity() { lastOpportunity = activeTicks; setDirty(); }
    public void recordEvent() { if (events < Long.MAX_VALUE) events++; setDirty(); }
    public void recordScheduled(AnomalyCatalog.Kind kind, UUID player) {
        lastEvents.put(kind, activeTicks);
        // If capacity is reached evict the oldest entry; online players remain eligible over time.
        if (!lastPlayers.containsKey(player) && lastPlayers.size() >= MAX_PLAYERS) {
            UUID oldest = lastPlayers.entrySet().stream().min(Map.Entry.comparingByValue()).orElseThrow().getKey();
            lastPlayers.remove(oldest);
        }
        lastPlayers.put(player, activeTicks);
        recordEvent();
    }
    public boolean playerReady(UUID player, int cooldown) {
        Long last = lastPlayers.get(player);
        return last == null || dev.quietledger.core.Progression.due(activeTicks, last, cooldown);
    }
    public Map<AnomalyCatalog.Kind, Long> lastEvents() { return Map.copyOf(lastEvents); }
    /** New journals stop being added at capacity; existing player discoveries remain intact. */
    public boolean discover(UUID player, Journal.Clue clue) {
        if (clue == null || (!journals.containsKey(player) && journals.size() >= MAX_PLAYERS)) return false;
        int current = journals.getOrDefault(player, 0);
        int updated = Journal.discover(current, clue);
        if (updated == current) return false;
        journals.put(player, updated);
        setDirty();
        return true;
    }
    public int journal(UUID player) { return journals.getOrDefault(player, 0); }
    public void setSuppressed(boolean value) { suppressed = value; setDirty(); }
    public long activeTicks() { return activeTicks; }
    public long lastOpportunity() { return lastOpportunity; }
    public long events() { return events; }
    public boolean suppressed() { return suppressed; }
}

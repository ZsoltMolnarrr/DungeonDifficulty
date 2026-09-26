package net.dungeon_difficulty.config;

/// Applied at game startup, requires restart. Must match between server and clients.
public class RegistryConfig { public RegistryConfig() { }
    // Registers custom status effects (such as `dungeon_difficulty:no_mining`)
    public boolean register_status_effects = true;
}

# 3.0.1

- Update scaling defaults

# 3.0.0

- Update to Minecraft 1.21.1
- Add shield scaling
- Zone pattern matching now supports: biome regex, biome tags, structure regex, structure tags
- Add new structure tags:
  - `dungeon_difficulty:level_1` ... `dungeon_difficulty:level_6`

# 2.2.1

- Enchanted items now have `Uncommon` instead `Rare` rarity (can be disabled in client config)

# 2.2.0

- Scaled items now have increased rarity

# 2.1.0

- Replace Projectile Damage Attribute with Ranged Weapon API

# 2.0.6

- Improve compatibility with BumbleZone, thanks to TelepathicGrunt

# 2.0.5
- Fix difficulty modifiers being applied to players #13

# 2.0.4
- Fix recursive loot scaling #10
- Fix mobs placed as part of structure NBT not scaled #12

# 2.0.3
- Fix manually placed spawners not spawning mobs #8

# 2.0.2
- Fix Null Pointer Exception in Mob Spawner custom rule set causing server wide crash. #7

# 2.0.1
- Temporarily remove scaling equipment of spawned mobs 

# 2.0.0
- Rework configuration structure, now much more user-friendly for changes
- Rework per player difficulty for more linear scaling, added toggle
- Rework item scaling logic
- Add entity scaling safeguard (to scaling multiple times)
- Add structure specific difficulty
- Fix item attributes with matching UUIDs disappearing
- Fix experience modifier
- New default config

# 1.2.1
- Fix some item attributes duplication

# 1.2.0
- Update dependencies
- Support 1.19.3

# 1.1.1
- PowerScale mod has been renamed to Dungeon Difficulty.

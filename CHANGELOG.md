# 3.6.8

- Add generic attribute modifier offset for scaling

# 3.6.7

- Update attribute identifiers for scaled items 

# 3.6.6

- Add `/power_level` command
- Fix weapon rescaling issues

# 3.6.5

- Fix spawners getting repeatedly scaled

# 3.6.4

- Fix some rare crashes #44

# 3.6.3

- Fixed smithing upgrades of Power Leveled items 

# 3.6.2

- Fixed entity scaling with attribute patterns
- Updated default config now buffs mobs specifically by `generic.attack_damage` attribute
- Improved entity scaling stacking with other modifiers

# 3.6.1

- Use unicode Miscellaneous Symbols Supplement characters for special symbols

# 3.6.0

- Migrate to Architectury
- Fix invalid mob spawners spamming errors to console
- Fix double scaling of items upgraded on smithing table (only when merge modifiers is on)

# 3.5.2

- Difficult announcement upon config reload

# 3.5.1

IMPORTANT DISCLAIMER: Config format changed, new config file is name `difficulty_v2`
- Loot scaling is now in a global scope, instead inside of difficulty types
- Optimize multi level difficulty type inheritance
- Support universal pattern matching on many fields
- Slightly improved server performance
- Improved config file consistency
- Add new config option for difficult type overrides

Migration steps:
- Rename `dimension_regex` -> `dimension`
- If `entity_matches` objects are undefined, I recommend removing them
- If `item_matches` objects are undefined, I recommend removing them
- Rename `item_id_regex` -> `id`
- Rename `entity_id_regex` -> `type`
- Migrate the `rewards` objects from your difficulty type into the `loot_scaling` root field
- Add `allow_loot_scaling` to your difficulty types


# 3.5.0

IMPORTANT DISCLAIMER: Config format changed, new config file is name `difficulty_v2`
- Loot scaling is now in a global scope, instead inside of difficulty types
- Optimize multi level difficulty type inheritance
- Support universal pattern matching on many fields
- Slightly improved server performance
- Improved config file consistency
- Add new config option for difficult type overrides 

Migration steps:
- Rename `dimension_regex` -> `dimension`
- If `entity_matches` objects are undefined, I recommend removing them
- If `item_matches` objects are undefined, I recommend removing them
- Rename `item_id_regex` -> `id`
- Rename `entity_id_regex` -> `type`
- Migrate the `rewards` objects from your difficulty type into the `loot_scaling` root field 

# 3.4.2

- Difficulty and  power level font symbols

# 3.4.1

- Fix scaling of out of zone entities #35

# 3.4.0

- Smithing Table support: Power Level value on looted items, is now kept but reduced upon upgrading

# 3.3.5

- Fix boss loot sometimes not being scaled

# 3.3.4

- Fix scaling of looted ranged weapons (RWA 2.1+)
- Fix scaling of looted shields 

# 3.3.3

- Improve difficulty announcement logic
- Add config for high priority difficulty assignments (to be checked before zones)

# 3.3.2

- Improve support for visual difficulty

# 3.3.1

- Add proper support for visual only difficulty, that doesn't really scale entities

# 3.3.0

Warning: Recommended to reset config file!

- Add config for item modifiers be merged (thanks to Muon)
- Universal matchers for: `biome`, `structure`, `entity_type`
  - `#` prefix matches for tags
  - `~` prefix matches for regex
  - no prefix matches for exact match

# 3.2.2

- Improve announcement occurrence

# 3.2.1

- Fix boosted armor bonus attributes not stacking

# 3.2.0

- Decouple loot level from difficulty level
- Add support for entity specific scaling
- Add support for loot table specific scaling

# 3.1.0

- Add difficulty announcement titles
- Add power level display onto scaled loot items
- Relocate configs into `config/dungeon_difficulty`

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

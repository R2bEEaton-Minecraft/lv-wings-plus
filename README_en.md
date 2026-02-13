**Change gradle.properties to your Java path so the build can proceed!**

# Wings

This is a community-maintained fork of the Wings mod.

## 📜 Project History

- **Original Project**: Created by [pau101](https://github.com/pau101/Wings).
- **1.18.2 Port**: Ported and maintained by [jt789](https://github.com/jt789/WingsPort).

This version of Wings is a community-developed continuation project, originally started by pau101 and later ported to Minecraft 1.18.2 by jt789. When you drink a wings potion, you receive different types of wings with unique attributes.

Compared to earlier versions, this branch adds a "Super Wings" item (obtainable only in Creative mode), stronger potion effects, and an effect that repels hostile mobs within a 14-block diameter. While wearing wings, press R to enter flight. Each type of wing consumes your hunger while flying, taking off, and landing.

There are plans to continue maintaining the project in the future.

![super wings fly](https://cdn.modrinth.com/data/cached_images/4552854a5700bc437ef1b81aa5abc6526bd5d93c.png)

## Crafting and Brewing

Base Material:

- Bat Blood Bottle: Obtain by right-clicking a bat with an empty glass bottle.

Wing Potion Brewing:

All wing potions are brewed in a Brewing Stand using a Slow Falling potion or a Long Slow Falling potion as the base.

| Wing Name | Brewing Ingredient |
|---------|---------|
| Angel Wings | Feather |
| Parrot Wings | Red Dye |
| Bat Wings | Bat Blood Bottle |
| Blue Butterfly Wings | Blue Dye |
| Dragon Wings | Leather |
| Evil Wings | Bone |
| Fairy Wings | Oxeye Daisy |
| Fire Wings | Blaze Powder |
| Monarch Butterfly Wings | Orange Dye |
| Slime Wings | Slime Ball |
| Lvjia Super Wings | Creative mode only |

Brewing steps:
1. Place a Slow Falling potion into the Brewing Stand as the base.
2. Put the corresponding ingredient into the top slot.
3. Wait for brewing to complete to obtain the wing potion.

## Custom Changes (1.20.1)

This section documents the custom changes added in this fork.

### Wearable Wings Item
- Added a new `Wings` chest item that equips angel wings when worn and removes them when unequipped.
- The item is dyeable in the crafting grid (like leather armor). The dye color tints the wing model.
- The chest armor model is hidden, so only the wings are visible.
- Wings are crafted by surrounding an Elytra with 4 Angel Wings Bottles.

Usage:
- Equip `Wings` in the chest slot to enable flight and render angel wings.
- Combine `Wings` with any dye in a crafting grid to recolor.

### Invisible Wings Item
- Added `Invisible Wings`, a chest item with the same wing flight behavior as `Wings` but with the wing model fully hidden.
- `Invisible Wings` cannot be recolored in a crafting grid and is not accepted by the Wing Colorizer block.
- Recipe is similar to `Wings` and also requires an Invisibility potion.

Recipe:
- 4 `Angel Wings Bottle`
- 1 `Elytra`
- 1 `Potion of Invisibility`

Usage:
- Equip `Invisible Wings` in the chest slot to keep wing flight mechanics without rendering visible wings.

### Flight Poses
- Added a pose cycle with four styles:
  - Default (both arms forward)
  - Main-hand forward
  - Hands at sides
  - Hands at sides (outward)
- Pose changes are only allowed while flying or hovering.
- The pose preview overlay is disabled by default. Enable it in the mod config screen (`ESC -> Mods -> Wings -> Config`) via `client.enablePosePreview`.
- When enabled, a small inventory-style paper-doll preview appears briefly when the pose changes.

Usage:
- Press `G` while flying/hovering to cycle poses.

### Hover Mode
- Added hover mode to hold position midair with wing flapping.
- Hover auto-disables when movement keys are pressed (WASD, Space, Shift).

Usage:
- Press `H` to toggle hover.
- Press any movement key to exit hover.

### Creative Flight Flapping
- While wearing wings in Creative mode and using vanilla Creative flight, wings now stay in a continuous flap state (hover-style animation).
- This no longer switches to descent-only flapping when moving downward in Creative flight.

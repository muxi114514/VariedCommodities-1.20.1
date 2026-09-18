# Varied Commodities for 1.20.1

An unofficial port of **Varied Commodities** by **Noppes** to Minecraft Forge 1.20.1.

The original mod stopped at 1.12.2 and its source code was never published, so this port was rebuilt from scratch, block by block and item by item. The goal is to keep it as close to the original as possible. The few places where it's different are listed below.

All the credit for the mod itself — the ideas, textures, models and sounds — goes to Noppes. The original mod page is [kodevelopment.nl](http://www.kodevelopment.nl/minecraft/varied-commodities).

## What's in it

Pretty much everything from the 1.12.2 version:

- **Furniture**: tables that join together, chairs and stools you can sit on, couches that connect and can be dyed, shelves, beams, crates, barrels, weapon racks, pedestals that show off an item, banners, tall lamps, lamps, candles, campfires, signs, big signs and tombstones you can write on, a book stand, crystal blocks and blood splatters
- **Carpentry bench**: a 4×4 crafting table. Most VC recipes need it, and normal crafting recipes work on it too
- **Weapons**: a huge pile of them (swords, daggers, spears, battleaxes, glaives, halberds, scythes and more) in materials from wood and stone up to bronze, emerald, demonic, frost and mithril. Daggers can be flipped to a reverse grip in the crafting grid
- **Ranged stuff**: guns, a musket, a machine gun, a crossbow, magic staffs, elemental staffs, kunai, shuriken, a slingshot and the Holy Hand Grenade
- **Armor**, musical instruments, coins, gems, ingots and a recipe book

## What's different from the original

- **Furniture is a lot lighter on your FPS.** The original drew every piece of furniture with a custom renderer every frame, which is why big builds used to lag. Here almost all of it is a normal block model, and only things that actually show an item (pedestals, weapon racks, banners, etc.) still need a renderer.
- **No trading block.** It was left out on purpose.
- **No tracking.** The original sent the server's address to the author's website whenever a player joined. That part is gone.
- **Enchantments are off by default.** The four VC enchantments only work on VC guns and staffs, so having them on just clutters the enchanting table for everyone. You can turn them on in the config (needs a restart).
- **Most recipes are off by default.** Only the carpentry bench and the crates are enabled out of the box, since this is mostly a decoration mod. Every recipe has its own on/off switch in the config.
- **Chairs don't leave junk behind.** Logging out while sitting no longer leaves invisible seat entities stuck in the world.
- **The book stand works like a shelf for one book.** Put a book in, take it out, or read a written book right there. It doesn't open an editor in place like the original did.
- **Big signs and tombstones lock after the first write** in survival, and the lock now actually survives a reload. Creative mode can still edit them.
- **JEI support is built in.** Back on 1.12 you needed a separate mod for it.

## New: crate upgrades

This one isn't from the original mod. You can upgrade a crate in place and keep everything inside it:

| Tier | Slots | Material |
|---|---|---|
| Wooden crate | 54 | – |
| Iron crate | 81 | Iron Block |
| Diamond crate | 108 | Diamond Block |
| Netherite crate | 135 | Netherite Ingot |

Sneak and right-click a crate while holding the material. You can skip tiers (wood straight to netherite is fine), but you can't go back down. By default it costs 4 of the material, and you can change that in the config. The netherite crate is blast proof and doesn't burn in lava, just like other netherite stuff. Barrels can't be upgraded.

## Config

Everything is in `config/variedcommodities-common.toml`: enchantments, recipe switches, gun damage, crate upgrades and so on.

## Heads up

- This is still a work in progress and hasn't been tested much in a real modpack yet, so expect some bugs.
- It uses the same mod id (`variedcommodities`) as the other community 1.20.1 port, so you can't install both at the same time.

## License

Same as the original: [Creative Commons Attribution-NonCommercial 3.0](https://creativecommons.org/licenses/by-nc/3.0/) (see [LICENSE](LICENSE)). You're free to share and change it, as long as you credit Noppes and don't use it commercially.

This port isn't affiliated with or endorsed by Noppes.

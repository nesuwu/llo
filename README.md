# LightLevelOverlay

Shows light levels on blocks. That's it. That's the mod.

## Features

- **Numbers on blocks** - See exactly where mobs can spawn
- **Color coded** - Red (0), Yellow (1-7), Green (8+)
- **Renders above foliage** - No more squinting through grass
- **Hides behind walls** - Only shows what you can actually see
- **Fast** - Cached and optimized, won't tank your FPS

## Controls

| Key | Action |
|-----|--------|
| `F9` | Toggle overlay |
| `F10` | Open config |

## Config

Press `F10` or edit `config/lightleveloverlay-client.json`

- **Range** - How far to scan (horizontal/vertical)
- **Update Interval** - Cache refresh rate in ms
- **Colors** - Customize the RGB values
- **Show Only Spawnable** - Hide safe blocks, show only danger zones
- **Text Scale** - Make numbers bigger or smaller

## Installation

1. Install [NeoForge](https://neoforged.net/)
2. Drop the jar in your `mods` folder
3. Done

## Credits

Inspired by [Light Level 2025](https://github.com/dark-lion-jp/light-level-2025) by dark-lion-jp. This is a standalone NeoForge implementation - not a port, not affiliated. Bug reports go here, not there.

## License

MIT
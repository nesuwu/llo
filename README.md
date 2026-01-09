# LightLevelOverlay

Shows light levels on blocks. That is it. That is the mod.

## Features

- Numbers on blocks so you can see exactly where mobs can spawn
- Color coded: Red (0), Yellow (1-7), Green (8+)
- Renders above foliage so you do not have to squint through grass
- Hides behind walls and only shows what you can actually see
- Fast, cached and optimized so it will not tank your FPS
- Underwater mode for drowned spawn prevention

## Platforms

Works on both Fabric and NeoForge.

## Controls

| Key | Action |
|-----|--------|
| F9 | Toggle overlay |
| F10 | Open config |

## Config

Press F10 or edit `config/lightleveloverlay-client.json`

- Range: How far to scan (horizontal/vertical)
- Update Interval: Cache refresh rate in ms
- Colors: Customize the RGB values
- Show Only Spawnable: Hide safe blocks, show only danger zones
- Text Scale: Make numbers bigger or smaller
- Underwater Mode: Show light levels in water

## Installation

1. Install Fabric or NeoForge for Minecraft 1.21.1
2. Install Architectury API for your loader
3. Install Cloth Config for your loader
4. Drop the jar in your mods folder
5. Done

## Dependencies

- Architectury API (required)
- Cloth Config (required)
- Fabric API (required for Fabric only)

## Credits

Inspired by Light Level 2025 by dark-lion-jp. This is a standalone implementation, not a port, not affiliated. Bug reports go here, not there.

## License

MIT
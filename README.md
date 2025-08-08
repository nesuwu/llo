# LLO - Light Level Overlay

A simple NeoForge mod that helps you optimize your lighting by displaying block light levels on surfaces. The mod provides a simple, color-coded overlay that makes it easy to manage mob spawning and ensure your builds are safe.

This is an unofficial NeoForge port of Dark-lion-jp's Light Level 2025 mod, available [here](https://github.com/dark-lion-jp/light-level-2025).

## Features

* **Light Level Overlay:** Displays the block light level on top of solid blocks within a 16-block radius of the player.
* **Color-Coded Indicators:** The mod uses a color system to quickly show the spawning status of a block:
    * **Green (8+):** The block is safe; mobs cannot spawn here.
    * **Yellow (1-7):** A warning color, indicating the block needs more light to be completely safe from spawning mobs.
    * **Red (0):** The block is dark enough for hostile mobs to spawn.
* **Toggleable:** The overlay can be toggled on or off at any time using the default keybind `F9`. You can change this keybind in your Minecraft controls settings.
* **Optimized:** Renders light levels only on valid top surfaces exposed to air to reduce noise and improve performance.

## Controls

- Toggle overlay: `F9`
- Open config: `F10` (or open from the Mods list via the Config button)

## Configuration

You can configure the mod in-game (preferred) or via JSON.

In-game config categories:
- General
  - Horizontal Range (1–128)
  - Vertical Range (1–64)
  - Update Interval (ms) (16–2000)
- Visuals
  - Color: Light Level 0 (red)
  - Color: Light Level 1–7 (yellow)
  - Color: Safe (>=8) (green)
  - Show Only Spawnable (show only 0–7)
  - Text Scale (0.015–0.06)

Config file path (created/updated automatically):
- Windows: `%APPDATA%\\.minecraft\\config\\lightleveloverlay-client.json`
- macOS: `~/Library/Application Support/minecraft/config/lightleveloverlay-client.json`
- Linux: `~/.minecraft/config/lightleveloverlay-client.json`

Notes:
- Color pickers store RGB only; the overlay always renders fully opaque.
- For compatibility with older versions, `showOnlySpawnable` is kept in sync with the legacy `showOnlyUnsafe` flag.

## Installation

This mod targets NeoForge and depends on Cloth Config (bundled for dev/runtime in this project).

1.  Make sure you have a NeoForge installation.
2.  Place the mod's `.jar` file into your Minecraft `mods` folder.
3.  Launch the game to start using the mod.

If you encounter any issues with missing libraries, you can run `gradlew --refresh-dependencies` to refresh your local cache.

## Authors and License

* **Light Level 2025:** dark-lion-jp
* **Author:** Nesuwu (me)
* **License:** MIT License

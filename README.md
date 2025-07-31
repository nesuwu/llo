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
* **(Somewhat) Optimized:** The mod only renders light levels on blocks with a solid top face that are exposed to an air block above them. This prevents rendering the overlay inside cave walls or under ceilings, improving performance and clarity (the mod really lagged without this 💀💀).

## Installation

This mod is for NeoForge and has no additional dependencies other than the NeoForge loader itself.

1.  Make sure you have a NeoForge installation.
2.  Place the mod's `.jar` file into your Minecraft `mods` folder.
3.  Launch the game to start using the mod.

If you encounter any issues with missing libraries, you can run `gradlew --refresh-dependencies` to refresh your local cache.

## Authors and License

* **Light Level 2025:** dark-lion-jp
* **Author:** Nesuwu (me)
* **License:** MIT License

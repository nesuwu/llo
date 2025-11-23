# LightLevelOverlay

This mod does exactly what it says in the name. It’s a simple tool, so don’t expect a bunch of extra bells and whistles!

## Disclaimer
Just a quick heads-up: **I wrote this whole thing from scratch as a standalone project.** It isn’t a port of any other mod, official or otherwise.  
I’m not affiliated with dark-lion-jp in any way, but I was definitely inspired by their Light Level mod! So, please don’t bother them with any issues you find here—come to me instead.

## Features

- **Simple Overlay:** It shows you the light level numbers right on top of blocks. This helps you stop mobs from spawning where you don’t want them.
- **Color Coded:**
  - **Green:** You’re safe! (Light level 8+)
  - **Yellow:** Be careful, it’s a bit risky. (Light level 1–7)
  - **Red:** Watch out, it’s dangerous! (Light level 0)
- **Performance:** I made sure it uses caching so it doesn’t slow down your game.
- **Configurable:** Feel free to change the ranges, colors, and how often it updates to fit your needs.

## Usage

- **Toggle Overlay:** Just press `F9`
- **Open Config:** Just press `F10`

## Installation

1. First, make sure you have **NeoForge** installed.
2. Download the latest version of **LightLevelOverlay**.
3. Pop the `.jar` file into your `mods` folder.
4. Start up the game and you’re good to go!

## Configuration
You can change the settings while you're playing by pressing `F10`, or you can edit the file at  
`config/lightleveloverlay-client.json` if you prefer.

- **Horizontal Range:** How far around you the mod checks for light levels.
- **Vertical Range:** How high up or down it checks.
- **Update Interval:** How often it re-checks the light levels. Lower numbers mean it’s smoother, but it might use a bit more computer power.
- **Show Only Spawnable:** Turn this on to hide the safe blocks and only see where mobs might spawn.

## License

This project is licensed under the **MIT License**.

# Create: Netherite Portable Engine

<img src="src/main/resources/assets/netheriteportableengine/icon.png" alt="Create: Netherite Portable Engine icon" width="128">

A small Create Simulated addon that adds a netherite portable engine.

## Description

The mod adds the **Netherite Portable Engine** block, which is four times more powerful than a regular Portable Engine. Like other netherite gear, it is upgraded on a smithing table using a Netherite Upgrade Smithing Template and a Netherite Ingot.

## Nerfed fork

Based on [Vladislav117/create-netherite-portable-engine](https://github.com/Vladislav117/create-netherite-portable-engine), commit `9918f1cbb8f7c8f4914fe97aa28bce55fc44d609`.

- All finite fuels burn four times as fast, including superheated fuels and cake. Creative infinite fuel stays infinite.
- Every netherite engine has a Sable mass of 16 units (kpg).
- Dyeing works in the crafting grid and by using dye on a placed engine, with all 16 dye colors. The engine stays netherite and keeps its power, fuel, direction, and heat when recolored in place. Crafting preserves item components.
- The original block ID is preserved for existing worlds. Colored variants retain their color when mined.
- Power output and the smithing upgrade recipe are unchanged. Colored engines use Simulated's colored body textures and netherite moving parts.

Requires Minecraft 1.21.1, NeoForge 21.1.230+, Create 6.0.10+, and Create Simulated 1.2.1 (with its dependencies). Replace the original mod JAR with this one on the client and server; both use the same mod ID.

## Images

### Netherite Portable Engine
<img src="gallery/0.png" alt="Netherite Portable Engine" width="320">

### Power comparison
<img src="gallery/1.png" alt="Power comparison" width="320">

### Crafting
<img src="gallery/2.png" alt="Crafting" width="320">

## Build

```bash
./gradlew runData
./gradlew build
```

The built `.jar` will be in `build/libs`.

Run the server regression tests after generating data:

```bash
./gradlew runGameTestServer
```

The four tests cover finite fuel drain and bucket remainders, creative fuel, all 16 crafting colors and item components, recoloring a running engine without losing state, colored loot, Sable mass across every block state, and unchanged power/ordinary engines. Tests and their structure template are excluded from the mod JAR.

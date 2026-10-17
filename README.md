# Auto Drop Item — Fabric 1.21.11

Client-side Fabric mod that drops a configured item from your inventory.

## Controls
- **O** — toggle automatic dropping on/off.
- **P** — drop one matching stack/action immediately.

## Configuration
Run Minecraft once with the mod, then edit:
`config/auto-drop-item.properties`

Example:
```properties
item=minecraft:rotten_flesh
drop-whole-stack=true
tick-delay=5
```

Examples of item IDs:
- `minecraft:cobblestone`
- `minecraft:dirt`
- `minecraft:rotten_flesh`
- `minecraft:diamond`

`drop-whole-stack=true` drops the whole stack; `false` drops one item per scan.
`tick-delay` is the number of client ticks between scans (20 ticks ≈ 1 second).

## Build
Requires JDK 21 and internet access to download Gradle dependencies:
```bash
gradle build
```
The output JAR will be in `build/libs/`.

This is source project code, not a precompiled JAR. Test on a single-player world first.
Server rules/plugins may restrict dropping items.

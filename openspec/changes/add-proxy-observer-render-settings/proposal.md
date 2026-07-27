## Why

Peripheral Proxy and Remote Observer overlays are currently fixed, while Network Manager overlays can be adjusted to suit a player's visibility needs. Extending the same text and box style controls to these blocks makes configurator behavior consistent and lets their shared block-level presentation be managed from both the UI and Lua.

## What Changes

- Open a dedicated render-settings screen when a bound Ultimate Configurator is right-clicked in the air for a Peripheral Proxy or Remote Observer.
- Configure one text style and one box style for all targets of the bound block, using the Network Manager style choices.
- Render Peripheral Proxy target peripheral names and Remote Observer target block names according to the selected text style.
- Apply the selected box style with green bound blocks, orange Observer targets, and orange Proxy targets with a green attached face.
- Persist and synchronize render settings on each Peripheral Proxy and Remote Observer block entity.
- Synchronize direct Remote Observer tracking additions and removals so clients discard stale targets.
- Include `textStyle` and `boxStyle` in each peripheral's `getConfiguration` result and expose `setTextStyle` and `setBoxStyle` Lua methods.
- Preserve existing visuals by default: Peripheral Proxy uses regular text with flare boxes, and Remote Observer uses no text with flare boxes.

## Capabilities

### New Capabilities
- `configurable-target-rendering`: Configurator UI, rendering behavior, persistence, synchronization, and Lua configuration for Peripheral Proxy and Remote Observer target overlays.

### Modified Capabilities

None.

## Impact

- Shared configurator mode, screen, rendering, and networking code in the core module.
- Peripheral Proxy and Remote Observer block entities, client renderers, and ComputerCraft peripheral APIs.
- Loader client-platform bridges used to open screens.
- Client GameTests, server GameTests, TypeScript fixtures, localization, and generated Lua API documentation.
- No new dependencies and no breaking changes; persisted blocks without the new fields retain their current presentation.

## Why

The AE2 integration currently turns any adjacent AE2 network block into a ComputerCraft peripheral, making peripheral availability accidental and difficult to understand. Players also need a native way to carry a CC:Tweaked wired network through AE2 P2P tunnels, including across dimensions.

## What Changes

- Add a dedicated AE2 network peripheral block that joins an AE2 network, requires a channel, and exposes the existing `ae2` Lua methods.
- Keep the existing `ae2` peripheral plugin on arbitrary AE2 network block entities for backward compatibility while adding the dedicated block as the explicit channel-owning option.
- Add a CC:Tweaked cable P2P tunnel part that is attuned from an AE2 P2P tunnel with a CC cable or wired modem.
- Make linked cable P2P tunnel endpoints bridge their attached CC:Tweaked wired networks bidirectionally while their AE2 tunnel nodes are active.
- Add recipes, models, localization, optional-AE2 registration, and multi-loader GameTests for both devices.
- Keep the existing generic AE2 item, fluid, and energy storage integrations unchanged.
- Existing computers that wrap arbitrary AE2 blocks continue to expose the same `ae2` Lua API.

## Capabilities

### New Capabilities

- `ae2-network-peripheral`: A dedicated, channel-aware AE2 block that exposes the existing AE2 network Lua interface to CC:Tweaked.
- `ae2-wired-p2p-tunnel`: An AE2 P2P tunnel type that transparently joins linked CC:Tweaked wired networks.

### Modified Capabilities

None.

## Impact

- Affects the optional AE2 integration, ComputerCraft peripheral provider registration, AE2 managed-node lifecycle, and integration configuration.
- Adds an AE2 part item and attunement tags plus a dedicated block, block entity, resources, recipes, and creative-tab entries when AE2 is present.
- Requires loader-specific optional registration where AE2 and CC:Tweaked APIs differ, while preserving shared behavior across Fabric and Forge.
- Requires GameTests for peripheral scoping, AE2 node connectivity, P2P wired-network joining and separation, lifecycle cleanup, and both supported loaders.

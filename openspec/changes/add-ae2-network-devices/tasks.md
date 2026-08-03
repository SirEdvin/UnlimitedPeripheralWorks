## 1. Optional AE2 Registration

- [x] 1.1 Add registration-time AE2 integration entry points for the ME network peripheral block/block entity and wired network P2P part item on Fabric and Forge, while keeping AE2 classes out of no-AE2 startup paths.
- [x] 1.2 Make the shared AE2 integration sources compile reliably for both loaders and isolate only loader-specific registry, model, capability, and wired-element lookup code.
- [x] 1.3 Wire the existing ME-interface configuration to the dedicated block and peripheral without changing the independent storage-integration setting.

## 2. ME Network Peripheral

- [x] 2.1 Implement the `me_network_peripheral` block entity with a channel-requiring managed AE2 node, smart cable connections on every face, visual representation, and complete load/unload/removal cleanup.
- [x] 2.2 Restrict `MENetworkBlockPlugin.Provider` to the new block entity and route grid access and crafting action sources through its managed node while preserving all existing Lua signatures and results.
- [x] 2.3 Add AE2 integration GameTests proving only the dedicated block exposes the `ae2` plugin, active/disconnected behavior is safe, the node consumes and releases a channel, and generic AE2 storage adapters still work on eligible AE2 blocks.

## 3. Wired Network P2P Tunnel

- [x] 3.1 Implement the `wired_network_p2p_tunnel` AE2 capability P2P part with owned CC:Tweaked internal and outward-facing wired nodes.
- [x] 3.2 Join active linked input and output endpoint nodes bidirectionally, including one-to-many and cross-dimensional links, and connect only the wired element directly outside each part face.
- [x] 3.3 Recompute and symmetrically remove owned wired-node edges on tunnel relinking, AE2 activity changes, neighbor changes, chunk unload, and part removal.
- [x] 3.4 Register AE2 P2P attunement for CC:Tweaked networking cable, wired modem, and full-block wired modem items.
- [x] 3.5 Add AE2 integration GameTests for peripheral discovery and modem packet flow through one-to-one and one-to-many links, outward-face isolation, relinking, deactivation, and removal cleanup.

## 4. Content And Migration

- [x] 4.1 Add datagenerated recipes, loot, block/item models, AE2 part models, attunement tags, and creative-tab entries with appropriate AE2 load conditions.
- [x] 4.2 Add English and Ukrainian names/tooltips for both devices and document the migration from wrapping arbitrary AE2 blocks to placing the ME network peripheral.

## 5. Verification

- [x] 5.1 Run formatting and the timed full Gradle build for both loader targets.
- [x] 5.2 Run targeted AE2 GameTests for Fabric and Forge under `xvfb-run` with explicit timeouts and confirm all bridge and P2P scenarios pass.
- [x] 5.3 Run the minimal no-AE2 GameTest environment and verify both loaders start without resolving optional AE2 classes.
- [x] 5.4 Launch an AE2-enabled development client under `xvfb-run` with an explicit timeout and verify recipes, models, attunement, channel state, peripheral attachment, and memory-card linking in game.

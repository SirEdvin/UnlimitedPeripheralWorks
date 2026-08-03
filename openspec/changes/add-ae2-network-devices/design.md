## Context

The current AE2 integration is loader-independent Kotlin duplicated from Fabric into Forge. `MENetworkBlockPlugin.Provider` accepts every `AENetworkBlockEntity`, so a computer can gain the full `ae2` method set by wrapping an arbitrary AE2 block. The same integration also registers generic item, fluid, and energy adapters for AE2 network block entities; those adapters solve a separate storage interoperability problem and must remain available.

This change also introduces an AE2 P2P part. Advanced Peripherals 0.8 is the behavioral reference: its cable tunnel uses AE2's capability P2P base and connects CC:Tweaked wired nodes for the linked input/output set. Unlimited Peripheral Works must provide equivalent behavior on both Fabric and Forge 1.20.1 without depending on Advanced Peripherals.

Reference material:

- Advanced Peripherals cable P2P implementation at commit `fafb3877eed9c40b5a5d56b20421b8625b2d8cce`: https://github.com/IntelligenceModding/AdvancedPeripherals/blob/fafb3877eed9c40b5a5d56b20421b8625b2d8cce/src/main/java/de/srendi/advancedperipherals/common/addons/ae2/WiredCableP2PTunnelPart.java
- Advanced Peripherals P2P registration and attunement at the same commit: https://github.com/IntelligenceModding/AdvancedPeripherals/blob/fafb3877eed9c40b5a5d56b20421b8625b2d8cce/src/main/java/de/srendi/advancedperipherals/common/addons/ae2/AE2Registries.java
- Advanced Peripherals cable P2P user documentation: https://docs.advanced-peripherals.de/0.8/integrations/ae2/cable_p2p_tunnel/
- Advanced Peripherals 1.20.1 ME Bridge block entity at commit `9f0101b22bd66418d2114f2e08fc61a11b1b77cb`: https://github.com/IntelligenceModding/AdvancedPeripherals/blob/9f0101b22bd66418d2114f2e08fc61a11b1b77cb/src/main/java/de/srendi/advancedperipherals/common/blocks/blockentities/MeBridgeEntity.java

## Goals / Non-Goals

**Goals:**

- Add an explicit channel-owning block for the `ae2` Lua interface while preserving existing arbitrary AE2 block wrappers.
- Preserve the current Lua method names, arguments, and results.
- Carry complete CC:Tweaked wired-network behavior through AE2's linked P2P topology, bidirectionally and across dimensions.
- Use AE2's native node, part, model, and attunement APIs and CC:Tweaked's native wired-node API.
- Keep AE2 optional and verify equivalent behavior on Fabric and Forge.

**Non-Goals:**

- Importing Advanced Peripherals' broader ME Bridge API, filters, transfer methods, or crafting job model.
- Changing the existing generic AE2 item, fluid, or energy storage adapters.
- Making the P2P tunnel carry wireless modem traffic or creating a second networking protocol.
- Supporting inactive or unloaded tunnel endpoints as chunk loaders.
- Removing arbitrary AE2 blocks as `ae2` peripherals.

## Decisions

### Give the dedicated block its own AE2 node

Register `me_network_peripheral` and its block entity as optional AE2 integration content. The block entity owns one AE2 managed in-world node, marks it as requiring a channel, supplies the block item as its visual representation, exposes smart cable connections on every face, and creates/destroys the node through load, unload, and removal lifecycle hooks.

Keep `MENetworkBlockPlugin` as the Lua implementation and use each accepted block entity's node as the grid and `IActionSource`. The provider accepts the dedicated block and other `AENetworkBlockEntity` instances so existing worlds remain compatible, while the dedicated block provides an explicit channel-owning network participant.

Alternative: restrict the plugin to the dedicated block. Rejected because existing worlds and automation already depend on wrapping arbitrary `AENetworkBlockEntity` instances.

Alternative: port Advanced Peripherals' ME Bridge peripheral. Rejected because the requested block replaces the current UPW integration; adding a second, larger API would be unrelated compatibility work.

### Keep storage adapters separate from peripheral selection

Do not narrow `MENetworkBlockPlugin.Provider`, `extractItemStorage`, `extractFluidStorage`, or `extractEnergyStorage`. The existing `enableStorageIntegrations` setting remains independent; the existing ME-interface setting controls both the legacy provider and dedicated block/peripheral behavior and can be renamed only if configuration migration is explicitly handled.

Alternative: restrict all AE2 integration to the new block. Rejected because that would silently remove unrelated storage interoperability and expand the breaking change.

### Implement the tunnel as a native AE2 P2P part

Register `wired_network_p2p_tunnel` as an AE2 part item and use AE2's capability P2P tunnel base for link and active-state behavior. Register its attunement tag with CC:Tweaked's cable, wired modem, and full-block wired modem items. Register part models through AE2's model mechanism.

Each part owns an internal CC:Tweaked wired element/node and an outward-facing wired element/node. While active, the internal node joins the part's outward node and the internal nodes of every active linked input/output peer. Recompute the peer set on tunnel configuration, AE2 network, and node-state changes. Query only the block position directly outside the part face for an external wired element.

Alternative: relay peripheral attach/detach events or modem packets manually. Rejected because joining native wired nodes already carries both peripheral discovery and packets with less state and correct CC:Tweaked semantics.

### Treat AE2 direction as topology, not traffic direction

Include both `getInput()` and `getOutputStream()` peers when forming wired links. AE2 still owns memory-card linking and channel activation, but CC:Tweaked traffic flows both ways and all active outputs share one wired network.

Alternative: forward only from AE2 input to outputs. Rejected because CC:Tweaked wired networks are bidirectional and computers and peripherals can exist on either endpoint.

### Remove every owned connection on deactivation

Track the peer parts currently connected by each endpoint. On recomputation, disconnect peers no longer in the active linked set. On deactivation, unload, or removal, disconnect the outward node, disconnect peer nodes symmetrically, and remove owned CC nodes so no stale cross-dimensional network remains. Neighbor changes refresh the outward-face connection.

Alternative: rely only on weak references or eventual neighbor updates. Rejected because stale wired-node edges can leak peripherals between networks after relinking or unloading.

### Register optional content before registry freeze

Split AE2 integration initialization into registration-time and common-setup work as required by each loader. Registration-time code creates the block, block entity type, part item, and models only when AE2 is present; common setup registers attunement and providers. Keep behavior source shared where the APIs match, but use small loader entry points for registry and capability/lookup differences instead of extending the current source-copy workaround unnecessarily.

Datagenerate recipes, loot, block/item models, part models, tags, and English/Ukrainian localization with AE2 load conditions where resource formats support them. Ensure the existing minimal environment still excludes integration classes.

Alternative: register integration content from the current late `Integration.run()`. Rejected because Forge registries are frozen before common setup and Fabric registration also must occur during initialization.

## Risks / Trade-offs

- [AE2's P2P and part APIs differ between the Fabric and Forge artifacts] -> Isolate only the differing registration and wired-element lookup code; keep topology behavior identical and test both loaders.
- [A CC node survives after a part or remote endpoint unloads] -> Make cleanup explicit in every AE2 part lifecycle/deactivation callback and assert separation in GameTests.
- [Cross-dimensional peer callbacks occur while one level is unloading] -> Recompute only on the server thread, tolerate missing peers, and disconnect tracked edges before removing owned nodes.
- [Optional classes are resolved when AE2 is absent] -> Keep AE2-typed classes behind loader mod-presence checks and verify a minimal no-AE2 build/GameTest startup.
- [Existing automation wraps arbitrary AE2 blocks] -> Preserve the broad provider predicate and existing Lua surface as an explicit backward-compatibility exception.
- [The current Forge AE2 source-copy task is not a reliable compilation dependency] -> Either make the shared source set explicit or wire the copy task into Kotlin compilation as part of implementation, without duplicating divergent logic.

## Migration Plan

Add the optional registrations and resources while retaining the broad plugin provider predicate. Existing worlds continue working unchanged; players can use the new ME network peripheral when they want an explicit channel-owning block. Existing generic storage integration configuration and behavior remain unchanged.

Rollback removes the new block and part. Worlds rolled back after placing the new content will report missing blocks/items, so rollback should be performed only with a backup or after removing the devices.

## Open Questions

None.

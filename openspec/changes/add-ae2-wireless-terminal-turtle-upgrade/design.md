## Context

UPW already adapts storage from an `AENetworkBlockEntity` into an `AEItemStorage`, and Tweakium's regular `item_storage` plugin provides item listing and named-peripheral transfers. It also exposes a turtle's inventory through `TurtlePeripheralOwner.storage` and provides `FuelBoon` through `TurtlePeripheralOwner.attachFuel()`.

The wireless terminal use case differs in two ways. Its AE2 storage is discovered from item NBT and the turtle's current position rather than a stationary block entity, and its transfer endpoint is always the same turtle rather than a peripheral name. A normal `ItemStoragePlugin` therefore cannot be reused unchanged because it retains one storage object and requires named peripherals.

AE2 15's standard Wireless Terminal stores its linked access-point position in item NBT. Vanilla resolves that block entity to discover the grid, scans the grid's access points for an active in-range access point, and normally drains terminal charge while a menu remains open. CC:Tweaked stores configurable turtle upgrade state separately from the upgrade's constant crafting item, so default upgrade behavior would discard the terminal's link, charge, cards, and other NBT when unequipped.

## Goals / Non-Goals

**Goals:**

- Equip a linked standard AE2 Wireless Terminal directly, without introducing another item or recipe.
- Preserve the exact terminal item across equip, turtle persistence, and unequip.
- Expose network item listing and implicit transfers to and from the turtle inventory.
- Revalidate the linked grid and wireless range on every call without chunk loading.
- Charge turtle fuel rather than terminal AE power for mutation calls.
- Attribute AE2 storage mutations to the turtle owner.
- Publish a TypeScript contract aligned with the runtime API.
- Keep Fabric and Forge behavior and tests equivalent.

**Non-Goals:**

- Wireless Crafting Terminal or addon-terminal support.
- Fluid or addon `AEKeyType` access.
- AE2 crafting requests or terminal UI configuration.
- Cross-dimensional, infinite-range, or chunk-loading behavior.
- Terminal battery drain or charging while installed.
- A general redesign of Tweakium's item-storage plugins.

## Decisions

### The linked Wireless Terminal is the upgrade item

Register an AE2-specific turtle upgrade whose custom crafting item is the standard Wireless Terminal. Require link NBT during suitability checks. Do not add a UPW bridge item or recipe.

The upgrade must override the CC:Tweaked item/data round trip: copy the equipped stack into upgrade data and reconstruct it from that data in `getUpgradeItem`. The runtime peripheral must read and update the terminal state through the turtle side's authoritative upgrade data rather than retaining the serializer's default crafting stack.

This keeps pairing in AE2's existing Wireless Access Point workflow and guarantees that unequipping returns the player's original terminal. A separate adapter item was rejected because it would duplicate link persistence and add a recipe without improving behavior.

### Every call resolves a fresh wireless session

Resolve the linked position from the stored terminal stack on every method call. Require the linked block entity to be already loaded and ticking, obtain its current grid, and search that grid for the nearest active access point in the turtle's current level whose range contains the turtle position.

Do not cache `IGrid`, `MEStorage`, access-point, or block-entity instances. Turtles move, grids split, access points lose channels, and Lua can retain peripherals across all of those changes. A cached storage reference was rejected because it would allow stale or out-of-range access.

Use AE2's public terminal/link and access-point APIs where possible, but perform the range calculation against the turtle position rather than constructing a menu host. Match vanilla's strict same-level and active-access-point checks and never call APIs that force a chunk load.

### The turtle owner is the AE2 action source

Wrap storage simulation and mutation in `TurtlePeripheralOwner.withPlayer(..., skipInventory = true)` and create a player `IActionSource` from that fake player. This preserves the turtle's owning profile when available and follows the project's existing fake-player conventions.

Using `IActionSource.empty()` was rejected because it loses ownership attribution. Treating the linked access point as the acting machine was rejected because the access point is only the connection anchor, not the initiator.

### A dedicated peripheral exposes item-storage-like methods

Expose peripheral type `ae2_wireless_terminal` with:

```text
items(detailed?, filter?)
pullItem(itemQuery?, limit?, toSlot?)
pushItem(fromSlotOrItemQuery?, limit?)
```

The method names use the turtle caller's perspective: `pullItem` moves AE2 to turtle and `pushItem` moves turtle to AE2. `pullItem` accepts an optional one-based destination slot. `pushItem` accepts either a one-based source slot or an item query as its first argument.

Use Tweakium's existing item representation and item-query conversion. Reuse `PeripheralWorksConfig.itemStorageTransferLimit`. Implement transfer methods directly or through a narrowly scoped shared transfer primitive; do not add a generalized plugin abstraction solely for this peripheral.

### Resolve AE storage with a caller-supplied action source

The existing `AEItemStorage` couples `MEStorage` operations to `IActionSource.ofMachine(entity)` and `entity.setChanged()`. The wireless peripheral needs the same item conversion and storage behavior with a player action source and no stationary entity.

Minimally generalize the adapter to receive its action source and optional change callback, preserving the current block integration behavior. If the loader APIs prevent an identical change, keep equivalent narrow implementations in each loader. A second full AE item-storage implementation was rejected because it would duplicate key conversion and transfer semantics.

### Fuel is charged once per validated transfer call

Attach a `FuelBoon` with a maximum consumption rate consistent with existing turtle upgrades. After link, range, arguments, and slots are validated, require and consume one base fuel before executing either mutation. A valid operation that moves zero items still costs fuel; rejected calls and `items()` do not. The fuel consumption rate multiplier remains available through inherited Fuel API methods, and CC:Tweaked's fuel-disabled mode remains free.

Per-item and distance-based charging were rejected by product decision. Terminal AE charge is retained but not consumed, avoiding an installed peripheral that must be repeatedly removed for charging.

### The typed contract is a new additive source module

Create `projects/typed-peripheral-unlimitedperipheralworks/integrations/ae2WirelessTerminal.ts`. It should import `ItemQuery` from `@siredvin/typed-peripheral-api/item_storage`, item detail types and `IPeripheralProvider` from `@siredvin/typed-peripheral-base`, and `FuelApi` from `@siredvin/typed-peripheral-api/fuel`.

Define listing overloads equivalent to `ItemStorageAPI`, redefine `pushItem` and `pullItem` with implicit turtle endpoints and optional slots, extend `FuelApi`, and export an `IPeripheralProvider` for `ae2_wireless_terminal`. Do not modify `integrations/ae2.ts`, which describes network-level methods attached to stationary AE blocks. Generated `.d.ts` and `.lua` files remain untracked build output.

### Registration remains inside the optional AE2 integration

Register the serializer, generated turtle upgrade data, client model, language entry, and peripheral only when AE2 integration loading runs. Use the existing integration hooks demonstrated by Nature's Compass and ProjectE. Render the terminal item slightly scaled down and facing upward as the turtle upgrade model rather than adding a custom model asset.

Keep implementation and GameTests in the Fabric and Forge AE2 integration source sets because core must remain free of hard AE2 references. Preserve equivalent behavior despite the project's supported AE2 version difference.

## Risks / Trade-offs

- [Default CC:Tweaked upgrade serialization returns a constant crafting item] -> Explicitly test complete terminal NBT through equip, save/load, and unequip.
- [A turtle can move or the AE2 grid can change between calls] -> Resolve the linked grid, access point, range, storage, and action source for every call.
- [The linked access-point chunk may be unloaded even when another network access point is nearby] -> Match vanilla terminal anchoring and fail without loading the chunk.
- [Fuel can be consumed when a valid transfer moves zero items] -> Document and test the product-selected per-operation semantics.
- [Fabric and Forge AE2 source is duplicated] -> Keep public behavior and GameTests equivalent and avoid loader-specific behavior in the typed contract.
- [Transfer simulation and mutation could use different action sources] -> Build one owner-derived action source per call and use it for both phases.
- [Terminal Energy Cards no longer affect runtime cost] -> Preserve them for exact item round trips but document that turtle fuel replaces terminal charge while installed.

## Migration Plan

This is additive and introduces no world migration. Register the optional upgrade and generated data when AE2 is present, add the typed source, and verify both loaders. Removing the feature unregisters the upgrade; players should unequip terminals before rollback so no equipped upgrade data becomes inaccessible.

## Open Questions

- Whether future support for Wireless Crafting Terminals should use a second upgrade ID with the same peripheral contract.
- Whether future AE2 versions expose a public wireless-session helper that can replace the small vanilla-equivalent range resolver.

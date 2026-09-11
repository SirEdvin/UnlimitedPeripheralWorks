## Context

The existing AE2 integration adds network-level methods to `AENetworkBlockEntity` instances. It does not expose the local configuration of an ME Interface or any multipart device mounted in a `CableBusBlockEntity`. AE2 represents these local controls through several distinct APIs: stock inventories with amounts, type-only filter inventories, upgrade inventories, settings managers, priorities, numeric thresholds, and real encoded-pattern inventories.

The public typed contract for this change is `projects/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects.ts`. The implementation supports Forge AE2 15.2.13 and Fabric AE2 15.0.7-beta without leaking loader-specific fluid units.

## Goals / Non-Goals

**Goals:**

- Give full-block machines direct methods for their one logical device.
- Let a cable peripheral return a side-bound Lua object for each supported multipart device.
- Expose semantic operations such as stock targets, filters, thresholds, and pattern slots rather than a generic `setConfig` method.
- Reuse AE2 mutation APIs so saves, stock replanning, watchers, crafting-provider updates, and storage remounts occur normally.
- Transfer real upgrade cards and patterns through inventories visible to the calling computer.
- Keep the TypeScript contract and runtime method surface aligned.

**Non-Goals:**

- Arbitrary addon-defined `AEKeyType` serialization.
- Reconstructing exact NBT- or capability-sensitive keys from an item ID or NBT hash.
- Pattern authoring through a Pattern Encoding Terminal.
- Editing a storage cell through a Cell Workbench.
- Adding configuration to an Annihilation Plane, which has no corresponding AE2 configuration surface.
- Exposing terminal display preferences or every upgrade-only production machine in the first implementation.

## Decisions

### Full blocks expose devices directly; cable blocks return side objects

An ME Interface block and Pattern Provider block each represent one logical device and receive their device methods directly as peripheral plugins. A cable host receives `getSide(direction)`, which returns the concrete API object for the part mounted on that side.

This avoids a redundant device argument for full blocks and avoids flattening several cable parts into one ambiguous method namespace. The alternative of adding a device ID to every method was rejected because callers already have a natural Minecraft direction for multipart parts.

Pattern Provider push direction uses a separate direction type that includes `all`, matching AE2's default state without allowing `all` in cable-side lookup.

### Side objects store locators, not part instances

A returned object stores the server level, cable position, side, expected part kind, and originating computer access. Each call resolves the current part again. Replacing a part with the same kind keeps the object usable; removing it or changing its kind produces an operational error.

Holding the original `IPart` was rejected because Lua can retain an object after the part has been removed. Re-resolving also keeps world access on the server thread.

### Returned objects capture the originating computer

CC:Tweaked can expose methods on arbitrary returned objects through `@LuaFunction`, but those methods do not automatically receive `IComputerAccess`. `getSide` therefore captures the calling computer access for later upgrade and pattern transfers. The object uses it only to resolve peripheral names visible to that computer.

### Device APIs use semantic capabilities

The implementation may share internal helpers, but the Lua surface follows the meaning of each AE2 device:

| Device | Public concept |
| --- | --- |
| Interface | Desired stock and current local contents |
| Import/Export Bus | Import or export filter |
| Storage Bus | External-storage partition filter |
| Formation Plane | World-placement filter |
| Storage Level Emitter | Monitored resource and threshold |
| Energy Level Emitter | Energy threshold |
| Pattern Provider | Real encoded-pattern inventory |

A generic `setConfig` was rejected because AE2 `CONFIG_STACKS` and `CONFIG_TYPES` inventories have incompatible amount semantics.

### Public resources initially support items and fluids

The API accepts `{ type = "item" | "fluid", name = registryId }`. Stock targets add `count`. Item counts remain item units; fluid counts are millibuckets. Fabric amounts are converted through the existing platform fluid divider.

Addon key types and exact tagged variants are deferred until UPW has a round-trippable representation. An ID-only setter always describes the default untagged variant.

### Active filter slots follow installed Capacity Cards

Import buses, export buses, storage buses, and formation planes expose `18 + 9 * capacityCards`, capped at 63. Reads and writes validate against the active count, not the 63-slot backing inventory.

Removing a Capacity Card clears filters that become inactive before completing the transfer. This matches AE2 menu cleanup and prevents hidden configuration from unexpectedly returning later. Failing card removal instead was considered safer for preservation but would differ from normal AE2 interaction.

### Mutations use AE2-owned inventories and managers

Configuration changes call `ConfigInventory.setStack`, settings use `IConfigManager`, priorities use `IPriorityHost`, and cards use `IUpgradeInventory`. Direct NBT mutation and GUI packet emulation are prohibited. This preserves AE2 listeners and loader behavior.

### The typed contract remains a separate additive source file

`integrations/ae2Objects.ts` is the source of the proposed TypeScript surface. It contains only interfaces and aliases, has no provider for returned objects, and does not modify the existing network API in `integrations/ae2.ts`. Generated `.d.ts` and `.lua` files remain build output.

## Risks / Trade-offs

- [Fabric export-bus crafting tracker has only nine entries] -> Verify whether the Fabric dependency can be upgraded; otherwise add a narrowly scoped compatibility correction and a regression test before enabling crafting-card scenarios beyond slot nine.
- [Returned objects retain computer access] -> Invalidate transfer operations after detach and never expose the captured object outside its originating Lua value.
- [Capacity-card removal clears ghost filters] -> Document and test the destructive configuration effect before moving the real card.
- [An unconfigured storage emitter sums heterogeneous raw AE amounts] -> Report its threshold unit as `ae_internal`; normalize only when an item or fluid key is selected.
- [A TypeScript union cannot automatically narrow from a method result] -> Keep `getDeviceType()` for Lua inspection and allow consumers to narrow or assert the concrete returned interface.
- [Fabric and Forge integrations are duplicated] -> Keep behavior and tests equivalent while retaining loader-specific AE2 imports and fluid conversion.

## Migration Plan

This is additive. Existing AE2 network methods and peripheral types remain unchanged. Add the new plugins and returned objects behind AE2 integration registration, add both-loader GameTests, then publish generated typed-peripheral declarations from `ae2Objects.ts`. Rollback consists of removing the new providers and typed module; no persisted data migration is needed because all state remains AE2-owned.

## Open Questions

- Whether Pattern Providers belong in the first implementation or a follow-up focused on real slot inventories.
- Whether exact tagged resources should later be copied from an inventory slot, resolved from an existing ME network key, or support both paths.
- Whether Drive and Chest priority-only plugins provide enough value for a follow-up.

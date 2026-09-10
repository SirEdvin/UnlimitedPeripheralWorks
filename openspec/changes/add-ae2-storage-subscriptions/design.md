## Context

See `proposal.md` for motivation. The dedicated ME network peripheral already owns a channel-requiring AE2 managed grid node. Wireless-terminal turtle and pocket upgrades instead resolve a linked grid and validate an active in-range access point on every operation; they intentionally retain no grid reference across calls.

AE2 15 exposes `IStorageWatcherNode` and `IStackWatcher`: a service registered on an AE2 grid node can watch all keys and receives each changed key with its current network amount. This is the native change-tracking mechanism. It is directly usable by the dedicated block's node, but wireless upgrades do not own a node on their linked grid. CC:Tweaked calls turtle and pocket upgrade update hooks every tick and exposes peripheral attach/detach lifecycle callbacks.

Tweakium's `PeripheralPluginUtils.itemQueryToPredicate` builds item predicates from Lua strings and tables but predicates are not serializable. Tweakium owner data storage already persists arbitrary upgrade NBT for turtles and pockets; the dedicated AE2 block needs equivalent subscription NBT in its block entity save data.

## Goals / Non-Goals

**Goals:**

- Use AE2's native storage watcher rather than scan the complete network each tick.
- Share validation, filter reconstruction, baselining, event formatting, and persistence behavior across stationary and wireless devices.
- Keep wireless observation active only while useful and preserve existing link, range, chunk-loading, and cleanup guarantees.
- Make corrupted persisted subscription data fail closed without preventing the peripheral or world from loading.

**Non-Goals:**

- Add subscriptions to arbitrary AE2 blocks accepted by the legacy compatibility provider.
- Persist amount snapshots or replay changes that happen while a peripheral is unavailable.
- Observe crafting, energy, channels, cell mounts, or addon-defined `AEKey` types.
- Add thresholds, debounce windows, event batching, subscription ownership per attached computer, or a configurable polling interval.

## Decisions

### Register AE2 storage-watcher services on owned observer nodes

The dedicated block adds one `IStorageWatcherNode` service to its existing managed node during node construction. `updateWatcher` baselines from the grid storage service's cached inventory and enables `setWatchAll(true)`; `onStackChange` forwards supported item/fluid keys and current amounts to the subscription tracker.

A wireless peripheral creates a private, non-in-world, zero-idle-power managed observer node only when it has at least one subscription and an attached computer. Its upgrade update hook first reuses the existing wireless-session validation, then creates a direct public `GridHelper.createConnection` from the observer node to the validated access point's grid node. The observer owns the same storage-watcher service as the stationary node, does not request a channel, and is destroyed on last detach, empty subscriptions, invalid link/range, peripheral removal, or observer replacement. Turtle updates recover the stored peripheral through `ITurtleAccess.getPeripheral(side)`; pocket updates use the peripheral argument supplied by CC:Tweaked.

This uses AE2's public watcher and connection APIs and reports individual mutations, unlike snapshot polling, which can miss transient changes and repeatedly scans large networks. Installing a watcher service onto AE2's access-point node was rejected because managed nodes expose no service-removal API and the mod must not mutate a foreign node permanently. Retaining a grid callback without an owned node was rejected because AE2 exposes no public standalone storage-listener registration.

The wireless observer is infrastructure only: no in-world exposure, no channel flag, no storage provider, no power consumption, and no chunk loading. The existing wireless resolver remains authoritative before creating or retaining the connection.

### Keep one serializable subscription definition and one runtime tracker

Use a small shared subscription definition containing `name`, `type`, and a serializable filter-source value. Build runtime predicates from definitions, but never persist predicate objects. A shared tracker owns definitions, compiled matchers, current key amounts, baseline state, deterministic listing, and attached event sinks. Stationary and wireless owners supply persistence and observer lifecycle hooks rather than separate subscription implementations.

Item filters retain their supported Lua source shape and are rebuilt through `PeripheralPluginUtils.itemQueryToPredicate`. Encode supported scalar/map source values as a tagged NBT tree so numeric table keys and nested boolean-composition tables round-trip without relying on `CompoundTag` string keys. Fluid filters store one validated registry-name string. Omitted filters use an explicit match-all source marker. Reject unsupported values before replacing an existing definition; while loading, skip malformed entries and mark the containing data dirty rather than throwing during world load.

Storing only generated predicates was rejected because predicates cannot survive restart. Inventing a second item-filter grammar was rejected because it would drift from `item_storage`. Java object serialization and a new JSON dependency were rejected; the existing NBT persistence boundary is sufficient.

### Baseline globally and filter each native callback locally

The tracker maintains current public amounts by `AEKey`. When AE2 provides a watcher or a wireless observer reconnects, copy supported item/fluid amounts from `IStorageService.cachedInventory`, mark the tracker baselined, and only then enable notifications. A callback compares its supplied current amount with the stored previous amount, updates the baseline, and emits once for each matching named subscription. Remove zero-valued keys after processing.

Replacing a subscription recompiles its matcher and uses the tracker's current amounts as its baseline, so replacement emits no synthetic event. Newly registered subscriptions likewise begin with current state. Clearing the baseline on disconnect or observer destruction guarantees that reconnect silently re-baselines instead of replaying an unknowable offline delta.

Maintaining a snapshot per subscription was rejected because the same network amounts would be duplicated for every filter. Persisting the global snapshot was rejected because it would produce misleading restart events and increase NBT size.

### Queue one stable event shape to every attached computer

Queue `ae2_storage_change` with positional arguments `(subscriptionName, resource, oldAmount, newAmount)`. Item resources reuse the detailed item-storage representation, remove its amount, and add `type = "item"`; fluid resources use `{ type = "fluid", name = <registry id> }`. Convert AE2 fluid units to public millibuckets with the existing helper. Ignore unsupported key types.

The subscription belongs to the peripheral's persisted state, not one computer. Every currently attached computer receives the event. Separate event names per subscription and batched payloads were rejected because one stable event is easier to type and one-resource events preserve AE2's native callback granularity.

### Scope methods through a dedicated subscription plugin

Add the subscription methods only through a plugin installed by `MENetworkBlockPlugin` when its entity is `MENetworkPeripheralBlockEntity`, and by `AE2WirelessTerminalPeripheral`. The existing provider continues wrapping arbitrary `AENetworkBlockEntity` instances with the old methods, but does not install this plugin for them.

This preserves compatibility without splitting the existing `ae2` peripheral type or adding checks to every existing method.

## Risks / Trade-offs

- [A virtual wireless observer node could remain connected after lifecycle loss] -> Destroy its connection and managed node on every detach, invalid resolver result, upgrade update mismatch, replacement, and removal path; GameTest cleanup and network-size behavior.
- [Creating an observer could accidentally consume a channel or power] -> Keep it non-in-world with no channel flag and zero idle power; verify channel information is unchanged while subscribed.
- [Watcher initialization could emit current inventory as changes] -> Baseline cached inventory before enabling watch-all and gate callbacks until baseline completion.
- [A malformed nested item query could corrupt upgrade or block loading] -> Use a bounded tagged decoder, validate through the existing query parser, skip invalid persisted entries, and never deserialize executable objects.
- [One resource can match many subscriptions and fan out many events] -> Preserve explicitly requested per-subscription/per-resource semantics; avoid extra copies beyond attached-computer queueing.
- [Fabric and Forge AE2 artifacts differ] -> Use only the shared AE2 15 public interfaces already present in both artifacts and run equivalent multi-loader GameTests.

## Migration Plan

This is additive. Existing blocks and equipped terminal NBT load without the subscription tag and behave as before. New definitions are written only after successful `subscribe` calls. Rollback ignores the unknown subscription tag while preserving terminal data; subscriptions become active again if the feature is restored.

No dependency, registry, recipe, or world-content migration is required.

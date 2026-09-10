## Why

AE2-backed peripherals can inspect storage only when Lua polls them, which makes responsive automation expensive and awkward. Named, persistent subscriptions let computers react to matching item and fluid amount changes while preserving the existing storage and crafting APIs.

## What Changes

- Add `subscribe(name, type, filter)` to the dedicated ME network peripheral and AE2 wireless-terminal turtle and pocket upgrades.
- Add subscription management methods to list and remove named subscriptions; subscribing with an existing name replaces it.
- Emit one CC:Tweaked event for each matching item or fluid resource whose stored amount changes, including the subscription name, resource identity, previous amount, and current amount.
- Reuse `item_storage` query semantics for item filters and registry-name matching for fluid filters.
- Persist each subscription's source filter data so predicates can be rebuilt after reload; re-baseline current network amounts without replaying changes that occurred while unavailable.
- Publish and test the matching typed peripheral contracts for stationary and wireless AE2 peripherals.

## Capabilities

### New Capabilities

- `ae2-storage-subscriptions`: Named, filtered, persistent AE2 item and fluid storage-change subscriptions and their CC:Tweaked event contract.

### Modified Capabilities

None.

## Impact

- Shared AE2 integration code for the dedicated ME network peripheral, wireless terminal upgrades, storage conversion, persistence, and lifecycle handling.
- CC:Tweaked Lua methods and event payloads exposed by `ae2` and `ae2_wireless_terminal` peripherals.
- AE2 native storage-watcher integration for the dedicated grid node and bounded snapshot comparison for wireless devices, which do not own an AE2 grid node.
- TypeScript contracts, TypeScript-to-Lua fixtures, and Fabric/Forge AE2 GameTests.
- No new dependency and no change to legacy arbitrary-AE2-block wrappers.

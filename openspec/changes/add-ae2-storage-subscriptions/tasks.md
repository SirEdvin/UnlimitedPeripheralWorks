## 1. Subscription Model and Persistence

- [x] 1.1 Add the shared named subscription definition, `item`/`fluid` validation, item-storage predicate reconstruction, deterministic listing, replacement, and removal behavior in the core AE2 integration.
- [x] 1.2 Add a bounded tagged-NBT codec for supported item-query source values and fluid names, rejecting unsupported Lua values before mutation and safely dropping malformed persisted entries on load.
- [x] 1.3 Add the shared runtime tracker for baseline state, supported AE2 keys, public item/fluid amounts, per-subscription matching, and individual `ae2_storage_change` event fan-out.

## 2. Native AE2 Observation

- [x] 2.1 Implement the shared `IStorageWatcherNode` service using `IStorageService.cachedInventory` for silent baselines and `IStackWatcher.setWatchAll(true)` for subsequent key changes.
- [x] 2.2 Extend the wireless-session resolver to expose the already-validated access-point grid node without retaining it across calls or loading chunks.
- [x] 2.3 Implement the zero-power, non-in-world wireless observer managed node and direct AE2 connection lifecycle, including cleanup on detach, empty subscriptions, invalid link/range, replacement, and removal.

## 3. Peripheral Integration

- [x] 3.1 Persist subscription definitions in `MENetworkPeripheralBlockEntity` NBT and register the watcher service on its existing channel-requiring main node without changing channel or power behavior.
- [x] 3.2 Add a dedicated subscription plugin to `MENetworkBlockPlugin` only for `MENetworkPeripheralBlockEntity`, exposing `subscribe`, `unsubscribe`, and `getSubscriptions` while leaving arbitrary AE2 block wrappers unchanged.
- [x] 3.3 Add the subscription plugin and persisted owner-data integration to `AE2WirelessTerminalPeripheral`, preserving the terminal stack tag and all existing storage, crafting, fuel, security, and range behavior.
- [x] 3.4 Wire turtle and pocket upgrade tick hooks to maintain or destroy the wireless observer through their live peripheral instances.

## 4. Contracts and Regression Coverage

- [x] 4.1 Extend the stationary and wireless TypeScript contracts with subscription definitions, method signatures, and the `ae2_storage_change` event payload types; update TypeScript-to-Lua fixtures.
- [x] 4.2 Add dedicated-peripheral GameTest coverage for item and fluid filters, match-all filters, individual event payloads, insert/remove amounts, multiple matching subscriptions, replacement, unsubscribe results, deterministic listing, persistence, silent re-baselining, and legacy-wrapper method scope.
- [x] 4.3 Extend wireless turtle and pocket GameTests for native observer attachment and cleanup, persisted filter restoration, range-loss re-baselining without replay, event delivery after recovery, unchanged terminal NBT, and unchanged AE2 channel use.
- [x] 4.4 Add corrupt-subscription-NBT regression coverage proving invalid definitions are discarded without preventing block, turtle, or pocket peripheral loading.

## 5. Verification

- [x] 5.1 Run `:typescript-tests:compileTestLua --no-daemon` and fix contract or fixture failures.
- [x] 5.2 Run the root AE2-enabled Fabric and Forge GameTests with `xvfb-run`, `--no-daemon`, and the minimal test environment; verify observer cleanup and all subscription scenarios on both loaders.
- [x] 5.3 Run the timed root `build --no-daemon`, inspect relevant failure windows if needed, and confirm `git diff --check` passes without generated output or logs entering the change.

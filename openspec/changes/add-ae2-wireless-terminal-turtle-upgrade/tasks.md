## 1. AE2 Storage And Wireless Resolution

- [x] 1.1 Generalize the Fabric and Forge `AEItemStorage` adapters to accept a caller-supplied `IActionSource` and change callback while preserving stationary ME block behavior.
- [x] 1.2 Add equivalent Fabric and Forge wireless resolvers that reconstruct the stored terminal, resolve its loaded linked access point and current grid, and select an active same-level access point containing the turtle in range without loading chunks.
- [x] 1.3 Return distinct operational errors for invalid stored state, unavailable linked network, and out-of-range access, and ensure each peripheral method resolves a fresh session.

## 2. Turtle Upgrade State And Registration

- [x] 2.1 Implement the AE2 wireless terminal turtle upgrade and peripheral in both loader integration source sets using a `TurtlePeripheralOwner` with an attached `FuelBoon`.
- [x] 2.2 Persist the complete equipped Wireless Terminal stack in authoritative side upgrade data and reconstruct it unchanged on unequip, while rejecting unlinked terminals and non-standard terminal variants.
- [x] 2.3 Register the optional `ae2_wireless_terminal` serializer, generated turtle upgrade data, scaled upward-facing terminal model, and English and Ukrainian language entries through the existing AE2 integration hooks.

## 3. Lua Item API

- [x] 3.1 Implement `items(detailed?, filter?)` with the regular item-storage representations and query semantics while excluding fluids and addon keys.
- [x] 3.2 Implement `pullItem(itemQuery?, limit?, toSlot?)` from AE2 into the turtle inventory with one-based slot validation and the configured item-storage transfer limit.
- [x] 3.3 Implement `pushItem(fromSlotOrItemQuery?, limit?)` from the turtle inventory into AE2 with one-based slot validation and the configured item-storage transfer limit.
- [x] 3.4 Use one owner-derived player `IActionSource` for transfer simulation and mutation, consume one base fuel after connection and argument validation for every transfer call including valid zero-move calls, and leave terminal AE charge unchanged.

## 4. Typed Peripheral Contract

- [x] 4.1 Create `projects/typed-peripheral-unlimitedperipheralworks/integrations/ae2WirelessTerminal.ts` with item listing overloads, implicit-turtle transfer signatures, optional one-based slot parameters, inherited `FuelApi`, and an `ae2_wireless_terminal` provider.
- [x] 4.2 Add a TypeScript fixture that resolves the provider and type-checks detailed/base listing plus push, pull, slot, and fuel calls.
- [x] 4.3 Run `./gradlew :typescript-tests:compileTestLua --no-daemon` with the required timeout and log capture, and verify generated declarations remain build output.

## 5. Multi-Loader GameTests

- [x] 5.1 Add equivalent Fabric and Forge GameTests that equip a linked terminal and verify link, charge, Energy Cards, name, and arbitrary NBT survive save/load and unequip.
- [x] 5.2 Test in-range listing, filtered representations, both transfer directions, explicit turtle slots, transfer limits, and owner-attributed AE2 simulation and mutation.
- [x] 5.3 Test one-fuel-per-call behavior for successful and valid zero-move transfers, insufficient and disabled fuel, free read calls, and no fuel loss on connection or slot validation errors.
- [x] 5.4 Test missing or unloaded linked access points, unavailable grids, inactive and out-of-range access points, turtle movement between calls, same-dimension enforcement, no chunk loading, and unchanged terminal AE charge.

## 6. Verification

- [x] 6.1 Run formatting and targeted compile checks for both loaders and fix all failures.
- [x] 6.2 Run the root `gameTest` task for Fabric and Forge under `xvfb-run` with the required timeout and log capture, and verify all existing and new GameTests pass.
- [x] 6.3 Run the timed root `build --no-daemon` with complete log capture and verify the multi-loader build and typed-peripheral outputs pass.

## 7. Crafting Job Tracking

- [x] 7.1 Return AE2 crafting-link UUIDs from successful stationary and crafting-monitor `scheduleCrafting` calls while preserving existing response ordering and errors.
- [x] 7.2 Extract one shared stationary/turtle crafting-job peripheral plugin with injected context, weakly cached links, lookup, listing, cancellation, terminal-state, and explicit missing-job responses.
- [x] 7.3 Register a separate `ae2_crafting_monitor` upgrade using a linked Wireless Crafting Terminal and remove crafting methods from the regular wireless terminal.
- [x] 7.4 Publish separate crafting-monitor typings and exercise runtime missing-job responses through the crafting monitor.
- [x] 7.5 Run datagen, strict OpenSpec validation, the minimal multi-loader GameTests, and the timed root build.

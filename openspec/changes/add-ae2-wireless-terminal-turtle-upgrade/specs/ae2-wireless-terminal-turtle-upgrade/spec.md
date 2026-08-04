## ADDED Requirements

### Requirement: Equip a linked AE2 Wireless Terminal directly
The system SHALL register the standard AE2 Wireless Terminal as the crafting item for a turtle peripheral upgrade with peripheral type `ae2_wireless_terminal`. The system SHALL accept only a terminal containing a valid AE2 access-point link and SHALL NOT require a separate UPW item or crafting recipe.

#### Scenario: Equip a linked terminal
- **WHEN** a turtle equips a standard AE2 Wireless Terminal containing an access-point link
- **THEN** the turtle gains an `ae2_wireless_terminal` peripheral on that side

#### Scenario: Reject an unlinked terminal
- **WHEN** a turtle attempts to equip a standard AE2 Wireless Terminal without an access-point link
- **THEN** the item is not accepted as the wireless terminal upgrade

#### Scenario: Do not accept other terminal variants
- **WHEN** a turtle attempts to equip an AE2 Wireless Crafting Terminal or another terminal variant
- **THEN** the item is not accepted as this upgrade

### Requirement: Preserve the complete terminal item state
The system SHALL copy the equipped terminal's complete item state into persistent turtle upgrade data and SHALL reconstruct that state when the upgrade is unequipped. Peripheral use SHALL NOT consume or modify the terminal's AE charge.

#### Scenario: Terminal survives an equip round trip
- **WHEN** a linked, charged, named terminal with installed Energy Cards is equipped, saved, loaded, and unequipped
- **THEN** the returned terminal retains its link, charge, name, upgrades, and all other item NBT

#### Scenario: Peripheral calls leave terminal charge unchanged
- **WHEN** the turtle lists or transfers items through the upgrade
- **THEN** the terminal's stored AE charge remains unchanged

### Requirement: Resolve a live vanilla wireless connection for every call
The system SHALL resolve the terminal's linked access point and AE2 grid on every peripheral call, then require an active wireless access point from that grid in the turtle's current dimension and within that access point's range. Resolution SHALL NOT load chunks and SHALL NOT retain a grid or storage reference across calls.

#### Scenario: Use an active access point in range
- **WHEN** the linked network is loaded and has an active access point whose range contains the turtle
- **THEN** the peripheral call operates on that network

#### Scenario: Linked access point is unavailable
- **WHEN** the terminal is unlinked, the linked access point is missing or unloaded, or its grid is unavailable
- **THEN** the call fails with an operational error and does not load the access point's chunk

#### Scenario: Turtle is outside wireless coverage
- **WHEN** no active access point from the linked grid is in the turtle's dimension and range
- **THEN** the call fails with an out-of-range operational error

#### Scenario: Turtle moves between calls
- **WHEN** the turtle moves out of range after a successful call
- **THEN** the next call revalidates its position and fails rather than using a cached connection

### Requirement: List network items with item-storage query semantics
The peripheral SHALL expose `items(detailed?, filter?)` with the same detailed/base representations and item-query matching used by the regular `item_storage` API. Listing SHALL include only item keys and SHALL NOT include AE2 fluids or addon key types.

#### Scenario: List detailed items
- **WHEN** Lua calls `items()` or `items(true)` while connected
- **THEN** the peripheral returns the matching network items using detailed item representations

#### Scenario: List filtered base items
- **WHEN** Lua calls `items(false, filter)` while connected
- **THEN** the peripheral returns only matching network items using base item representations

### Requirement: Push network items into the turtle inventory
The peripheral SHALL expose `pullItem(itemQuery?, limit?, toSlot?)`, which moves matching items from the connected AE2 network into the owning turtle's 16-slot inventory. `toSlot`, when present, SHALL use one-based turtle slot numbering. The operation SHALL respect the configured item-storage transfer limit.

#### Scenario: Pull into any turtle slot
- **WHEN** Lua calls `pullItem(query, limit)` with matching network items and available turtle capacity
- **THEN** up to the effective limit is extracted from AE2 and inserted into the turtle inventory and the moved count is returned

#### Scenario: Pull into a selected turtle slot
- **WHEN** Lua calls `pullItem(query, limit, toSlot)` with a valid compatible destination slot
- **THEN** items are inserted only into that turtle slot and the moved count is returned

#### Scenario: Reject an invalid destination slot
- **WHEN** `toSlot` is outside the inclusive range 1 through 16
- **THEN** the call fails without moving items or consuming fuel

### Requirement: Pull turtle items into the network
The peripheral SHALL expose `pushItem(fromSlotOrItemQuery?, limit?)`, which moves items from the owning turtle's inventory into the connected AE2 network. A numeric first argument SHALL select a one-based turtle source slot; a string or table SHALL use the regular item-query semantics across the turtle inventory. The operation SHALL respect the configured item-storage transfer limit.

#### Scenario: Push from any turtle slot
- **WHEN** Lua calls `pushItem(query, limit)` with matching turtle items and available AE2 capacity
- **THEN** up to the effective limit is removed from the turtle inventory, inserted into AE2, and the moved count is returned

#### Scenario: Push from a selected turtle slot
- **WHEN** Lua calls `pushItem(fromSlot, limit)` with a valid source slot
- **THEN** only items from that turtle slot are offered to AE2 and the moved count is returned

#### Scenario: Reject an invalid source slot
- **WHEN** `fromSlot` is outside the inclusive range 1 through 16
- **THEN** the call fails without moving items or consuming fuel

### Requirement: Charge turtle fuel per valid transfer call
The peripheral SHALL use a Tweakium `FuelBoon` and consume one base turtle fuel for each connected, validated `pushItem` or `pullItem` operation. Fuel SHALL be charged per call regardless of the number of items moved, including when zero items move after validation. Read-only calls and calls rejected before transfer execution SHALL consume no fuel. Turtles configured with fuel disabled SHALL execute without fuel consumption.

#### Scenario: Successful transfer consumes fuel
- **WHEN** a connected turtle with fuel calls `pushItem` or `pullItem`
- **THEN** one base fuel, adjusted by the configured fuel consumption rate, is consumed

#### Scenario: Valid no-op transfer consumes fuel
- **WHEN** a connected turtle calls a transfer method but no item matches or the destination accepts nothing
- **THEN** the method returns zero and consumes one base fuel

#### Scenario: Insufficient fuel prevents transfer
- **WHEN** a fuel-enabled turtle lacks the required fuel for a transfer operation
- **THEN** the call fails and no items move

#### Scenario: Connection failure does not consume fuel
- **WHEN** a transfer call fails link, grid, dimension, range, or slot validation
- **THEN** no turtle fuel is consumed

### Requirement: Attribute AE2 mutations to the turtle owner
The system SHALL perform AE2 insertion and extraction with a player action source derived from the turtle's owning-player fake player, following the existing Tweakium turtle-owner mechanism.

#### Scenario: Transfer uses turtle owner context
- **WHEN** a turtle transfers items with AE2
- **THEN** both simulated and committed AE2 storage operations use the turtle owner's action source

### Requirement: Publish a typed peripheral contract
The typed project SHALL define the source contract in `projects/typed-peripheral-unlimitedperipheralworks/integrations/ae2WirelessTerminal.ts`. The contract SHALL declare the `ae2_wireless_terminal` provider, item listing overloads, implicit-turtle `pushItem` and `pullItem` signatures, one-based optional slot parameters, numeric moved counts, crafting-job methods, and inherited fuel methods. The stationary AE2 contract SHALL expose the same job type, IDs, lookup, listing, and cancellation methods. Generated `.d.ts` and `.lua` files SHALL remain build output.

#### Scenario: Compile the typed contract
- **WHEN** the typed-peripheral project is built
- **THEN** the new source compiles and produces declarations matching the runtime Lua surface

#### Scenario: Resolve the peripheral provider
- **WHEN** a TypeScript consumer uses the exported wireless terminal provider
- **THEN** it resolves peripherals whose runtime type is `ae2_wireless_terminal`

### Requirement: Request and weakly track AE2 crafting jobs
Successful `scheduleCrafting` calls SHALL retain the existing leading `true` result and additionally return the submitted AE2 crafting-link UUID. The server process SHALL weakly track submitted links per AE2 crafting service and expose `getCraftingJob`, `getCraftingJobs`, and `cancelCrafting` on stationary and wireless AE2 peripherals without retaining links or grids solely for tracking.

#### Scenario: Request a tracked crafting job
- **WHEN** AE2 accepts a crafting request
- **THEN** the call returns `true` and a job ID that can be queried or canceled while the weak link remains available

#### Scenario: Query a tracked crafting job
- **WHEN** Lua queries a known job ID on the same AE2 network
- **THEN** it receives the ID, target, requested amount, and `running`, `done`, or `canceled` state

#### Scenario: Cancel a running job
- **WHEN** Lua cancels a known running job
- **THEN** the AE2 crafting link is canceled and the call returns true

#### Scenario: Handle a missing job
- **WHEN** an ID is unknown, belongs to another network, or its weakly cached link has been reclaimed
- **THEN** lookup and cancellation return `nil` and a not-found error without throwing

### Requirement: Keep item storage local-dimensional
The first version's storage API SHALL NOT expose fluids, terminal user-interface settings, cross-dimensional access, or Wireless Crafting Terminal support. Crafting requests MAY target AE2 item or fluid patterns through the existing mode parameter.

#### Scenario: Network contains non-item keys
- **WHEN** the connected AE2 network contains fluids or addon-defined keys
- **THEN** `items()` omits those keys and transfer methods operate only on items

## ADDED Requirements

### Requirement: Full-block device API
The system SHALL expose the device-specific API directly on a supported full-block ME Interface or Pattern Provider without requiring a device or side argument. The method surface SHALL match the corresponding multipart object except for controls that only exist on the full block.

#### Scenario: Configure a full-block interface
- **WHEN** a computer calls `setStock` on an attached ME Interface block
- **THEN** the interface updates that stock target without requiring a device selector

#### Scenario: Configure a full-block pattern provider direction
- **WHEN** a computer calls `setPushDirection` on an attached Pattern Provider block
- **THEN** the block updates its output direction through AE2's normal state mutation

### Requirement: Multipart side lookup
The system SHALL expose `getSide(side)` on an AE2 cable peripheral for the six Minecraft directions. It SHALL return a Lua object for a supported part or `nil, error` for an empty or unsupported side.

#### Scenario: Resolve an export bus
- **WHEN** a cable has an Export Bus on its north side and a computer calls `getSide("north")`
- **THEN** the call returns an object exposing the Export Bus API

#### Scenario: Resolve an empty side
- **WHEN** a cable has no part on its south side and a computer calls `getSide("south")`
- **THEN** the call returns `nil` and a descriptive error

#### Scenario: Reject an invalid direction
- **WHEN** a computer calls `getSide` with a value outside north, south, east, west, up, and down
- **THEN** the call raises a Lua argument error

### Requirement: Side-object lifecycle
A returned side object SHALL re-resolve the cable part by level, block position, and side for every world operation. It SHALL operate on a replacement part of the same kind and SHALL fail if the part is removed or replaced by another kind.

#### Scenario: Same-kind replacement
- **WHEN** an Export Bus is replaced by another Export Bus after its side object was obtained
- **THEN** subsequent object calls operate on the replacement Export Bus

#### Scenario: Different-kind replacement
- **WHEN** an Export Bus is replaced by an Import Bus after its side object was obtained
- **THEN** subsequent Export Bus object calls fail without mutating the Import Bus

### Requirement: Resource representation
The system SHALL accept built-in item and fluid resources using a type and registry name. Item amounts SHALL use item counts and fluid amounts SHALL use millibuckets on both loaders. Unsupported key types, unknown IDs, and invalid amounts SHALL raise Lua errors.

#### Scenario: Configure fluid stock on Fabric
- **WHEN** a computer configures 1000 mB of water on a Fabric ME Interface
- **THEN** the system stores the loader-correct AE2 amount representing one bucket

#### Scenario: Reject an addon key type
- **WHEN** a computer supplies a resource type other than item or fluid
- **THEN** the operation fails without changing the AE2 configuration

### Requirement: Upgrade inventory access
Every supported device with a non-empty AE2 upgrade inventory SHALL expose its physical slot count, sparse slot contents, slot detail, and `pullUpgrade` and `pushUpgrade` transfers. Transfers SHALL move real items, honor AE2 card and slot limits, use one-based Lua slots, and resolve inventories through the originating computer.

#### Scenario: Insert a valid card
- **WHEN** `pullUpgrade` targets an empty compatible upgrade slot and the source inventory contains a valid card
- **THEN** the card moves into the AE2 upgrade inventory and the method returns the moved count

#### Scenario: Reject an invalid card atomically
- **WHEN** `pullUpgrade` supplies a card that the target device does not accept
- **THEN** the method returns zero or raises the established transfer error and leaves the source inventory unchanged

#### Scenario: Extract through a returned object
- **WHEN** `pushUpgrade` is called on a side object with a destination peripheral visible to the originating computer
- **THEN** the card moves to that destination using the captured computer access

### Requirement: Capacity-controlled filters
Import Buses, Export Buses, Storage Buses, and Formation Planes SHALL expose 18 active filter slots plus nine per installed Capacity Card, capped at 63. Filter methods SHALL address only active one-based slots and SHALL represent filters without amounts.

#### Scenario: Capacity Card expands filters
- **WHEN** a device has two installed Capacity Cards
- **THEN** `getFilterSlotCount` returns 36 and slot 36 is configurable

#### Scenario: Reject an amount on a filter
- **WHEN** a caller supplies an amount to a type-only filter operation
- **THEN** the operation fails rather than silently discarding the amount

#### Scenario: Remove a Capacity Card
- **WHEN** removing a Capacity Card reduces the active filter range
- **THEN** filters in newly inactive slots are cleared before the card transfer completes

### Requirement: ME Interface stock control
An ME Interface block or part SHALL expose nine stock rows. Each configured row SHALL contain a resource and positive target amount, and read methods SHALL report both desired target and current local stored contents when present.

#### Scenario: Increase an item stock target
- **WHEN** a computer sets an iron-ingot target of 32 on row one
- **THEN** AE2 saves the configuration and replans local stock toward 32 items

#### Scenario: Clear a stock target
- **WHEN** a computer calls `clearStock` for a configured row
- **THEN** AE2 clears the target and replans remaining local contents back into network storage

#### Scenario: Reject an excessive target
- **WHEN** a target exceeds the resource capacity accepted by the interface row
- **THEN** the operation fails or reports the normalized accepted value instead of silently presenting the requested value as stored

### Requirement: Import Bus control
An Import Bus object SHALL expose its import filter, fuzzy mode, redstone mode, and upgrade inventory. An installed Inverter Card SHALL invert filter behavior according to AE2 rules.

#### Scenario: Set an import filter
- **WHEN** a computer configures water in an active Import Bus filter slot
- **THEN** AE2 rebuilds the import partition filter and wakes or sleeps the bus according to its redstone state

### Requirement: Export Bus control
An Export Bus object SHALL expose its export filter, fuzzy mode, redstone mode, craft-only setting, scheduling mode, and upgrade inventory. Filter entries SHALL select resource types but SHALL NOT specify an export quantity.

#### Scenario: Set round-robin export
- **WHEN** a computer sets scheduling mode to `round_robin`
- **THEN** AE2 uses its round-robin configured-slot behavior

#### Scenario: Enable craft-only mode
- **WHEN** a computer enables craft-only mode and a Crafting Card is installed
- **THEN** the Export Bus requests configured resources through AE2 crafting rather than extracting stored resources

### Requirement: Storage Bus control
A Storage Bus object SHALL expose its partition filter, fuzzy mode, access mode, storage-filter mode, filter-on-extract setting, priority, and upgrade inventory. Mutations SHALL request the AE2 storage remount needed to apply them.

#### Scenario: Change storage access
- **WHEN** a computer changes a Storage Bus from `read_write` to `read`
- **THEN** AE2 remounts the external storage with extraction-only access

#### Scenario: Change priority
- **WHEN** a computer changes the Storage Bus priority
- **THEN** AE2 saves the priority and requests a storage remount

### Requirement: Formation Plane control
A Formation Plane object SHALL expose its placement filter, fuzzy mode, block-placement setting, priority, and upgrade inventory. Filter and upgrade changes SHALL rebuild its partition behavior through AE2.

#### Scenario: Configure dropped-item behavior
- **WHEN** a computer disables block placement
- **THEN** the Formation Plane uses AE2's dropped-item behavior for placeable resources

### Requirement: Storage Level Emitter control
A Storage Level Emitter object SHALL expose one optional monitored resource, threshold, threshold unit, fuzzy mode, craft-via-redstone setting, emitter mode, output state, and upgrade inventory. Item thresholds SHALL use item counts, configured fluid thresholds SHALL use millibuckets, and an unconfigured heterogeneous threshold SHALL report `ae_internal`.

#### Scenario: Monitor a fluid threshold
- **WHEN** a computer selects water and sets a threshold of 4000
- **THEN** the emitter compares against four buckets using loader-correct internal units

#### Scenario: Use a Crafting Card
- **WHEN** a Crafting Card is installed
- **THEN** emitter output reflects whether AE2 is requesting the selected resource, or any resource when none is selected

### Requirement: Energy Level Emitter control
An Energy Level Emitter object SHALL expose its AE energy threshold, emitter mode, and current redstone output. It SHALL NOT expose upgrade methods because it has no physical upgrade slots.

#### Scenario: Change energy threshold
- **WHEN** a computer changes the energy threshold
- **THEN** AE2 reinstalls or updates its energy watcher and refreshes emitter output

### Requirement: Pattern Provider control
A Pattern Provider block or part SHALL expose its nine real encoded-pattern slots, priority, blocking setting, Pattern Access Terminal visibility, and crafting lock mode. Pattern transfers SHALL move real validated encoded-pattern items. A full block SHALL report and accept its six directional push states plus AE2's default `all` state.

#### Scenario: Insert an encoded pattern
- **WHEN** `pullPattern` selects a valid encoded pattern from another inventory
- **THEN** the item moves into the requested provider slot and AE2 refreshes advertised patterns

#### Scenario: Reject a non-pattern item
- **WHEN** `pullPattern` selects an item that AE2 cannot decode as a pattern
- **THEN** the item remains in the source inventory and the provider does not advertise it

### Requirement: AE2-owned mutation callbacks
All mutations SHALL execute on the server thread through AE2 inventories, configuration managers, priority hosts, and block-state APIs. The implementation SHALL NOT mutate AE2 NBT directly or emulate GUI packets.

#### Scenario: Interface callback execution
- **WHEN** a stock target changes through Lua
- **THEN** AE2 saves the host, recalculates its stock plan, and notifies neighbors through its normal callback

#### Scenario: Storage callback execution
- **WHEN** a Storage Bus filter changes through Lua
- **THEN** AE2 invalidates the old partition and remounts storage through its normal callback path

### Requirement: Typed API contract
The runtime method names, arguments, returned structures, and enum values SHALL match `projects/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects.ts`. Returned object interfaces SHALL be `@noSelf` types and SHALL NOT declare independent peripheral providers.

#### Scenario: Compile typed contract
- **WHEN** the typed-peripheral project is compiled with TypeScriptToLua
- **THEN** `ae2Objects.ts` compiles without type errors or runtime provider declarations

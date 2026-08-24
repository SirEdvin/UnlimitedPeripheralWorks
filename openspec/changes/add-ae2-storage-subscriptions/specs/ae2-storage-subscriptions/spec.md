## Purpose

Provide persistent, named AE2 storage subscriptions so CC:Tweaked programs can react to filtered item and fluid amount changes without repeatedly polling the network.

## ADDED Requirements

### Requirement: Subscription API is available on supported AE2 peripherals
The dedicated ME network peripheral and AE2 wireless-terminal turtle and pocket upgrades SHALL expose `subscribe(name, type, filter)`, `unsubscribe(name)`, and `getSubscriptions()` methods. Legacy `ae2` plugins attached to other AE2 blocks SHALL retain their existing API without these methods.

#### Scenario: Dedicated peripheral exposes subscriptions
- **WHEN** a computer attaches to the dedicated ME network peripheral
- **THEN** its `ae2` peripheral exposes all three subscription-management methods

#### Scenario: Wireless upgrades expose subscriptions
- **WHEN** a linked AE2 wireless terminal is equipped as a turtle or pocket upgrade
- **THEN** its `ae2_wireless_terminal` peripheral exposes all three subscription-management methods

#### Scenario: Legacy wrapper remains unchanged
- **WHEN** a computer wraps an AE2 block other than the dedicated ME network peripheral through the compatibility provider
- **THEN** that block does not gain storage-subscription methods

### Requirement: Named subscriptions are validated and replaceable
`subscribe(name, type, filter)` SHALL require a non-empty name, accept only `item` or `fluid` as the type, validate the filter before changing persistent state, and replace any existing subscription with the same name. A successful registration or replacement SHALL establish the current matching amounts as its baseline without emitting an event.

#### Scenario: Add a named subscription
- **WHEN** Lua subscribes with a new valid name, type, and filter
- **THEN** the subscription is stored and appears in `getSubscriptions()` with its source filter data

#### Scenario: Replace a named subscription
- **WHEN** Lua subscribes with a name that already exists
- **THEN** the old type and filter are atomically replaced and the replacement starts from a fresh baseline

#### Scenario: Reject invalid input without partial replacement
- **WHEN** Lua supplies an empty name, unsupported type, or invalid filter for an existing subscription name
- **THEN** the call fails and the existing subscription remains unchanged

### Requirement: Item subscriptions use item-storage query semantics
An `item` subscription SHALL accept the same string and table query forms as the regular `item_storage` API, including its supported name, display-name, tag, NBT, boolean-composition, and alias semantics. An omitted filter SHALL match every AE2 item key. Item subscriptions SHALL ignore fluid and addon-defined AE2 keys.

#### Scenario: Match an item query
- **WHEN** an item's network amount changes and its item stack matches the subscription's item-storage query
- **THEN** the system emits that subscription's storage-change event for the item

#### Scenario: Ignore a non-matching item
- **WHEN** an item's network amount changes but its item stack does not match the subscription query
- **THEN** the system emits no event for that subscription and change

#### Scenario: Match all items
- **WHEN** an item subscription omits its filter and any AE2 item amount changes
- **THEN** the system emits an event for that item change

### Requirement: Fluid subscriptions use registry names
A `fluid` subscription SHALL accept a fluid registry name string as its filter and SHALL reject unknown or malformed fluid names. An omitted filter SHALL match every AE2 fluid key. Fluid subscriptions SHALL ignore item and addon-defined AE2 keys.

#### Scenario: Match a named fluid
- **WHEN** the amount of the fluid named by a fluid subscription changes
- **THEN** the system emits that subscription's storage-change event for the fluid

#### Scenario: Ignore another fluid
- **WHEN** a different fluid's network amount changes
- **THEN** the system emits no event for the named-fluid subscription

#### Scenario: Match all fluids
- **WHEN** a fluid subscription omits its filter and any AE2 fluid amount changes
- **THEN** the system emits an event for that fluid change

### Requirement: Each matching resource change emits an individual event
For each matching subscription and changed AE2 resource, the system SHALL queue one `ae2_storage_change` event to every computer currently attached to that peripheral. Event arguments SHALL be the subscription name, a resource table, the previous public amount, and the current public amount. Item resource tables SHALL use the detailed item-storage representation with `type = "item"` and without an embedded amount; fluid resource tables SHALL contain `type = "fluid"` and the fluid registry `name`. Item amounts SHALL use item counts and fluid amounts SHALL use millibuckets.

#### Scenario: Existing resource amount changes
- **WHEN** a matching resource changes from one non-zero amount to another
- **THEN** each attached computer receives `ae2_storage_change`, the subscription name, the resource, the old amount, and the new amount

#### Scenario: Resource is inserted or removed
- **WHEN** a matching resource appears in or disappears from AE2 storage
- **THEN** the event reports zero as the previous amount for insertion or as the current amount for removal

#### Scenario: Resource matches multiple subscriptions
- **WHEN** one resource change matches multiple differently named subscriptions
- **THEN** each attached computer receives one event for each matching subscription

#### Scenario: Multiple resources change
- **WHEN** one operation changes multiple matching resources
- **THEN** each resource change is emitted as an individual event rather than a combined batch

### Requirement: Subscription definitions persist while baselines do not
The system SHALL persist each subscription's name, type, and serializable source filter data in the dedicated block or equipped upgrade data and SHALL rebuild runtime predicates from that source after reload. Runtime amount baselines SHALL NOT be persisted. After server restart, peripheral recreation, network reconnection, wireless range recovery, or first attachment, the system SHALL silently baseline current amounts before emitting later changes.

#### Scenario: Restore an item predicate after restart
- **WHEN** the server reloads a peripheral containing a persisted item subscription
- **THEN** the system rebuilds the item-storage predicate from its stored source query and retains the subscription in `getSubscriptions()`

#### Scenario: Reconnect without replay
- **WHEN** a subscribed peripheral becomes available after restart, disconnection, or wireless range loss
- **THEN** it records current matching amounts without emitting changes that occurred while unavailable

#### Scenario: Emit after re-baselining
- **WHEN** a matching amount changes after the restored peripheral has established its baseline
- **THEN** the system emits the change using the re-baselined amount as the previous amount

### Requirement: Subscription management is persistent and observable
`unsubscribe(name)` SHALL remove the named subscription and return whether one existed. `getSubscriptions()` SHALL return every current subscription's name, type, and source filter in deterministic name order. Removing a subscription SHALL stop future events for that name without affecting other subscriptions.

#### Scenario: Remove an existing subscription
- **WHEN** Lua unsubscribes an existing name
- **THEN** the method returns true, the definition is removed from persistent state, and later changes produce no event for that name

#### Scenario: Remove an unknown subscription
- **WHEN** Lua unsubscribes a name that does not exist
- **THEN** the method returns false and existing subscriptions remain unchanged

#### Scenario: List subscriptions
- **WHEN** Lua calls `getSubscriptions()`
- **THEN** it receives all definitions sorted by name with filters represented in their original supported Lua shape

### Requirement: Existing AE2 peripheral behavior remains compatible
Storage subscriptions SHALL be additive and SHALL NOT change existing storage listing, transfer, crafting, fuel, wireless range, security attribution, channel use, or terminal item persistence behavior.

#### Scenario: Existing API call
- **WHEN** Lua uses an existing method on a subscribed dedicated or wireless AE2 peripheral
- **THEN** the method preserves its prior arguments, results, validation, and side effects

#### Scenario: Wireless observer lifecycle
- **WHEN** a wireless peripheral has no attached computer, no subscriptions, loses its valid link or range, or is removed
- **THEN** subscription observation releases its AE2 connection and does not retain or load the linked network

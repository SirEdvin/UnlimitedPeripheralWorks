## ADDED Requirements

### Requirement: Dedicated ME network peripheral
The system SHALL provide a dedicated `me_network_peripheral` block whose CC:Tweaked peripheral type is `ae2`.

#### Scenario: Computer wraps the dedicated block
- **WHEN** a computer or wired modem accesses a placed ME network peripheral
- **THEN** it discovers an `ae2` peripheral on that block

#### Scenario: Computer wraps another AE2 block
- **WHEN** a computer or wired modem accesses an AE2 network block other than the dedicated ME network peripheral
- **THEN** the system does not add the `ae2` peripheral plugin to that block

### Requirement: AE2 network participation
The ME network peripheral SHALL own an AE2 in-world grid node, require one AE2 channel, expose a smart-cable connection on every face, and cleanly create and destroy its node with the block entity lifecycle.

#### Scenario: Active network connection
- **WHEN** the ME network peripheral is connected to a powered AE2 network with an available channel
- **THEN** its grid node becomes active and its peripheral operations address that AE2 network

#### Scenario: Channel unavailable
- **WHEN** the connected AE2 network cannot allocate a channel to the ME network peripheral
- **THEN** the block remains unavailable for network-dependent operations

#### Scenario: Block unload or removal
- **WHEN** the ME network peripheral unloads or is removed
- **THEN** its managed AE2 node is destroyed without leaving a grid node or channel allocation behind

### Requirement: Existing Lua interface compatibility
The dedicated block SHALL expose the existing `getAverageEnergyDemand`, `getAverageEnergyIncome`, `getChannelEnergyDemand`, `getChannelInformation`, `getCraftingCPUs`, `getCraftableItems`, `getCraftableFluids`, `getPatternsFor`, `getActiveCraftings`, and `scheduleCrafting` methods with their existing arguments and return semantics.

#### Scenario: Connected API call
- **WHEN** a Lua program calls an existing `ae2` method on an active ME network peripheral
- **THEN** the method returns data or performs the action against the block's connected AE2 grid

#### Scenario: Disconnected API call
- **WHEN** a Lua program calls an existing `ae2` method while the block has no active AE2 grid
- **THEN** the method preserves its existing disconnected result or error behavior and does not crash the server

#### Scenario: Crafting action source
- **WHEN** `scheduleCrafting` submits a valid crafting job
- **THEN** AE2 attributes the action to the dedicated ME network peripheral's grid node

### Requirement: Existing storage integrations remain independent
The system SHALL continue to expose the existing generic AE2 item, fluid, and energy storage adapters independently from the dedicated Lua peripheral.

#### Scenario: Storage access on another AE2 block
- **WHEN** a supported storage consumer accesses an eligible AE2 network block and AE2 storage integrations are enabled
- **THEN** item, fluid, and energy storage access behaves as it did before this change

### Requirement: Optional integration availability
The ME network peripheral SHALL be usable only when AE2 is loaded and its integration configuration is enabled, without making AE2 a required runtime dependency of Unlimited Peripheral Works.

#### Scenario: AE2 is loaded
- **WHEN** the game starts with AE2 and the ME network peripheral enabled
- **THEN** the block, block item, block entity, recipe, model, localization, and creative inventory entry are available

#### Scenario: AE2 is absent
- **WHEN** the game starts without AE2
- **THEN** Unlimited Peripheral Works starts normally and does not load AE2 API classes or expose a craftable ME network peripheral

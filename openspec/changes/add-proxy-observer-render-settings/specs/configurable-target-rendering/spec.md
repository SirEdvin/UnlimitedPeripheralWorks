## ADDED Requirements

### Requirement: Bound configurators open target render settings
The system SHALL open a target render settings screen when a player right-clicks the air without crouching while holding a main-hand Ultimate Configurator bound to a Peripheral Proxy or Remote Observer in the current dimension.

#### Scenario: Open Peripheral Proxy settings
- **WHEN** a player uses a configurator bound to a loaded Peripheral Proxy and the use ray misses all blocks
- **THEN** the client opens the target render settings screen for that Peripheral Proxy

#### Scenario: Open Remote Observer settings
- **WHEN** a player uses a configurator bound to a loaded Remote Observer and the use ray misses all blocks
- **THEN** the client opens the target render settings screen for that Remote Observer

#### Scenario: Bound block is unavailable
- **WHEN** the bound position is in another dimension, unloaded, removed, or no longer contains the expected block entity
- **THEN** the system does not open a settings screen and reports that the target is unavailable

#### Scenario: Crouching air use remains detach behavior
- **WHEN** a player crouch-right-clicks the air with a bound Ultimate Configurator
- **THEN** the configurator detaches from its bound block instead of opening the target render settings screen

### Requirement: One screen configures all targets
The target render settings screen SHALL show the block's current text style and box style and SHALL apply each selected style uniformly to every target tracked by that block.

#### Scenario: Cycle text style forward
- **WHEN** the player left-clicks the text style control
- **THEN** the control cycles through `none`, `regular`, and `bold` in forward order and applies the selected value to the block

#### Scenario: Cycle box style backward
- **WHEN** the player right-clicks the box style control
- **THEN** the control cycles through `none`, `flare`, `filled`, and `outline` from the initial `none` value and applies the selected value to the block

#### Scenario: Reopen settings
- **WHEN** the player closes and reopens the screen for the same block
- **THEN** the controls show that block's persisted text and box styles

### Requirement: Style mutations are server-authoritative
The system MUST accept configurator style mutations only for a loaded Peripheral Proxy or Remote Observer matching the main-hand configurator's bound mode, position, and dimension, and SHALL synchronize accepted values to clients.

#### Scenario: Valid configurator mutation
- **WHEN** a player changes a style for the block to which the held configurator is validly bound
- **THEN** the server stores the new style and synchronizes it to observing clients

#### Scenario: Invalid configurator mutation
- **WHEN** a mutation names an invalid style or does not match the held configurator's mode, position, dimension, or target block type
- **THEN** the server rejects the mutation without changing the block's settings

### Requirement: Peripheral Proxy renders configured targets
While Peripheral Proxy configurator rendering is active, the system SHALL render every tracked target using the Proxy's one configured text style and one configured box style.

#### Scenario: Proxy text style
- **WHEN** the Proxy text style is `regular` or `bold`
- **THEN** each target displays its assigned remote peripheral name in white using the selected weight

#### Scenario: Proxy text disabled
- **WHEN** the Proxy text style is `none`
- **THEN** no target peripheral names are rendered

#### Scenario: Proxy box style
- **WHEN** the Proxy box style is `outline`, `filled`, or `flare`
- **THEN** the Proxy displays the selected green box effect
- **AND** each target displays the selected orange box effect with its attached face green
- **AND** no fixed flare is also displayed

#### Scenario: Proxy boxes disabled
- **WHEN** the Proxy box style is `none`
- **THEN** no target box or target flare is rendered

### Requirement: Remote Observer renders configured targets
While Remote Observer configurator rendering is active, the system SHALL render every tracked target using the Observer's one configured text style and one configured box style.

#### Scenario: Observer text style
- **WHEN** the Observer text style is `regular` or `bold`
- **THEN** each target displays the target block's translated name in white using the selected weight

#### Scenario: Observer text disabled
- **WHEN** the Observer text style is `none`
- **THEN** no target block names are rendered

#### Scenario: Observer box style
- **WHEN** the Observer box style is `outline`, `filled`, or `flare`
- **THEN** the Observer displays the selected green box effect
- **AND** each target displays the selected orange box effect
- **AND** no fixed flare is also displayed

#### Scenario: Observer boxes disabled
- **WHEN** the Observer box style is `none`
- **THEN** no target box or target flare is rendered

### Requirement: Remote Observer tracking changes synchronize
The system SHALL synchronize direct Remote Observer tracking additions and removals and SHALL replace stale client tracking state when updates arrive.

#### Scenario: Remove a tracked position through Lua
- **WHEN** a computer removes a tracked position from a Remote Observer
- **THEN** observing clients remove that position from the Observer overlay

### Requirement: Box style includes the source block
The system SHALL render the bound Peripheral Proxy or Remote Observer in green using the same box style selected for its targets.

#### Scenario: All box effects disabled
- **WHEN** box style is `none`
- **THEN** source and target box effects are absent

### Requirement: Render settings persist with compatibility defaults
The system SHALL persist text and box styles in each Peripheral Proxy and Remote Observer block entity and SHALL safely fall back to the block type's compatibility defaults when either value is absent or invalid.

#### Scenario: Peripheral Proxy legacy data
- **WHEN** a Peripheral Proxy loads without valid render-style fields
- **THEN** its text style is `regular` and its box style is `flare`

#### Scenario: Remote Observer legacy data
- **WHEN** a Remote Observer loads without valid render-style fields
- **THEN** its text style is `none` and its box style is `flare`

#### Scenario: Persist configured styles
- **WHEN** a configured block is saved and loaded again
- **THEN** its valid text and box style values are restored

### Requirement: Lua exposes and changes render settings
Peripheral Proxy and Remote Observer peripherals SHALL include lowercase `textStyle` and `boxStyle` values in `getConfiguration` and SHALL expose `setTextStyle` and `setBoxStyle` methods that mutate the same persisted block settings.

#### Scenario: Read configuration
- **WHEN** a computer calls `getConfiguration`
- **THEN** the returned table includes the block's current `textStyle` and `boxStyle` lowercase strings

#### Scenario: Set valid text style
- **WHEN** a computer calls `setTextStyle` with exactly `none`, `regular`, or `bold`
- **THEN** the method succeeds, persists the selected text style, and synchronizes it to clients

#### Scenario: Set valid box style
- **WHEN** a computer calls `setBoxStyle` with exactly `none`, `outline`, `filled`, or `flare`
- **THEN** the method succeeds, persists the selected box style, and synchronizes it to clients

#### Scenario: Reject invalid Lua style
- **WHEN** a computer passes any other spelling or letter case to either style setter
- **THEN** the method returns `false` with an explanatory error and leaves both settings unchanged

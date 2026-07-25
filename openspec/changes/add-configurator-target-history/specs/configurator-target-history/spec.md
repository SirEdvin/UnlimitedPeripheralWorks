## ADDED Requirements

### Requirement: Target history
The Ultimate Configurator SHALL persist one last-used target order containing every favorite and the three most recently attached distinct non-favorites. Each target SHALL include its configuration type, dimension, and block coordinates, and target identity SHALL be determined by dimension and coordinates.

#### Scenario: Attach a new target
- **WHEN** a player attaches the configurator to a configurable block not already in its history
- **THEN** the system stores that target first and retains every favorite plus at most the three newest distinct non-favorites

#### Scenario: Reattach a stored target
- **WHEN** a player attaches the configurator to a dimension and coordinates already in its history
- **THEN** the system updates the stored configuration type, moves that target to the first position, and does not create a duplicate

#### Scenario: Detach the configurator
- **WHEN** a player detaches an Ultimate Configurator
- **THEN** the system clears its active binding without clearing recent targets or favorites

### Requirement: Detached target menu
The system SHALL open a non-pausing native target menu when a player right-clicks air with a detached main-hand Ultimate Configurator. The menu SHALL display one list containing all favorites and the three latest non-favorites, ordered by last use.

#### Scenario: Open detached configurator menu
- **WHEN** a player normally right-clicks air with a detached Ultimate Configurator in the main hand
- **THEN** the system opens the target menu and displays the configurator's stored targets in last-used order

#### Scenario: Use an attached configurator on air
- **WHEN** a player normally right-clicks air with an attached Ultimate Configurator
- **THEN** the system preserves the active configuration mode's existing air-use behavior instead of opening the target menu

#### Scenario: Empty history
- **WHEN** the target menu opens for a configurator with no stored targets
- **THEN** the system displays the list as empty without creating records

### Requirement: Favorite target management
The target menu SHALL show a favorite action for non-favorites and an edit action for favorites. Each Ultimate Configurator SHALL persist no more than 16 distinct favorites while preserving last-use ordering in the combined target list.

#### Scenario: Favorite a recent target
- **WHEN** a player favorites a recent target and fewer than 16 favorites exist
- **THEN** the system marks it as a favorite without changing its last-used position

#### Scenario: Favorite an existing favorite
- **WHEN** a duplicate favorite action references a target already in the favorite list
- **THEN** the system retains one favorite record and does not exceed the limit

#### Scenario: Reach the favorite limit
- **WHEN** a player attempts to add a distinct seventeenth favorite
- **THEN** the server rejects the addition and retains the existing 16 favorites

#### Scenario: Unfavorite a target
- **WHEN** a player removes a target from favorites
- **THEN** the system removes its favorite record and retains it only if it remains among the three latest non-favorites

#### Scenario: Select or rename a favorite
- **WHEN** a player selects an existing favorite
- **THEN** the system promotes it to the first last-used position
- **WHEN** a player only renames an existing favorite
- **THEN** the system preserves its last-used position

### Requirement: Favorite target names
The target menu SHALL allow a player to open a favorite editor and assign a custom name of at most 64 characters. A named favorite SHALL display only its custom name with a golden outline; an unnamed target SHALL display its translated block name, dimension, and coordinates.

#### Scenario: Rename a favorite
- **WHEN** a player submits a non-empty valid custom name for a favorite
- **THEN** the system persists the name on the configurator and hides that row's type, dimension, and coordinates behind the custom name

#### Scenario: Clear a favorite name
- **WHEN** a player submits an empty custom name for a named favorite
- **THEN** the system removes the custom name and restores the type, dimension, and coordinate label

#### Scenario: Reject an oversized name
- **WHEN** a player submits a custom name longer than 64 characters
- **THEN** the server rejects the mutation and preserves the previous name

#### Scenario: Rename a non-favorite target
- **WHEN** a client requests a rename for a target that is not currently a favorite
- **THEN** the server rejects the mutation

### Requirement: Configurator appearance settings
The detached menu SHALL provide a Settings tab for assigning a custom Ultimate Configurator name and default favorite text and outline colors. A favorite editor SHALL allow each favorite to override both colors using the existing color picker.

#### Scenario: Change configurator settings
- **WHEN** a player submits a valid name or default favorite color from the Settings tab
- **THEN** the server persists it on that Ultimate Configurator and synchronizes the item

#### Scenario: Override favorite colors
- **WHEN** a player selects a valid text or outline color in a favorite editor
- **THEN** the server persists that color on the favorite and the target list uses it instead of the configurator default

### Requirement: Validated target selection
Selecting a stored target row SHALL immediately reattach the configurator and close the menu only when the target is in the player's current dimension, its position is loaded, and its current block state supports the stored configuration type.

#### Scenario: Select a valid target
- **WHEN** a player selects a stored target in the current dimension whose loaded block still supports its stored configuration type
- **THEN** the server attaches the configurator, promotes the target in recent history, synchronizes the item, and closes the menu

#### Scenario: Select a target in another dimension
- **WHEN** a player selects a stored target outside the current dimension
- **THEN** the system leaves the configurator detached, keeps the record, and reports that the target is unavailable

#### Scenario: Select an unloaded target
- **WHEN** a player selects a stored target whose position is not loaded
- **THEN** the system does not load the position, leaves the configurator detached, keeps the record, and reports that the target is unavailable

#### Scenario: Select a changed or removed target
- **WHEN** a player selects a loaded stored target whose block no longer supports the stored configuration type
- **THEN** the system leaves the configurator detached, keeps the record, and reports that the target is unavailable

### Requirement: Server-authoritative item mutations
The server SHALL accept target selection, favorite, unfavorite, and rename actions only for a detached Ultimate Configurator in the player's main hand and SHALL resolve each requested target against that stack's current stored records.

#### Scenario: Mutate a stale or forged target
- **WHEN** a client requests an action for a target absent from the server-side configurator history and favorites
- **THEN** the server rejects the action without changing item NBT

#### Scenario: Mutate a different item state
- **WHEN** a client requests an action while the main-hand item is not a detached Ultimate Configurator
- **THEN** the server rejects the action without changing either item

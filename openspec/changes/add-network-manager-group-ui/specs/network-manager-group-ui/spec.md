## ADDED Requirements

### Requirement: Open network manager group UI
The system SHALL open a native group management screen when a player uses an Ultimate Configurator on air while it is in network manager mode and its bound network manager is available to the client.

#### Scenario: Open UI for an available manager
- **WHEN** the player uses the configured Ultimate Configurator on air and the bound network manager is loaded with synchronized data
- **THEN** the system opens the group management screen for that network manager

#### Scenario: Bound manager is unavailable
- **WHEN** the player uses the configured Ultimate Configurator on air and the bound network manager is not available to the client
- **THEN** the system does not open the screen and informs the player that the manager is unavailable

### Requirement: Select and create groups
The group management screen SHALL provide searchable group selection and SHALL allow creation of a valid new group from entered text. The system SHALL store the selected full group name on the Ultimate Configurator.

#### Scenario: Select an existing group
- **WHEN** the player selects an existing group in the screen
- **THEN** the system records that full group name as the configurator's active assignment group

#### Scenario: Search groups
- **WHEN** the player enters search text
- **THEN** the screen displays groups whose full names match the search text

#### Scenario: Create a new group
- **WHEN** the player submits a valid, non-empty, unique group name
- **THEN** the server creates the group and the configurator selects it

#### Scenario: Reject an invalid group name
- **WHEN** the player submits an empty, over-length, or duplicate group name
- **THEN** the server leaves group data unchanged and the screen displays the rejection

### Requirement: Assign selected group in-world
The system SHALL toggle membership in the configurator's selected group when the player uses the configured Ultimate Configurator on a peripheral attached to the bound network manager. Assignment SHALL NOT require a renamed name tag.

#### Scenario: Add peripheral to selected group
- **WHEN** the player clicks an attached peripheral that is not in the selected group
- **THEN** the server adds that peripheral to the selected group and emits the existing group membership event

#### Scenario: Remove peripheral from selected group
- **WHEN** the player clicks an attached peripheral that is already in the selected group
- **THEN** the server removes that peripheral from the selected group and emits the existing group membership event

#### Scenario: Selected group is stale
- **WHEN** the configurator names a group that no longer exists
- **THEN** the server rejects the membership change and informs the player to select a group

### Requirement: Edit groups
The group management screen SHALL allow the selected group to be renamed, assigned a display color, or deleted after explicit confirmation. Server mutations SHALL preserve authoritative consistency and synchronize updated group data to clients.

#### Scenario: Rename a group
- **WHEN** the player submits a valid unique name for the selected group
- **THEN** the server atomically moves its color and memberships to the new name and updates the current configurator selection

#### Scenario: Change a group color
- **WHEN** the player chooses a valid color for the selected group
- **THEN** the server persists and synchronizes that color for overlay rendering

#### Scenario: Cancel group deletion
- **WHEN** the player opens the deletion confirmation and cancels it
- **THEN** the server leaves the group and memberships unchanged

#### Scenario: Confirm non-empty group deletion
- **WHEN** the player confirms deletion of a group containing peripherals
- **THEN** the server removes the group and all memberships, emits membership removal events, and clears the current configurator selection

### Requirement: Manage membership from the UI
The screen SHALL provide a membership view for the selected group containing every peripheral currently attached to the bound network manager and SHALL allow each membership to be toggled.

#### Scenario: Display network peripherals
- **WHEN** the player opens the membership view for a selected group
- **THEN** the screen lists every synchronized peripheral name and indicates whether each belongs to that group

#### Scenario: Toggle membership in the screen
- **WHEN** the player toggles an attached peripheral in the membership view
- **THEN** the server updates that membership, emits the existing group membership event, and synchronizes the result

#### Scenario: Peripheral detached before mutation
- **WHEN** the client requests a membership change for a peripheral no longer attached to the manager
- **THEN** the server rejects the request and leaves group data unchanged

### Requirement: Display virtual group hierarchy
The screen SHALL derive a client-side visual hierarchy by splitting full group names with the configured delimiter. Intermediate hierarchy nodes SHALL remain visual only and SHALL NOT be persisted as server groups.

#### Scenario: Build nested paths
- **WHEN** the delimiter is `/` and groups include `factory/ore/iron` and `factory/ore/gold`
- **THEN** the screen displays both real groups beneath virtual `factory` and `ore` hierarchy nodes

#### Scenario: Disable hierarchy
- **WHEN** the configured delimiter is empty
- **THEN** the screen displays the real full group names as a flat list

#### Scenario: Select a hierarchy leaf
- **WHEN** the player selects a leaf in the hierarchy
- **THEN** the configurator stores the leaf's original full group name rather than a virtual path node

### Requirement: Persist per-manager presentation settings locally
The client SHALL persist delimiter, overlay range, and hierarchy presentation state by dimension and network manager block position. These settings SHALL NOT alter or synchronize authoritative server group data.

#### Scenario: Restore local settings
- **WHEN** the player reopens a previously configured network manager on the same client
- **THEN** the screen restores that manager's delimiter, overlay range, and hierarchy presentation state

#### Scenario: Independent manager settings
- **WHEN** the player configures different settings for two network managers
- **THEN** each manager retains its own client-local settings

#### Scenario: Different clients use different hierarchy settings
- **WHEN** two clients configure different delimiters for the same network manager
- **THEN** each client sees its own hierarchy without changing the server's groups

### Requirement: Configure overlay range in the UI
The settings area SHALL allow the player to configure the overlay range for the bound network manager, and the overlay renderer SHALL use that client-local value. Swinging the configurator SHALL NOT cycle the range.

#### Scenario: Change overlay range
- **WHEN** the player changes the range in the screen settings
- **THEN** subsequent network manager overlay rendering uses the persisted local range

#### Scenario: Swing configurator
- **WHEN** the player swings an Ultimate Configurator in network manager mode
- **THEN** the configured overlay range remains unchanged

### Requirement: Validate UI mutations on the server
The server SHALL accept group mutation requests only when the sender holds an Ultimate Configurator in network manager mode bound to the targeted loaded network manager, and SHALL validate group names, colors, and peripheral membership against current server state.

#### Scenario: Valid mutation request
- **WHEN** a player holding the correctly bound configurator sends a valid mutation for the loaded manager
- **THEN** the server applies the mutation on the server thread and synchronizes the result

#### Scenario: Request targets a different manager
- **WHEN** a mutation request targets a manager other than the one bound to the held configurator
- **THEN** the server rejects the request and leaves both managers unchanged

#### Scenario: Stale concurrent request
- **WHEN** a request references group or peripheral state that changed before server handling
- **THEN** the server validates against current state and rejects any no-longer-valid mutation

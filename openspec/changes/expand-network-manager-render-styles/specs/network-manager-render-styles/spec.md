## ADDED Requirements

### Requirement: Configure overlay categories independently
The settings area SHALL let the player independently choose a render style for the selected group hierarchy, other groups, and ungrouped peripherals.

#### Scenario: Configure different styles
- **WHEN** the player chooses different styles for the three categories
- **THEN** each category retains and uses its own style on that configurator

#### Scenario: Synchronize render settings
- **WHEN** the player changes a category style in the network manager screen
- **THEN** the server validates and stores that style on the held, correctly bound configurator

#### Scenario: Cycle styles forward and backward
- **WHEN** the player left-clicks or right-clicks a render-style button
- **THEN** the setting advances to the next style or returns to the previous style respectively, wrapping at either end

### Requirement: Classify selected hierarchy
The overlay SHALL classify a peripheral as selected when it belongs to the selected group or any descendant group separated by the manager's configured non-empty delimiter. A selected classification SHALL take precedence over other group memberships.

#### Scenario: Render descendant membership as selected
- **WHEN** `t1` is selected and a peripheral belongs to `t1/t2` or `t1/t2/t3` with `/` as the delimiter
- **THEN** the overlay renders that peripheral using the selected-group style

#### Scenario: Render remaining group membership
- **WHEN** a peripheral has group memberships but none belong to the selected hierarchy
- **THEN** the overlay renders that peripheral using the other-groups style

#### Scenario: Render without memberships
- **WHEN** a peripheral has no group memberships
- **THEN** the overlay renders that peripheral using the ungrouped style

### Requirement: Configure text and box styles independently
Each overlay category SHALL provide one text control supporting none, regular, and bold, and one box control supporting none, outline, filled, and flare. Text and box choices SHALL render independently.

#### Scenario: Display category settings
- **WHEN** the settings tab is open
- **THEN** each category appears once as a label followed by its text and box controls

#### Scenario: Render text styles
- **WHEN** a category uses regular or bold text
- **THEN** the overlay renders its peripheral labels through intervening blocks in normal or bold text with a contrasting glyph outline and without applying group color to the text

#### Scenario: Combine text and box styles
- **WHEN** a category enables both a text style and a box style
- **THEN** the overlay renders both for each peripheral in that category, with text composited after and above the box effect

#### Scenario: Render an outline box
- **WHEN** a category uses outline box
- **THEN** the overlay renders a thick group-colored outline around the peripheral block that remains visible through intervening blocks

#### Scenario: Render a filled box
- **WHEN** a category uses filled box
- **THEN** the overlay renders a strong translucent group-colored fill over the peripheral block that remains visible through intervening blocks

#### Scenario: Render a flare
- **WHEN** a category uses flare
- **THEN** the overlay renders a group-colored flare inside the peripheral block

### Requirement: Resolve render colors
Colored styles SHALL use a deterministic matching group color, treating an unset group color and every ungrouped peripheral as white.

#### Scenario: Use configured group color
- **WHEN** a grouped peripheral uses a colored style and its resolved group has an explicit color
- **THEN** the box uses that color

#### Scenario: Use default white
- **WHEN** a colored style applies to an ungrouped peripheral or a group whose color is unset
- **THEN** the box uses white

### Requirement: Preserve legacy configurator behavior
The system SHALL derive missing per-category text styles from the legacy visualization mode stored on an existing configurator and default missing box styles to none.

#### Scenario: Read legacy selected and ungrouped mode
- **WHEN** a configurator has the legacy selected-plus-ungrouped mode and no new category style tags
- **THEN** selected and ungrouped text defaults to regular while other-group text defaults to none and every box defaults to none

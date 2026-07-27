## Context

Peripheral Proxy and Remote Observer are already Ultimate Configurator modes with client overlays, but their air-click handlers and settings screens do not exist and their target presentation is hard-coded. Network Manager recently established the desired text styles (`none`, `regular`, `bold`), box styles (`none`, `outline`, `filled`, `flare`), client-only screen opening, authenticated serverbound mutations, and reusable box/flare rendering behavior.

Unlike Network Manager's per-category configurator preferences, Proxy and Observer settings apply uniformly to all targets and must belong to the block so computers and every player observe the same configuration. The core implementation must remain loader-independent; Fabric and Forge only bridge client screen opening and packet transport.

## Goals / Non-Goals

**Goals:**

- Give both configurator modes one consistent target-render settings screen.
- Store, synchronize, and render one text style and one box style per block.
- Preserve existing target presentation when old block data lacks the new fields.
- Expose the same persisted values through `getConfiguration` and validated Lua setters.
- Reuse the Network Manager style vocabulary and rendering primitives.

**Non-Goals:**

- Configurable colors, source-marker styles, overlay range, or per-target styles.
- Changes to target membership, Proxy peripheral-side lookup, Observer capacity/duplicate behavior, or other existing APIs.
- Changes to Network Manager configuration ownership or rendering behavior.
- New container menus, dependencies, or loader-specific business logic.

## Decisions

### Share style types and rendering controls

Move the Network Manager text and box style enums to a shared core location and update Network Manager to use them. This keeps persistence, packets, UI labels, Lua serialization, and rendering on one vocabulary instead of introducing equivalent Proxy/Observer enums. Reuse or minimally extract the existing bidirectional style button and target box/text rendering primitives where that reduces duplication; do not generalize the full Network Manager screen or renderer.

Alternative considered: keep mode-specific enums and duplicate controls. Rejected because three representations of the same closed value set would require repeated parsing and drift-prone rendering branches.

### Store settings on each block entity

Peripheral Proxy and Remote Observer block entities each hold `textStyle` and `boxStyle`, save their enum names to NBT, and provide one validated mutation path that marks data dirty and synchronizes it to clients. Missing or invalid NBT uses type-specific defaults: Proxy `regular` plus `flare`, Observer `none` plus `flare`.

Alternative considered: store preferences on the Ultimate Configurator like Network Manager. Rejected because the requested setting is peripheral configuration, must be available to Lua, and applies to all players rather than one held item.

Remote Observer direct tracking mutations also use the existing block-entity synchronization path, and incoming NBT replaces rather than appends tracked positions so removed targets do not remain in client overlays.

### Use one lightweight screen and packet path

Both configurator modes implement `onBlockMiss`, retain the existing dimension guard, and request a shared target-render settings screen through `ModClientPlatform`. The screen entry resolves the bound client block entity and opens only for a Proxy or Observer. The screen presents one text-style control and one box-style control with the same forward/ reverse cycling behavior as Network Manager.

Style changes update the synchronized client value immediately for responsive controls and send a serverbound message. The server accepts a mutation only when the player holds a main-hand Ultimate Configurator bound to the same mode, dimension, and block position and the expected block entity is loaded. The authoritative block-entity mutation then synchronizes the result.

Alternative considered: separate screens and packets per block type. Rejected because the fields, controls, validation flow, and interaction are identical.

### Keep target-specific labels and distinguish source connections

Proxy text remains the assigned remote peripheral name. Observer text is the target block's translated display name. Text remains white. Proxy and Observer source effects are green, Observer targets are orange, and Proxy targets are orange except for the green face to which the Proxy connects in outline and filled modes. Target flares remain orange. The selected box style applies to the bound block and every target, so selecting `none` suppresses all box effects.

Alternative considered: coordinates for Observer labels or configurable colors. Rejected because block names were selected and color configuration is outside the requested scope.

### Expose lowercase Lua values

Both peripherals add lowercase `textStyle` and `boxStyle` strings to `getConfiguration`. `setTextStyle` accepts exactly `none`, `regular`, or `bold`; `setBoxStyle` accepts exactly `none`, `outline`, `filled`, or `flare`. Invalid input returns `false` with an explanatory error and leaves state unchanged; valid input returns success and uses the same synchronized block-entity mutation as the UI.

Alternative considered: case-insensitive parsing or uppercase enum names. Rejected in favor of the explicitly selected lowercase-only API contract.

## Risks / Trade-offs

- [Moving shared style enums touches existing Network Manager code] -> Keep it a mechanical type relocation and retain its persisted enum names and behavior.
- [A client screen can outlive or lose its bound block entity] -> Resolve the expected entity before opening and close or disable mutation when it is no longer available.
- [Optimistic controls can briefly differ from rejected server state] -> Apply the authoritative synchronized block value on the next update and test packet validation paths.
- [Translated Observer names depend on client resources] -> Derive labels from the synchronized target block state and use Minecraft's normal translated block name.

## Migration Plan

1. Add shared style types and block-entity fields with missing/invalid-value fallbacks that reproduce current visuals.
2. Add synchronized mutations, Lua exposure, screen opening, and rendering consumption.
3. Verify existing Network Manager behavior and both loaders through build, GameTests, and TypeScript fixtures.

No data fixer is required. Rolling back leaves unknown NBT fields that older versions ignore; existing tracked targets remain intact.

## Open Questions

None.

## Context

See `proposal.md` for motivation. `ConfigHolder` currently constructs `PeripheralWorksConfig.CommonConfig` during object initialization. That constructor iterates a mutable map populated by optional `Integration.run()` methods, but Forge registers the spec before most of those methods execute. Fabric avoids part of the timing problem by delaying spec registration, yet configuration ownership still lives in optional source packages and duplicate definitions exist for shared integrations.

The project must retain the existing ForgeConfigSpec-backed TOML format on Forge and through Forge Config API Port on Fabric. Minimal-environment builds exclude integration implementation packages, and existing worlds may already contain values at every current integration path. Configuration comments are descriptive rather than compatibility data; the duplicate Toms Storage handlers currently disagree, so the central definition will use one loader-neutral comment.

## Goals / Non-Goals

**Goals:**

- Make schema construction deterministic and independent of optional mod discovery or integration initialization.
- Keep one loader-independent definition for each logical integration's settings in the core main source set.
- Select a complete, explicit Forge or Fabric catalog before the common spec is built.
- Preserve existing configuration paths, types, defaults, ranges, and semantics exactly while allowing comments to be clarified.
- Reuse the existing `IForgeConfigHandler`/ForgeConfigSpec pattern without adding a new configuration framework.

**Non-Goals:**

- Unify Forge and Fabric integration implementations.
- Add settings for integrations unsupported by the active loader.
- Conditionally hide settings when an optional dependency is absent.
- Rename typoed or inconsistent legacy keys, change defaults, or redesign the broader base configuration.
- Change when enabled integration behavior is registered beyond removing schema mutation from integration startup.

## Decisions

### 1. Move integration setting owners into always-loaded core configuration code

Each logical integration will retain a small `IForgeConfigHandler` definition under the common configuration area in `projects/core/src/main/`. These definitions may depend on ForgeConfigSpec and project configuration interfaces, but not on integration implementation classes or optional mod APIs. Shared Forge/Fabric integrations will have one definition instead of duplicate loader copies; AE2 configuration will move out of the optional AE2 source set as well. Where duplicate comments differ, use a loader-neutral description without changing the setting contract.

Integration implementations will import and read these central setting owners. Their startup methods will no longer register configuration handlers.

**Alternatives considered:**

- Keep definitions beside integrations and force integrations to load earlier: rejected because it preserves the optional-class dependency and merely moves the lifecycle race.
- Introduce a generic configuration DSL/data model: rejected because the existing handler abstraction already expresses the required types, comments, defaults, and ranges.
- Put every integration in one large configuration object: rejected because per-integration owners preserve the current cohesion and make loader catalogs reviewable without inventing new abstraction.

### 2. Use fixed loader catalogs composed from shared and loader-exclusive sets

Core configuration code will expose deterministic catalogs composed from:

- integrations supported by both loaders, including AE2;
- Forge-only integrations; and
- Fabric-only integrations.

The Forge startup path will select the Forge catalog and the Fabric startup path will select the Fabric catalog. Catalog membership depends only on the loader build, not runtime mod presence. This intentionally keeps settings visible for supported-but-absent mods while avoiding irrelevant settings from the other loader.

**Alternatives considered:**

- Use the union on both loaders: rejected because it exposes settings that can never have an effect.
- Filter by installed mods: rejected because it recreates dependency-sensitive schema shape and causes settings to disappear between modpack edits.
- Have loader modules build ad hoc lists: rejected because central composition is easier to compare and test and keeps configuration knowledge in one place.

### 3. Replace mutable late registration with explicit one-time initialization

`ConfigHolder` will build the common config/spec from the selected catalog before either loader registers the spec and before optional integrations load. The mutable integration-registration map and `registerIntegrationConfiguration` entry point will be removed. Initialization will be one-shot and fail clearly if the spec is requested before catalog selection or if conflicting initialization is attempted.

The common configuration constructor will receive the selected handlers directly rather than reading mutable global registration state. This keeps schema construction a single deterministic pass and prevents duplicate section names from silently overwriting each other.

**Alternatives considered:**

- Eagerly build a union spec in `ConfigHolder`: simpler, but violates loader-specific scope.
- Rebuild the spec after integrations load: rejected because loaders expect registration at defined startup phases and rebuilding risks discarding loaded values.
- Extend the general platform interface with configuration catalogs: rejected because this is startup wiring used only once; a direct loader selection keeps the platform abstraction smaller.

### 4. Verify catalogs and compatibility at the schema boundary

Focused tests will construct both catalogs without optional dependencies and inspect the resulting ForgeConfigSpec values. They will assert loader membership, shared integration presence, absence of opposite-loader integrations, and representative legacy defaults/ranges/paths. A complete inventory comparison during implementation will ensure every current handler is moved and every integration consumer is redirected.

The existing minimal-environment GameTests and full multi-loader build remain end-to-end checks that no optional classes leak into configuration construction.

## Risks / Trade-offs

- [A new integration is implemented but omitted from its loader catalog] → Keep catalog membership explicit beside the central definitions and add a checklist/test entry whenever an integration configuration is introduced.
- [Initialization order changes expose an early configuration read] → Initialize immediately after core platform configuration and before registration/integration loading; fail with a targeted message rather than exposing an uninitialized property.
- [A legacy path, default, or range changes during file movement] → Inventory current definitions first and add schema-level compatibility assertions before deleting old files.
- [Fixed catalogs show settings for absent mods] → This is intentional and required; comments and section names remain unchanged, and settings become effective if the supported mod is later installed.
- [Per-integration files retain some repetition] → Prefer the existing boring handler pattern over a new DSL; duplicate Forge/Fabric definitions are still eliminated.

## Migration Plan

1. Inventory all current integration handlers by loader, including exact sections, keys, defaults, ranges, and comments.
2. Add central, optional-dependency-free definitions and shared/Forge/Fabric catalog composition in core main code.
3. Add explicit `ConfigHolder` initialization and invoke it from both loader startup paths before spec registration or optional integration loading.
4. Redirect every integration consumer to the central owner and remove runtime handler registration plus old loader/AE2 configuration files.
5. Run focused catalog/schema tests, minimal-environment GameTests, and the full build; inspect generated/default TOML shape or equivalent ForgeConfigSpec paths for compatibility.

Rollback is a source rollback: restore the old configuration files, mutable registration entry point, and loader timing together. No data-file migration is required because paths and value semantics do not change.

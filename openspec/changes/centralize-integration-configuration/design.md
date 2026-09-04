## Context

See `proposal.md` for motivation. `ConfigHolder` currently constructs `PeripheralWorksConfig.CommonConfig` during object initialization. That constructor iterates a mutable map populated by optional `Integration.run()` methods, but Forge registers the spec before most of those methods execute. Fabric avoids part of the timing problem by delaying spec registration, yet configuration ownership still lives in optional source packages and duplicate definitions exist for shared integrations.

The project must retain the existing ForgeConfigSpec-backed TOML format on Forge and through Forge Config API Port on Fabric. Minimal-environment builds exclude integration implementation packages, and existing worlds may already contain values at every current integration path. Configuration comments are descriptive rather than compatibility data; the duplicate Toms Storage handlers currently disagree, so the central definition will use one loader-neutral comment.

## Goals / Non-Goals

**Goals:**

- Make schema construction deterministic and independent of optional integration initialization order.
- Keep one loader-independent, mod-named definition for each logical integration's settings in one flat core-main package.
- Discover configuration definitions automatically and filter them by loaded dependency mod before the common spec is built.
- Preserve existing configuration paths, types, defaults, ranges, and semantics exactly while allowing comments to be clarified.
- Reuse the existing `IForgeConfigHandler`/ForgeConfigSpec pattern without adding a new configuration framework.

**Non-Goals:**

- Unify Forge and Fabric integration implementations.
- Expose settings for optional dependency mods that are not installed.
- Rename typoed or inconsistent legacy keys, change defaults, or redesign the broader base configuration.
- Change when enabled integration behavior is registered beyond removing schema mutation from integration startup.

## Decisions

### 1. Move integration setting owners into always-loaded core configuration code

Each logical integration will retain a small, uniquely mod-named configuration object directly under one flat common configuration package in `projects/core/src/main/`. Each object declares the dependency mod ID it represents and may depend on ForgeConfigSpec and project configuration interfaces, but not on integration implementation classes or optional mod APIs. Shared Forge/Fabric integrations have one definition instead of duplicate loader copies; AE2 configuration lives outside the optional AE2 source set as well. Where duplicate comments differ, use a loader-neutral description without changing the setting contract.

Integration implementations will import and read these central setting owners. Their startup methods will no longer register configuration handlers.

**Alternatives considered:**

- Keep definitions beside integrations and force integrations to load earlier: rejected because it preserves the optional-class dependency and merely moves the lifecycle race.
- Introduce a generic configuration DSL/data model: rejected because the existing handler abstraction already expresses the required types, comments, defaults, and ranges.
- Put every integration in one large configuration object: rejected because separate mod-named files preserve cohesion while the flat package avoids single-file directory noise.

### 2. Discover configuration objects from the flat package

Core configuration code will enumerate class files directly in the flat configuration package, load implementations of the integration-configuration contract, and obtain Kotlin object instances through their generated `INSTANCE` field. Discovery accepts a mod-presence predicate from the active loader and returns only configurations whose declared mod ID is installed. Results are sorted by section name and validated for duplicate sections so TOML order and failures remain deterministic.

Discovery supports both exploded class directories used by development runs and JAR entries used by packaged mods. It scans only direct classes whose names end in `Configuration`, excluding nested/generated classes. Because every discovered class is in always-loaded core code, class loading never links an optional mod API.

**Alternatives considered:**

- Keep fixed Forge/Fabric lists: rejected because every new integration would require another central edit and could silently be omitted.
- Add a classpath-scanning dependency: rejected because direct package enumeration is small and the runtime already supplies the class location.
- Use `ServiceLoader`: rejected because its provider metadata is another manually maintained class list.

### 3. Replace mutable late registration with explicit one-time initialization

`ConfigHolder` will discover installed integration configurations through the loader's existing `isModPresent` function, then build the common config/spec before either loader registers the spec and before optional integrations load. The mutable integration-registration map and `registerIntegrationConfiguration` entry point remain removed. Initialization is one-shot and fails clearly if the spec is requested before discovery or if conflicting initialization is attempted.

The common configuration constructor will receive the selected handlers directly rather than reading mutable global registration state. This keeps schema construction a single deterministic pass and prevents duplicate section names from silently overwriting each other.

**Alternatives considered:**

- Eagerly build a union spec in `ConfigHolder`: rejected because absent mods must not add sections.
- Rebuild the spec after integrations load: rejected because loaders expect registration at defined startup phases and rebuilding risks discarding loaded values.
- Extend the general platform interface with configuration catalogs: rejected because this is startup wiring used only once; a direct loader selection keeps the platform abstraction smaller.

### 4. Verify catalogs and compatibility at the schema boundary

Focused tests will run real package discovery with permissive and selective mod predicates, assert that all mod-named objects are found without a class list, and inspect the active ForgeConfigSpec for representative legacy defaults/ranges/paths. A complete inventory comparison during implementation will ensure every current handler is moved and every integration consumer is redirected.

The existing minimal-environment GameTests and full multi-loader build remain end-to-end checks that no optional classes leak into configuration construction.

## Risks / Trade-offs

- [Runtime class location uses an unsupported URI scheme] → Handle exploded directories and JAR files explicitly and exercise both loader development runtimes plus packaged builds.
- [A malformed discovered class is silently ignored] → Treat load/instantiation failures as startup errors with the offending class name.
- [Initialization order changes expose an early configuration read] → Initialize immediately after core platform configuration and before registration/integration loading; fail with a targeted message rather than exposing an uninitialized property.
- [A legacy path, default, or range changes during file movement] → Inventory current definitions first and add schema-level compatibility assertions before deleting old files.
- [Removing a mod removes its section from newly generated configuration] → This is intentional; NightConfig preserves unrelated existing file data, and reinstalling the mod restores the same paths.
- [Per-integration files retain some repetition] → Prefer the existing boring handler pattern over a new DSL; duplicate Forge/Fabric definitions are still eliminated.

## Migration Plan

1. Inventory all current integration handlers by loader, including exact sections, keys, defaults, ranges, and comments.
2. Add central, optional-dependency-free, mod-named definitions in one flat core-main package.
3. Add package autodiscovery with loader-provided mod-presence filtering and invoke it from both startup paths before spec registration or optional integration loading.
4. Redirect every integration consumer to the renamed central owner and keep runtime handler registration removed.
5. Run focused discovery/schema tests, minimal-environment GameTests, and the full build; inspect generated/default TOML shape or equivalent ForgeConfigSpec paths for compatibility.

Rollback is a source rollback: restore the old configuration files, mutable registration entry point, and loader timing together. No data-file migration is required because paths and value semantics do not change.

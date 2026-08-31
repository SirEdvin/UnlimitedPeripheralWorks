## 1. Capture the Existing Contract

- [x] 1.1 Inventory every Forge, Fabric, shared, and AE2 integration configuration section, key, type, default, range, comment divergence, and runtime consumer before moving files
- [x] 1.2 Record the shared, Forge-only, and Fabric-only integration membership and reconcile duplicate shared definitions so no loader-supported integration is omitted
- [x] 1.3 Add focused schema tests that assert both loader catalogs, supported-but-absent integration sections, opposite-loader exclusions, and representative legacy paths/defaults/ranges

## 2. Centralize Configuration Ownership

- [x] 2.1 Move each integration configuration handler into the core main configuration area with no imports from optional mod APIs or integration implementation source sets
- [x] 2.2 Replace duplicate Forge/Fabric handlers with one shared logical definition, normalize divergent comments, and preserve each existing TOML value contract exactly
- [x] 2.3 Move the AE2 handler from the optional AE2 source set into core main configuration code
- [x] 2.4 Define deterministic shared, Forge-only, and Fabric-only handler sets and compose explicit Forge and Fabric catalogs with duplicate-section validation

## 3. Make Schema Construction Deterministic

- [x] 3.1 Change `PeripheralWorksConfig.CommonConfig` to receive its integration handlers directly and remove the mutable late-registration map and registration API
- [x] 3.2 Change `ConfigHolder` to perform guarded one-time construction from an explicitly selected loader catalog and provide a clear failure for access before initialization or conflicting reinitialization
- [x] 3.3 Initialize the Forge catalog before Forge config registration and before any optional integration loader call
- [x] 3.4 Initialize and register the Fabric catalog before any optional integration loader call, preserving Forge Config API Port behavior

## 4. Redirect Integration Consumers

- [x] 4.1 Update every Forge integration to read its central configuration owner and remove schema registration from integration startup
- [x] 4.2 Update every Fabric integration to read its central configuration owner and remove schema registration from integration startup
- [x] 4.3 Update shared AE2 integration consumers to use the core-main owner, then delete all superseded loader and optional-source-set `Configuration.kt` files
- [x] 4.4 Search the full project to verify no integration implementation calls configuration registration and no configuration owner imports an optional dependency class

## 5. Verify Both Loaders

- [x] 5.1 Run the focused configuration schema tests and inspect failures against the captured legacy contract
- [x] 5.2 Run `./gradlew gameTest --no-daemon -PminimalTestEnvironment` under `xvfb-run` to verify configuration construction without optional integration code
- [x] 5.3 Run `./gradlew build --no-daemon` to verify the complete Forge and Fabric builds and tests
- [x] 5.4 Inspect the final diff and schema paths to confirm only planning-approved configuration lifecycle changes occurred and existing TOML files require no migration

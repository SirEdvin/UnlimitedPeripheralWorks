## Why

Integration settings are currently registered by optional integrations after configuration construction has already started, especially on Forge, so supported settings can be missing or ineffective. Configuration ownership must be independent of optional mod classes and integration load timing.

## What Changes

- Define every integration setting in the always-loaded main configuration code rather than inside optional integration source packages.
- Build each loader's configuration from a fixed catalog of the integrations that loader supports, regardless of whether those dependency mods are installed.
- Preserve existing TOML section names, setting keys, defaults, ranges, and runtime behavior.
- Remove runtime integration-driven configuration registration and make integration implementations consume the centrally owned settings.
- Add regression coverage for configuration availability without optional integration dependencies and for loader-specific catalog membership.

## Capabilities

### New Capabilities

- `integration-configuration`: Defines lifecycle-independent, loader-appropriate configuration for optional integrations.

### Modified Capabilities

None.

## Impact

- Shared configuration ownership under `projects/core/src/main/`.
- Forge and Fabric startup/config registration paths.
- Forge-, Fabric-, and AE2 integration implementations that currently own or register `Configuration` objects.
- Existing `peripheralworks.toml` compatibility and minimal-test-environment builds.
- No new dependencies or public Lua API changes.

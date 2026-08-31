## Purpose

Ensure optional-integration settings are complete, compatible, and available at configuration startup without loading optional integration classes or dependency mods.

## ADDED Requirements

### Requirement: Configuration is available before integrations load
The system SHALL construct the integration configuration catalog before any optional integration implementation is loaded or asked to register behavior.

#### Scenario: Forge starts without optional dependency mods
- **WHEN** the Forge build starts with no optional integration dependency mods installed
- **THEN** configuration construction succeeds and includes every integration section supported by the Forge build

#### Scenario: Fabric starts without optional dependency mods
- **WHEN** the Fabric build starts with no optional integration dependency mods installed
- **THEN** configuration construction succeeds and includes every integration section supported by the Fabric build

### Requirement: Configuration catalog is loader-specific
Each loader SHALL expose settings for all integrations supported by that loader, including absent dependency mods, and SHALL omit integrations that are supported only by the other loader.

#### Scenario: Loader-exclusive integrations
- **WHEN** the configuration catalog is constructed for a loader
- **THEN** it contains that loader's shared and loader-exclusive integrations and excludes integrations exclusive to the other loader

#### Scenario: Supported dependency mod is absent
- **WHEN** a supported integration's dependency mod is not installed
- **THEN** that integration's settings remain present and configurable

### Requirement: Existing configuration remains compatible
The system MUST preserve every existing integration section name, setting key, value type, default value, validation range, and meaning on each loader.

#### Scenario: Existing configuration file is loaded
- **WHEN** a user starts the same loader with an existing `peripheralworks.toml`
- **THEN** all existing integration values are read from their original paths and retain their prior behavior

#### Scenario: New configuration file is generated
- **WHEN** a loader generates `peripheralworks.toml`
- **THEN** each supported integration setting uses its established path, type, default, and validation constraints

### Requirement: Optional dependencies are not required for configuration
Configuration construction SHALL use only always-available main-code types and SHALL NOT load, reflect on, or link against classes supplied by optional integration dependency mods.

#### Scenario: Minimal environment constructs configuration
- **WHEN** the project runs in its minimal test environment with optional integration code excluded
- **THEN** both loader-specific configuration catalogs can be constructed and validated successfully

### Requirement: Integrations consume central settings
When an optional integration is loaded, it SHALL read the centrally constructed settings for its loader and SHALL NOT mutate the configuration schema.

#### Scenario: Installed integration starts
- **WHEN** an installed optional integration initializes
- **THEN** its behavior follows the configured central values without adding or rebuilding configuration entries

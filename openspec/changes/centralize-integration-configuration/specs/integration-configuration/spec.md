## Purpose

Ensure optional-integration settings are discovered, compatible, and available at configuration startup without loading optional integration implementation classes.

## ADDED Requirements

### Requirement: Configuration is available before integrations load
The system SHALL construct the integration configuration catalog before any optional integration implementation is loaded or asked to register behavior.

#### Scenario: Forge starts without optional dependency mods
- **WHEN** the Forge build starts with no optional integration dependency mods installed
- **THEN** configuration construction succeeds without adding optional integration sections

#### Scenario: Fabric starts without optional dependency mods
- **WHEN** the Fabric build starts with no optional integration dependency mods installed
- **THEN** configuration construction succeeds without adding optional integration sections

### Requirement: Configuration discovery follows installed mods
The system SHALL discover integration configuration classes from the designated flat configuration package and SHALL include a discovered configuration only when its declared dependency mod ID is loaded.

#### Scenario: Installed integration configuration
- **WHEN** a discovered configuration declares a mod ID that is loaded
- **THEN** its settings are included without a manually maintained catalog entry

#### Scenario: Dependency mod is absent
- **WHEN** a discovered configuration declares a mod ID that is not loaded
- **THEN** its settings are omitted from the generated configuration

#### Scenario: New configuration class is added
- **WHEN** a valid configuration class is added directly to the designated package
- **THEN** discovery can load it without editing a central class list

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
- **THEN** configuration discovery and mod filtering complete successfully

### Requirement: Integrations consume central settings
When an optional integration is loaded, it SHALL read the centrally constructed settings for its loader and SHALL NOT mutate the configuration schema.

#### Scenario: Installed integration starts
- **WHEN** an installed optional integration initializes
- **THEN** its behavior follows the configured central values without adding or rebuilding configuration entries

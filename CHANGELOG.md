# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- `getActiveCrafting` for ae2 integration

### Fixed

- CPU logic for ae2 now correctly works with names

## [0.2.2] - 2022-12-26
### Fixed
- ix fluid tally in ME networks #6
- Item Count with "item_storage" type is reported as 1 when there's more #4 (derived from Periperallium)
- AE2 fluid amount unit inconsistency #8

## [0.2.1] - 2022-11-02
### Added
- `ae2` integration

## [0.2.0] - 2022-10-25
### Added
- `lifts` integration
- 1.19 integration

### Fixed
- Lectern logic for counting pages

## [0.1.2] - 2022-05-09
### Fixed
- `item_storage` now propagates peripheral type

## [0.1.1] - 2022-05-09
### Fixed
- Written book now will have correct text display in lectern peripheral4

### Changed
- Now `item_storage` and `fluid_storage` have transfer limit
- Peripheral names are now much similar to standard ComputerCraft logic
- Lectern now will strip text to not corrupt book
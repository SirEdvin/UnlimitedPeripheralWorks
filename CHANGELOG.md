# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.2.7] - 2023-05-21

### Added

- Create item vaults (and probably something else) will now have inventory peripheral instead of item_storage.

## [0.2.6] - 2023-05-21

### Fixed

- [Null-check](https://github.com/SirEdvin/UnlimitedPeripheralWorks/issues/15) for NBT sorting 

## [0.2.5] - 2023-05-17

### Fixed

- NBT sorting [issue](https://github.com/cc-tweaked/CC-Tweaked/issues/1196) for CC:R

## [0.2.4] - 2023-04-27

### Fixed

- Potential null exception in Lectern Mixin
- `getCraftingInformation` logic now more friendly

## [0.2.3] - 2023-04-27
### Added

- `getActiveCrafting` for ae2 integration
- Events for lectern integration
- Modern industrialization integration

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

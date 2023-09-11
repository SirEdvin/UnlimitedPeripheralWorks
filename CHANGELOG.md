# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### BREAKING CHANGE

- Peripheral proxy now generate peripheral names in same way as CC:T modems.
  They will be unique, but numbers will raise significantly.

### Fixed

- Peripheral names are always sorted

## [1.3.4] - 2023-08-14

### Changed

- Display pedestal now has extended event
- Reduce cooldown for scanner

### Fixed

- Lectern now correctly add page for empty book

## [1.3.3] - 2023-08-06

### Fixed

- `tint` logic for forge version
- particles for flexible statue
- Powah energy storage integration

## [1.3.2] - 2023-08-01

### Added

- Reality forger `batchForgeRealityPieces`

### Changed

- Flexible reality anchor now use translucent

## [1.3.1] - 2023-07-31

### Added

- Opacity support for statue workbench

## [1.3.0] - 2023-07-30

### Added

- Automobility integration for entity link
- Flux networks integration
- Occultism entity integration

### Fixed

- Entity card message only on server side
- Entity link (again!)

## [1.2.2] - 2023-07-27

### Fixed

- Player disabled from entity link
- Peripheral equals check

## [1.2.1] - 2023-07-25

### Added

- Entity link blocklist

### Fixed

- Peripheralium hub with `find` interaction
- Peripheralium hub with turtlematic render

## [1.2.0] - 2023-07-25

### Added

- Entity card

### Fixed

- Incorrect item localization

## [1.1.1] - 2023-07-22

### Fixed

- Radius checks for reality forger #23

## [1.1.0] - 2023-07-20

### Added

- Statue workbench
- Alloy forgery integration for fabric
- Universal shop integration for fabric
- Powah integration for forge and fabric
- More reality flexible anchor blocklist items

### Fixed

- Various scanner problems (see #22)
- Missed tooltips
- Mod meta information

## [1.0.0] - 2023-07-09

### Added

- `integrateddynamics` integration
- `additional lanterns` integration for fabric
- Reality forger
- Recipe registry
- Informative registry

## [0.5.3] - 2023-07-01

### Added

- `deepresonsance` support
- `additional lanterns` support

## [0.5.2] - 2023-06-24

### Added

- `ae2` support
- `1.20.1` support
- `natures compass` support

## [0.5.1] - 2023-06-13

### Fixed

- Dependencies definition

## [0.5.0] - 2023-06-13

### Added

- `1.20` support

## [0.4.3] - 2023-06-12

### Changed

- Introduce `peripheral_proxy_forbidden` tag and exclude peripheral proxy and modems to be added as peripherals to peripheral proxy

## [0.4.2] - 2023-06-04

### Added

- Ultimate scanner now can scan for players around
- Block scanner now can scan up to 24 blocks
- Remote observer
- Peripheral proxy, for accessing peripheral with wired network in small range.

## [0.4.1] - 2023-05-23

### Added

- Peripheralium 0.5.3 support
- DeepResonance integration

### Fixed

- Log spamming for integrateddynamics aspect

## [0.4.0] - 2023-05-16

### Added

- Peripheralium hub: pocket/turtle upgrade that can equip other upgrades
- Universal scanner: scan entity, block or items around, can be equipped on turtle or pocket computer
- Ultimate sensor: allow you to inspect a lot about world that surround you!
- Pedestal: One for item inspection, one for map inspection and one for displaying everything with events

## [0.3.0] - 2023-08-05

### Added

- 1.19.4 support
- Forge support
- Occultism integration
- IntegratedDynamics integration
- Nature's Compass integration
- Easy Villager integration
- Tom's Simple Storage integration

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
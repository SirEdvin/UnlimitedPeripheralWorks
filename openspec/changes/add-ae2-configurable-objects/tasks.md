## 1. Shared API Model

- [x] 1.1 Add shared resource parsing and representation for item counts and millibucket fluid amounts
- [x] 1.2 Add locator-based returned Lua objects that re-resolve cable parts and validate their expected device kind
- [x] 1.3 Add common priority, settings, filter, and upgrade-inventory operations used by concrete device objects
- [x] 1.4 Capture and invalidate originating computer access for side-object inventory transfers

## 2. Device Plugins

- [x] 2.1 Add direct full-block and side-object ME Interface stock APIs
- [x] 2.2 Add Import Bus and Export Bus filter, setting, and upgrade APIs
- [x] 2.3 Add Storage Bus filter, storage setting, priority, and upgrade APIs
- [x] 2.4 Add Formation Plane filter, placement setting, priority, and upgrade APIs
- [x] 2.5 Add Storage and Energy Level Emitter threshold and output APIs
- [x] 2.6 Add full-block and side-object Pattern Provider pattern, setting, priority, and direction APIs
- [x] 2.7 Register equivalent AE2 providers and cable-side dispatch on Fabric and Forge

## 3. Compatibility and Validation

- [x] 3.1 Resolve the Fabric 15.0.4-beta Export Bus crafting-tracker defect through an AE2 update or targeted compatibility correction
- [x] 3.2 Validate active filter slots and clear newly inactive filters during Capacity Card extraction
- [x] 3.3 Reject unknown resources, unsupported key types, invalid enum values, inactive slots, and invalid stock amounts without partial mutation
- [x] 3.4 Keep runtime signatures synchronized with `projects/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects.ts`

## 4. Tests

- [x] 4.1 Add shared GameTests for full-block direct APIs and cable `getSide` lookup, replacement, and removal behavior
- [x] 4.2 Add shared GameTests for stock targets, filters, settings, priorities, and AE2 mutation callbacks
- [x] 4.3 Add shared GameTests for atomic upgrade and pattern transfers, card limits, and Capacity Card slot changes
- [x] 4.4 Add loader assertions for millibucket normalization and Export Bus crafting beyond slot nine
- [x] 4.5 Compile the typed-peripheral project and TypeScript fixtures
- [x] 4.6 Run the complete Fabric and Forge GameTest suite and timed multi-loader build

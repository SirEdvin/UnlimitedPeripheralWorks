## Why

UPW exposes an AE2 network as a peripheral but does not expose the configuration, upgrade cards, priorities, or real pattern slots of individual AE2 devices. Multipart cable blocks also need a way to address a specific mounted part without flattening every part into one ambiguous peripheral API.

## What Changes

- Add direct configuration APIs for full-block ME Interfaces and Pattern Providers.
- Add a cable-host `getSide(side)` method that returns a Lua object bound to a supported AE2 part.
- Add semantic stock, filter, threshold, settings, priority, upgrade-card transfer, and encoded-pattern transfer methods appropriate to each supported device.
- Normalize public fluid amounts to millibuckets across Fabric and Forge.
- Add the proposed typed contract in `projects/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects.ts`.
- Exclude devices without a meaningful automation configuration surface and defer pattern authoring and inserted-cell editing.

## Capabilities

### New Capabilities

- `ae2-configurable-objects`: Configurable full-block AE2 peripherals and side-bound multipart Lua objects, including their device-specific state and inventory transfers.

### Modified Capabilities

None.

## Impact

- AE2 integrations in the Fabric and Forge projects.
- Peripheral plugin discovery and multipart cable-side resolution.
- Lua object conversion through CC:Tweaked `@LuaFunction` methods.
- Typed API contract at `projects/typed-peripheral-unlimitedperipheralworks/integrations/ae2Objects.ts`.
- GameTests for both loaders, including card transfers, device replacement, amount normalization, and AE2 mutation callbacks.

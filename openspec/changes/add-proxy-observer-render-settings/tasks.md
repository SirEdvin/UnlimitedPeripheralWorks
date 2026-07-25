## 1. Shared Render Styles

- [x] 1.1 Move text and box style enums into shared core types and update Network Manager references without changing its persisted values or behavior.
- [x] 1.2 Extract only the reusable style-cycle control and target text/box rendering needed by all three configurator renderers.

## 2. Persisted Peripheral Settings

- [x] 2.1 Add persisted and synchronized text/box styles to Peripheral Proxy with `regular` and `flare` compatibility defaults and safe invalid-NBT fallback.
- [x] 2.2 Add persisted and synchronized text/box styles to Remote Observer with `none` and `flare` compatibility defaults and safe invalid-NBT fallback.
- [x] 2.3 Centralize each block entity's style mutation so UI and Lua changes share validation, dirty marking, and client synchronization.
- [x] 2.4 Synchronize direct Remote Observer tracking changes and replace stale tracked positions on client reload.

## 3. Lua Configuration API

- [x] 3.1 Add lowercase `textStyle` and `boxStyle` values to Peripheral Proxy and Remote Observer `getConfiguration` results.
- [x] 3.2 Add lowercase-only `setTextStyle` and `setBoxStyle` Lua methods with success results and non-mutating explanatory failures.
- [x] 3.3 Update TypeScript fixtures to exercise defaults, valid mutations, synchronized configuration values, and invalid/case-mismatched values for both peripherals.

## 4. Configurator Screen And Networking

- [x] 4.1 Add non-crouching air-use screen opening to Peripheral Proxy and Remote Observer modes while preserving dimension checks and crouching detach behavior.
- [x] 4.2 Add the shared target render settings screen and client entry validation for loaded Proxy/Observer block entities.
- [x] 4.3 Add the serverbound style message and loader registrations, validating main hand, bound mode, position, dimension, block type, and style value before mutation.
- [x] 4.4 Add Fabric and Forge client-platform screen bridges plus localized screen labels, style values, and unavailable feedback.

## 5. Target Rendering

- [x] 5.1 Update Peripheral Proxy rendering to use persisted styles for white peripheral-name text and green source/target boxes.
- [x] 5.2 Update Remote Observer rendering to use persisted styles for translated white block-name text and green source/target boxes.
- [x] 5.3 Verify `none`, `regular`, `bold`, `outline`, `filled`, and `flare` produce the specified effects without duplicate target flares.

## 6. Verification

- [x] 6.1 Add server GameTests for style defaults, valid/invalid mutation, persistence, and synchronization on both block entities.
- [x] 6.2 Add client GameTests for opening each screen, forward/reverse style cycling, unavailable targets, and retained crouching detach behavior.
- [x] 6.3 Run `:typescript-tests:compileTestLua` and fix fixture or generated API regressions.
- [x] 6.4 Run the Xvfb-backed Fabric and Forge GameTests and the timed multi-loader build, fixing all regressions including Network Manager rendering and settings tests.

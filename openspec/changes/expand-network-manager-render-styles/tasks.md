## 1. Configurator Settings

- [x] 1.1 Replace the global visualization mode with three persisted render-style settings and legacy defaults.
- [x] 1.2 Validate and synchronize per-category render-style changes through the existing network manager message.
- [x] 1.3 Replace the settings selector and group visibility control with three localized style selectors.

## 2. Overlay Rendering

- [x] 2.1 Classify peripherals into selected hierarchy, other groups, or ungrouped with deterministic color resolution.
- [x] 2.2 Render none, neutral text, neutral bold text, colored outline box, colored filled box, and colored flare styles.

## 3. Verification

- [x] 3.1 Update client GameTests for independent settings and legacy defaults.
- [x] 3.2 Regenerate localized resources and run the multi-loader build and GameTests.

## 4. Interaction And Visibility Refinements

- [x] 4.1 Render thicker outline and stronger filled boxes without depth testing.
- [x] 4.2 Add right-click previous-style behavior with wraparound and client GameTest coverage.
- [x] 4.3 Run formatting, the multi-loader build, and client GameTests.

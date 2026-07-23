## 1. Configurator Settings

- [x] 1.1 Replace the global visualization mode with three persisted render-style settings and legacy defaults.
- [x] 1.2 Validate and synchronize per-category render-style changes through the existing network manager message.
- [x] 1.3 Replace the settings selector and group visibility control with three localized style selectors.

## 2. Overlay Rendering

- [x] 2.1 Classify peripherals into selected hierarchy, other groups, or ungrouped with deterministic color resolution.
- [x] 2.2 Render neutral regular or bold text and colored outline or filled box styles.

## 3. Verification

- [x] 3.1 Update client GameTests for independent settings and legacy defaults.
- [x] 3.2 Regenerate localized resources and run the multi-loader build and GameTests.

## 4. Interaction And Visibility Refinements

- [x] 4.1 Render thicker outline and stronger filled boxes without depth testing.
- [x] 4.2 Add right-click previous-style behavior with wraparound and client GameTest coverage.
- [x] 4.3 Run formatting, the multi-loader build, and client GameTests.

## 5. Split Text And Box Settings

- [x] 5.1 Replace unified render styles with independent text and box settings for each category.
- [x] 5.2 Render enabled text and box styles together.
- [x] 5.3 Update the settings screen, synchronization, localization, and client GameTest coverage.
- [x] 5.4 Regenerate resources and run formatting, build, and GameTests.

## 6. Restore Flare Box Effect

- [x] 6.1 Add flare to the independent box options and rendering path.
- [x] 6.2 Update localization, generated resources, OpenSpec, and GameTest coverage.

## 7. Improve Combined Settings Readability

- [x] 7.1 Render text with a contrasting glyph outline.
- [x] 7.2 Present each category as one label with separate text and box buttons.
- [x] 7.3 Regenerate resources and run formatting, build, and GameTests.

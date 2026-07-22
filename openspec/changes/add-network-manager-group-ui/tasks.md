## 1. Authoritative Group Operations

- [x] 1.1 Add validated network manager operations for group creation, rename, destructive deletion, color changes, and membership toggles, including synchronization and membership events.
- [x] 1.2 Route overlapping Lua group methods through the shared operations while preserving their existing contracts, including rejection of non-empty deletion through Lua.
- [x] 1.3 Add GameTests covering rename preservation, confirmed deletion semantics, membership validation, and event-relevant mutations.

## 2. Configurator And Networking

- [x] 2.1 Store, read, update, and clear the selected full group name on the Ultimate Configurator.
- [x] 2.2 Add loader-neutral serverbound group mutation messages and handlers that validate the held configurator, bound manager position, current group state, and attached peripheral names.
- [x] 2.3 Replace name-tag assignment with selected-group toggling, open the screen on air use, and remove swing-based range cycling.

## 3. Client Presentation State

- [x] 3.1 Persist and synchronize delimiter and overlay range on each manager; store hierarchy expansion paths on the Ultimate Configurator.
- [x] 3.2 Implement the virtual hierarchy derivation and searchable leaf model while preserving full group names as identity.
- [x] 3.3 Add focused tests for flat mode, nested paths, search matching, and ambiguous or empty path segments.
- [x] 3.4 Update the network manager overlay renderer and configurator tooltip to use synchronized manager settings and selected-group state.

## 4. Group Management Screen

- [x] 4.1 Implement the native screen shell, unavailable-manager feedback, tabs, responsive layout, keyboard handling, and synchronized-state refresh behavior.
- [x] 4.2 Implement searchable group selection and creation plus rename, color editing, and confirmed destructive deletion.
- [x] 4.3 Implement the membership tab listing all attached peripherals with current membership and validated toggle actions.
- [x] 4.4 Implement server-authoritative delimiter and overlay range controls plus configurator-backed hierarchy expansion.
- [x] 4.5 Add English and Ukrainian localization for screen controls, validation feedback, confirmations, and updated configurator tooltips.

## 5. Verification

- [x] 5.1 Run formatting and the full Gradle build for both loader targets.
- [x] 5.2 Run the bounded GameTest suite and confirm group mutations pass on both loaders.
- [ ] 5.3 Launch the client under `xvfb-run` with an explicit timeout and verify screen opening, group CRUD, hierarchy settings, membership changes, selected-group in-world assignment, and overlay range behavior.
- [x] 5.4 Expose manager settings through the peripheral API and test delimiter-aware parent group queries.

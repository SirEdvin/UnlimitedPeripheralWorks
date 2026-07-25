## 1. Target Data And Attachment

- [x] 1.1 Add bounded NBT serialization and parsing for target records, a last-used history containing all favorites plus three non-favorites, a 16-favorite limit, and optional 64-character favorite names.
- [x] 1.2 Record recent targets in the shared active-mode save path while preserving history and favorites during detach.
- [x] 1.3 Add item mutation helpers for favorite toggle, rename/reset, and validated reattachment using current dimension, loaded position, and registered block mode.

## 2. Networking And Interaction

- [x] 2.1 Add and register a loader-neutral serverbound target action message that validates a detached main-hand configurator and resolves target identity from authoritative item NBT.
- [x] 2.2 Synchronize successful item mutations and report rejected, unavailable, and favorite-limit actions without loading target chunks.
- [x] 2.3 Open the target-history screen on client-side normal air use only when the Ultimate Configurator is detached, preserving all attached-mode air interactions.

## 3. Target Menu

- [x] 3.1 Add the loader-neutral client platform hook and non-pausing screen entry for the held detached configurator.
- [x] 3.2 Implement one last-used target list with multi-row pagination, default block/dimension/coordinate labels, and golden-outlined custom-name-only favorite labels.
- [x] 3.3 Implement immediate row selection, non-favorite Favorite actions, favorite Edit actions, and a favorite edit screen with rename/reset/removal controls, a 64-character client limit, and server-authoritative refresh/close behavior.
- [x] 3.4 Add English and Ukrainian localization source entries for the screen, controls, empty states, and mutation feedback, then regenerate loader resources.

## 4. Verification

- [x] 4.1 Add server GameTests for MRU deduplication/trimming, detach retention, favorite ordering/limit, rename validation, malformed NBT handling, and target-selection validation.
- [x] 4.2 Add client GameTests for detached air-use opening, section rendering and labels, favorite/rename controls, successful selection closure, and attached-mode interaction precedence.
- [x] 4.3 Run the root Fabric and Forge GameTests under Xvfb with an explicit timeout and fix failures.
- [x] 4.4 Run the timed root multi-loader build and fix failures.

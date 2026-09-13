## 1. Branch and pinned integration setup

- [x] 1.1 Create `feat/hostile-neural-network-integrations` from the intended `1.20` base before implementation, preserving planning artifacts and unrelated changes; verify branch/base with git status and merge-base.
- [x] 1.2 Resolve published Forge 1.20.1 HNN, Extra HNN and Placebo artifacts, inspect exact native selection/model/tier APIs and all ultimate registrations, then add pinned catalog and Forge dependency entries; verify Gradle resolves them without adding Fabric dependencies and record versions. Verified HNN 5.3.3, Extra HNN 1.2.2 (8004392), Placebo 8.6.3; Forge compile passed in `build/hnn-compile-20260913-101121.log`.
- [x] 1.3 Add separately gated integration/config registration using existing startup conventions, keeping optional classes outside always-loaded definitions; verify Forge startup with neither mod, HNN alone, both mods and disabled integration settings without class-loading errors.

## 2. Loot selection plugins

- [x] 2.1 Implement live entity/model resolution and ordered loot snapshots; verify sorted discovery, valid subtypes, primary precedence, ambiguity errors, malformed/unknown/no-model entities, duplicate drops, empty drops and reload behavior with native test models.
- [x] 2.2 Implement the HNN fabricator plugin with 1-based validation and nil/null clearing via native methods; verify real Lua get/set/clear calls, invalid-index atomicity, independent machine/model selections, alias sharing and unchanged inventory/energy during selection calls.
- [x] 2.3 Add the Extra HNN ultimate adapter with identical contract and native state ownership; verify V1, V2, V3 and V4 resolution, complete method behavior and coexistence with inventory/energy APIs.
- [x] 2.4 Verify both machine families through save/reload, native GUI synchronization, changing selection during processing and actual output production; assert selected drop identity/count and normal input/energy consumption, not just saved selection values.

## 3. ItemDetail providers

- [x] 3.1 Add ordinary-model detail enrichment through the existing CC:Tweaked detail registry; verify native identities, tiers, counters, cumulative thresholds, remaining data, FE/tick and maximum-tier null behavior through detailed inventory Lua queries.
- [x] 3.2 Add Extra HNN combined-model enrichment with ordered constituent identities and shared native progression; verify four constituents, duplicate identities, rank transitions, combined cost and highest Extra HNN tier without ordinary-tier coercion.
- [x] 3.3 Harden both providers for malformed NBT, invalid resource IDs, wrong constituent counts, missing/unbound models and invalid progression; verify ordinary details survive, the extension is absent on invalid/blank/unrelated items, basic listings stay unchanged, other providers coexist and NBT snapshots remain identical.

- [x] 3.4 Add the approved narrowly gated malformed-model display-name compatibility guard; verify malformed models through the real CC:Tweaked detail registry and preserve valid names, unrelated items and NBT.

## 4. TypeScript contracts and executable fixtures

- [x] 4.1 Add/export `integrations/hostileNetworks.ts` in `projects/typed-peripheral-unlimitedperipheralworks` with both fabricator interfaces, optional ItemDetail extension, single/combined union and documented identity/index/null/error/unit semantics; verify the definition project's configured type check and client type narrowing.
- [x] 4.2 Add `projects/typescript-tests/src` fixtures and Forge-native GameTest setup for both capabilities, using shared harness code only where loader-independent; verify real peripheral calls for every method, ordinary/combined item queries and negative cases.
- [x] 4.3 Run `timeout --foreground 5m ./gradlew :typescript-tests:compileTestLua --no-daemon` with complete output redirected to a timestamped build log; verify exit zero and execute the emitted fixtures in integration GameTests, including explicit null-to-nil setter/return behavior.

## 5. End-to-end verification and PR

- [x] 5.1 Run timed integration-enabled Forge GameTests for HNN-only and HNN-plus-Extra environments, including all ultimate variants, custom model/configuration fixtures and reload cases; record resolved mods and executed test names/counts to prove tests were not silently skipped.
- [x] 5.2 Run `timeout --foreground 20m xvfb-run -a ./gradlew gameTest --no-daemon -PminimalTestEnvironment` with complete output redirected to a timestamped build log; verify both Fabric and Forge suites pass and absent integrations do not break dedicated-server startup or unrelated peripherals.
- [x] 5.3 Run `timeout --foreground 10m ./gradlew build --no-daemon` with complete output redirected to a timestamped build log and run `git diff --check`; verify both loader artifacts build, report each verification command/exit/duration/log path, inspect relevant failure windows, and stop development processes.
- [x] 5.4 Review the diff against both capability specifications, commit/push only the feature branch, and open a PR targeting `1.20`; verify the remote head and PR base, include honest test evidence and remaining CI state, and leave build outputs/logs/run directories uncommitted. Published PR: https://github.com/SirEdvin/UnlimitedPeripheralWorks/pull/105. Local verification is recorded in `verification.md`; hosted CI is tracked separately on the PR.

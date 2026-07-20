import { cpSync, mkdirSync, rmSync } from "node:fs";
import { resolve } from "node:path";
import { spawnSync } from "node:child_process";

const output = resolve("build/generated/test-lua");
const soteriaSource = resolve("build/soteria-source");
rmSync(output, { recursive: true, force: true });
rmSync(soteriaSource, { recursive: true, force: true });
mkdirSync(output, { recursive: true });
mkdirSync(soteriaSource, { recursive: true });
for (const file of ["index.ts", "base.ts", "asserts.ts", "reports.ts"]) {
  cpSync(resolve("node_modules/@siredvin/soteria", file), resolve(soteriaSource, file));
}

const result = spawnSync(
  resolve("node_modules/.bin/tstl"),
  ["-p", "tsconfig.json", "--luaBundle", resolve(output, "peripheralworksgametests.universal_scanner.lua"), "--luaBundleEntry", "src/universal_scanner.ts"],
  { stdio: "inherit" }
);
if (result.status !== 0) process.exit(result.status ?? 1);

for (const peripheral of [
  "ultimate_sensor", "item_pedestal", "map_pedestal", "display_pedestal",
  "remote_observer", "peripheral_proxy", "reality_forger", "recipe_registry",
  "informative_registry", "statue_workbench", "entity_link", "network_manager",
  "hologram_projector",
]) {
  cpSync(resolve(output, "peripheralworksgametests.universal_scanner.lua"), resolve(output, `peripheralworksgametests.${peripheral}.lua`));
}

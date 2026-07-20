import { asserts, BasicTest, printReports, TestResult, TestSuite } from "@siredvin/soteria";
import { calculateLength } from "@siredvin/cc-utils";

const has = (values: string[], expected: string): boolean => {
    for (const value of values) if (value === expected) return true;
    return false;
};

class PeripheralTest extends BasicTest {
    execute(): void {
        const label = os.getComputerLabel() as string;
        const peripheralType = label.substring("peripheralworksgametests.".length);
        const [wrapped] = peripheral.find(peripheralType);
        asserts.assertNotNull(wrapped, `${peripheralType} peripheral is missing`);
        const call = (method: string, ...args: any[]): any => peripheral.call("back", method, ...args)[0];
        asserts.assertNotNull(call("getConfiguration"), "configuration is missing");

        if (peripheralType === "universal_scanner") {
            asserts.assertNotNull(call("getOperations"), "scan operations are missing");
            call("getCooldown", "STATIONARY_UNIVERSAL_SCAN");
            asserts.assertNotNull(call("scan", "block", 1), "block scan failed");
        } else if (peripheralType === "ultimate_sensor") {
            asserts.assert(has(call("listAnalyzers"), "dimension"), "dimension analyzer is missing");
            asserts.assert(has(call("analyze", "dimension"), "minecraft:overworld"), "overworld is missing");
            const inspectors = call("listInspectors");
            for (const inspector of ["dimension", "biome", "weather", "orientation", "time", "light", "calendar", "chunk"]) {
                asserts.assert(has(inspectors, inspector), `${inspector} inspector is missing`);
            }
            for (const inspector of ["dimension", "biome", "weather", "orientation", "time", "light", "calendar"]) {
                asserts.assertNotNull(call("inspect", inspector), `${inspector} inspection failed`);
            }
        } else if (peripheralType === "item_pedestal" || peripheralType === "map_pedestal") {
            asserts.assertEqual(call("size"), 1, "pedestal size is incorrect");
            asserts.assertNotNull(call("list"), "pedestal list failed");
            call("getItemDetail", 1);
            asserts.assertGreater(call("getItemLimit", 1), 0, "pedestal limit is invalid");
            asserts.assertEqual(call("pullItems", "right", 1, 1, 1), 1, "pedestal pull failed");
            asserts.assertEqual(call("pushItems", "right", 1, 1, 1), 1, "pedestal push failed");
            if (peripheralType === "map_pedestal") {
                call("getData");
                call("updateData");
            }
        } else if (peripheralType === "display_pedestal") {
            asserts.assertNull(call("getItem"), "pedestal should start empty");
            asserts.assert(call("isLabelRendered"), "label should render by default");
            asserts.assert(call("isItemRendered"), "item should render by default");
            call("setLabelRendered", false);
            call("setItemRendered", false);
            asserts.assertFalse(call("isLabelRendered"), "label render toggle failed");
            asserts.assertFalse(call("isItemRendered"), "item render toggle failed");
            asserts.assert(call("setItem", "minecraft:diamond", "GameTest"), "setItem failed");
            asserts.assertEqual(call("getItem").name, "minecraft:diamond", "wrong displayed item");
        } else if (peripheralType === "remote_observer") {
            const position = { x: 1, y: 0, z: 0 };
            asserts.assert(call("addPosition", position), "position was not added");
            asserts.assertEqual(calculateLength(call("getPositions")), 1, "position was not tracked");
            asserts.assert(call("removePosition", position), "position was not removed");
            asserts.assertEqual(calculateLength(call("getPositions")), 0, "position remains tracked");
        } else if (peripheralType === "peripheral_proxy") {
            // ponytail: remote targets use absolute positions, so the relocatable fixture tests deterministic empty/error behavior.
            asserts.assertEqual(calculateLength(call("getNamesRemote")), 0, "proxy should start empty");
            asserts.assertFalse(call("isPresentRemote", "missing"), "missing peripheral is present");
            asserts.assertNull(call("getTypeRemote", "missing"), "missing peripheral has a type");
            asserts.assertFalse(call("hasTypeRemote", "missing", "missing"), "missing peripheral has a type");
            asserts.assertNull(call("getMethodsRemote", "missing"), "missing peripheral has methods");
            pcall(() => call("callRemote", "missing", "missing"));
        } else if (peripheralType === "reality_forger") {
            const anchors = call("detectAnchors");
            asserts.assertGreater(calculateLength(anchors), 0, "fixture anchor is missing");
            const anchor = anchors[0];
            asserts.assert(call("forgeRealityPieces", [anchor], { block: "minecraft:stone" }), "forge pieces failed");
            asserts.assert(call("batchForgeRealityPieces", [[[anchor], { block: "minecraft:oak_planks" }]]), "batch forge failed");
            asserts.assert(call("forgeReality", { block: "minecraft:glass" }), "forge all failed");
            asserts.assert(call("clearAnchors", [anchor]), "selective clear failed");
            asserts.assert(call("clearAnchors"), "clear all failed");
        } else if (peripheralType === "recipe_registry") {
            asserts.assertGreater(calculateLength(call("getTypes")), 0, "recipe types are empty");
            asserts.assertGreater(calculateLength(call("list", "*")), 0, "recipe list is empty");
            asserts.assertNotNull(call("get", "minecraft:oak_planks"), "known recipe cannot be read");
            call("getRaw", "minecraft:oak_planks");
            asserts.assertNotNull(call("search", "minecraft:stick", "minecraft:crafting"), "recipe search failed");
        } else if (peripheralType === "informative_registry") {
            for (const target of ["itemTags", "blockTags", "entityTypeTags", "fluidTags", "mods", "entity", "item", "block", "fluid", "list"]) {
                asserts.assertNotNull(call("list", target), `${target} list failed`);
            }
            for (const [target, id] of [["item", "minecraft:stone"], ["block", "minecraft:stone"], ["fluid", "minecraft:water"], ["entity", "minecraft:pig"], ["list", "item"]]) {
                asserts.assertNotNull(call("describe", target, id), `${target} description failed`);
            }
        } else if (peripheralType === "statue_workbench") {
            asserts.assert(call("isPresent"), "fixture statue is missing");
            call("setStatueName", "GameTest Statue");
            asserts.assertEqual(call("getStatueName"), "GameTest Statue", "statue name mismatch");
            call("setAuthor", "GameTest");
            asserts.assertEqual(call("getAuthor"), "GameTest", "author mismatch");
            call("setLightLevel", 7);
            asserts.assertEqual(call("getLightLevel"), 7, "light level mismatch");
            call("setCubes", [{ x1: 0, y1: 0, z1: 0, x2: 16, y2: 16, z2: 16, texture: "minecraft:block/stone", tint: 16777215, opacity: 1 }]);
            asserts.assertEqual(calculateLength(call("getCubes")), 1, "cube was not set");
            call("reset");
            asserts.assertEqual(calculateLength(call("getCubes")), 0, "reset did not clear cubes");
        } else if (peripheralType === "entity_link") {
            asserts.assertFalse(call("isEntityFound"), "unconfigured link found an entity");
            asserts.assertNull(call("inspect"), "unconfigured link inspected an entity");
        } else if (peripheralType === "network_manager") {
            asserts.assertEqual(calculateLength(call("getGroups")), 0, "manager should start empty");
            asserts.assert(call("addGroup", "test"), "group was not added");
            call("add", "test", "missing");
            call("remove", "test", "missing");
            call("setGroupColor", "test", 0x123456);
            asserts.assertEqual(call("getGroupColor", "test"), 0x123456, "group color mismatch");
            call("get", "test");
            call("getDistanceBetween", "missing", "missing");
            asserts.assert(call("removeGroup", "test"), "group was not removed");
        } else if (peripheralType === "hologram_projector") {
            const text = call("text", { text: "GameTest" });
            const block = call("block", { Name: "minecraft:stone" });
            const item = call("item", { id: "minecraft:stone", Count: 1 });
            asserts.assertNotNull(text, "text hologram was not created");
            asserts.assertNotNull(block, "block hologram was not created");
            asserts.assertNotNull(item, "item hologram was not created");
            asserts.assertNotNull(call("list", "owned"), "owned hologram list failed");
            asserts.assertNotNull(call("list", "around"), "nearby hologram list failed");
            asserts.assert(call("update", text, { text: "Updated" }, { shadow: true }), "update failed");
            asserts.assert(call("move", text, { x: 0, y: 1, z: 0 }), "move failed");
            asserts.assert(call("rotate", text, 10, 20), "rotate failed");
            asserts.assert(call("ride", item, block), "ride failed");
            asserts.assert(call("unmount", item), "unmount failed");
            for (const id of [item, block, text]) asserts.assert(call("destroy", id), "destroy failed");
        }
    }
}

const suite = new TestSuite("Core peripheral methods");
suite.addTest(new PeripheralTest("executes every method"));
const reports = suite.run();
printReports(reports);
const report = reports[0];
if (report.result != TestResult.SUCCESS) throw report.result + ": " + report.message;

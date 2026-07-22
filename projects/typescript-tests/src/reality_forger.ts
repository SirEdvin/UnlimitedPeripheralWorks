import { asserts, BasicTest, TestSuite } from "@siredvin/soteria";
import { calculateLength, finish, peripheralCall } from "./test_helper";

class PeripheralTest extends BasicTest {
    execute(): void {
        const call = peripheralCall("reality_forger");
        const anchors = call("detectAnchors");
        asserts.assertGreater(calculateLength(anchors), 0, "fixture anchor is missing");
        const anchor = anchors[0];
        asserts.assert(call("forgeRealityPieces", [anchor], { block: "minecraft:stone" }), "forge pieces failed");
        asserts.assert(call("batchForgeRealityPieces", [[[anchor], { block: "minecraft:oak_planks" }]]), "batch forge failed");
        asserts.assert(call("forgeReality", { block: "minecraft:glass" }), "forge all failed");
        asserts.assert(call("clearAnchors", [anchor]), "selective clear failed");
        asserts.assert(call("clearAnchors"), "clear all failed");
    }
}

const suite = new TestSuite("Reality forger methods");
suite.addTest(new PeripheralTest("executes every method"));
finish(suite.run());

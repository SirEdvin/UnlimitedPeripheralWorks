import { asserts, BasicTest, TestSuite } from "@siredvin/soteria";
import { calculateLength, finish, peripheralCall } from "./test_helper";

class PeripheralTest extends BasicTest {
    execute(): void {
        const call = peripheralCall("statue_workbench");
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
    }
}

const suite = new TestSuite("Statue workbench methods");
suite.addTest(new PeripheralTest("executes every method"));
finish(suite.run());

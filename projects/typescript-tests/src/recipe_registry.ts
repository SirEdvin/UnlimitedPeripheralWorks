import { asserts, BasicTest, TestSuite } from "@siredvin/soteria";
import { calculateLength, finish, peripheralCall } from "./test_helper";

class PeripheralTest extends BasicTest {
    execute(): void {
        const call = peripheralCall("recipe_registry");
        asserts.assertGreater(calculateLength(call("getTypes")), 0, "recipe types are empty");
        asserts.assertGreater(calculateLength(call("list", "*")), 0, "recipe list is empty");
        asserts.assertNotNull(call("get", "minecraft:oak_planks"), "known recipe cannot be read");
        call("getRaw", "minecraft:oak_planks");
        asserts.assertNotNull(call("search", "minecraft:stick", "minecraft:crafting"), "recipe search failed");
    }
}

const suite = new TestSuite("Recipe registry methods");
suite.addTest(new PeripheralTest("executes every method"));
finish(suite.run());

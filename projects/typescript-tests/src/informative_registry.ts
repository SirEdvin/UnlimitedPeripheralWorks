import { asserts, BasicTest, TestSuite } from "@siredvin/soteria";
import { finish, peripheralCall } from "./test_helper";

class PeripheralTest extends BasicTest {
    execute(): void {
        const call = peripheralCall("informative_registry");
        for (const target of ["itemTags", "blockTags", "entityTypeTags", "fluidTags", "mods", "entity", "item", "block", "fluid", "list"]) {
            asserts.assertNotNull(call("list", target), `${target} list failed`);
        }
        for (const [target, id] of [["item", "minecraft:stone"], ["block", "minecraft:stone"], ["fluid", "minecraft:water"], ["entity", "minecraft:pig"], ["list", "item"]]) {
            asserts.assertNotNull(call("describe", target, id), `${target} description failed`);
        }
    }
}

const suite = new TestSuite("Informative registry methods");
suite.addTest(new PeripheralTest("executes every method"));
finish(suite.run());

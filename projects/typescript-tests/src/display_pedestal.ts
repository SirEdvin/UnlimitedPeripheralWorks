import { asserts, BasicTest, TestSuite } from "@siredvin/soteria";
import { finish, peripheralCall } from "./test_helper";

class PeripheralTest extends BasicTest {
    execute(): void {
        const call = peripheralCall("display_pedestal");
        asserts.assertNull(call("getItem"), "pedestal should start empty");
        asserts.assert(call("isLabelRendered"), "label should render by default");
        asserts.assert(call("isItemRendered"), "item should render by default");
        call("setLabelRendered", false);
        call("setItemRendered", false);
        asserts.assertFalse(call("isLabelRendered"), "label render toggle failed");
        asserts.assertFalse(call("isItemRendered"), "item render toggle failed");
        asserts.assert(call("setItem", "minecraft:diamond", "GameTest"), "setItem failed");
        asserts.assertEqual(call("getItem").name, "minecraft:diamond", "wrong displayed item");
    }
}

const suite = new TestSuite("Display pedestal methods");
suite.addTest(new PeripheralTest("executes every method"));
finish(suite.run());

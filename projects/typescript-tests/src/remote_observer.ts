import { asserts, BasicTest, TestSuite } from "@siredvin/soteria";
import { calculateLength, finish, peripheralCall } from "./test_helper";

class PeripheralTest extends BasicTest {
    execute(): void {
        const call = peripheralCall("remote_observer");
        let configuration = call("getConfiguration");
        asserts.assertEqual(configuration.textStyle, "none", "observer text style default is wrong");
        asserts.assertEqual(configuration.boxStyle, "flare", "observer box style default is wrong");
        asserts.assert(call("setTextStyle", "regular"), "observer text style was not changed");
        asserts.assert(call("setBoxStyle", "filled"), "observer box style was not changed");
        configuration = call("getConfiguration");
        asserts.assertEqual(configuration.textStyle, "regular", "observer text style did not synchronize");
        asserts.assertEqual(configuration.boxStyle, "filled", "observer box style did not synchronize");
        asserts.assertFalse(call("setTextStyle", "Regular"), "case-mismatched observer text style was accepted");
        asserts.assertFalse(call("setBoxStyle", "invalid"), "invalid observer box style was accepted");
        configuration = call("getConfiguration");
        asserts.assertEqual(configuration.textStyle, "regular", "invalid observer style changed text");
        asserts.assertEqual(configuration.boxStyle, "filled", "invalid observer style changed box");
        const position = { x: 1, y: 0, z: 0 };
        asserts.assert(call("addPosition", position), "position was not added");
        asserts.assertEqual(calculateLength(call("getPositions")), 1, "position was not tracked");
        asserts.assert(call("removePosition", position), "position was not removed");
        asserts.assertEqual(calculateLength(call("getPositions")), 0, "position remains tracked");
    }
}

const suite = new TestSuite("Remote observer methods");
suite.addTest(new PeripheralTest("executes every method"));
finish(suite.run());

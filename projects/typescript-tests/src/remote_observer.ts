import { asserts, BasicTest, TestSuite } from "@siredvin/soteria";
import { calculateLength, finish, peripheralCall } from "./test_helper";

class PeripheralTest extends BasicTest {
    execute(): void {
        const call = peripheralCall("remote_observer");
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

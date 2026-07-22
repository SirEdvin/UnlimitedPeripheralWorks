import { asserts, BasicTest, TestSuite } from "@siredvin/soteria";
import { calculateLength, finish, peripheralCall } from "./test_helper";

class PeripheralTest extends BasicTest {
    execute(): void {
        const call = peripheralCall("peripheral_proxy");
        // ponytail: remote targets use absolute positions, so the relocatable fixture tests deterministic empty/error behavior.
        asserts.assertEqual(calculateLength(call("getNamesRemote")), 0, "proxy should start empty");
        asserts.assertFalse(call("isPresentRemote", "missing"), "missing peripheral is present");
        asserts.assertNull(call("getTypeRemote", "missing"), "missing peripheral has a type");
        asserts.assertFalse(call("hasTypeRemote", "missing", "missing"), "missing peripheral has a type");
        asserts.assertNull(call("getMethodsRemote", "missing"), "missing peripheral has methods");
        pcall(() => call("callRemote", "missing", "missing"));
    }
}

const suite = new TestSuite("Peripheral proxy methods");
suite.addTest(new PeripheralTest("executes every method"));
finish(suite.run());

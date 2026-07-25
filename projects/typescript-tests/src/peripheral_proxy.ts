import { asserts, BasicTest, TestSuite } from "@siredvin/soteria";
import { calculateLength, finish, peripheralCall } from "./test_helper";

class PeripheralTest extends BasicTest {
    execute(): void {
        const call = peripheralCall("peripheral_proxy");
        let configuration = call("getConfiguration");
        asserts.assertEqual(configuration.textStyle, "regular", "proxy text style default is wrong");
        asserts.assertEqual(configuration.boxStyle, "flare", "proxy box style default is wrong");
        asserts.assert(call("setTextStyle", "bold"), "proxy text style was not changed");
        asserts.assert(call("setBoxStyle", "outline"), "proxy box style was not changed");
        configuration = call("getConfiguration");
        asserts.assertEqual(configuration.textStyle, "bold", "proxy text style did not synchronize");
        asserts.assertEqual(configuration.boxStyle, "outline", "proxy box style did not synchronize");
        asserts.assertFalse(call("setTextStyle", "BOLD"), "case-mismatched proxy text style was accepted");
        asserts.assertFalse(call("setBoxStyle", "invalid"), "invalid proxy box style was accepted");
        configuration = call("getConfiguration");
        asserts.assertEqual(configuration.textStyle, "bold", "invalid proxy style changed text");
        asserts.assertEqual(configuration.boxStyle, "outline", "invalid proxy style changed box");
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

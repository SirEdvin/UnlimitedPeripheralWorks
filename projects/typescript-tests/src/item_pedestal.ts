import { asserts, BasicTest, TestSuite } from "@siredvin/soteria";
import { finish, peripheralCall } from "./test_helper";

class PeripheralTest extends BasicTest {
    execute(): void {
        const call = peripheralCall("item_pedestal");
        asserts.assertEqual(call("size"), 1, "pedestal size is incorrect");
        asserts.assertNotNull(call("list"), "pedestal list failed");
        call("getItemDetail", 1);
        asserts.assertGreater(call("getItemLimit", 1), 0, "pedestal limit is invalid");
        asserts.assertEqual(call("pullItems", "right", 1, 1, 1), 1, "pedestal pull failed");
        asserts.assertEqual(call("pushItems", "right", 1, 1, 1), 1, "pedestal push failed");
    }
}

const suite = new TestSuite("Item pedestal methods");
suite.addTest(new PeripheralTest("executes every method", 60));
finish(suite.run());

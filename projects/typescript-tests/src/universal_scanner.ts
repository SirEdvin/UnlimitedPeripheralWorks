import { asserts, BasicTest, TestSuite } from "@siredvin/soteria";
import { finish, peripheralCall } from "./test_helper";

class PeripheralTest extends BasicTest {
    execute(): void {
        const call = peripheralCall("universal_scanner");
        asserts.assertNotNull(call("getOperations"), "scan operations are missing");
        call("getCooldown", "STATIONARY_UNIVERSAL_SCAN");
        asserts.assertNotNull(call("scan", "block", 1), "block scan failed");
    }
}

const suite = new TestSuite("Universal scanner methods");
suite.addTest(new PeripheralTest("executes every method"));
finish(suite.run());

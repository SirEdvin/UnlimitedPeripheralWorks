import { asserts, BasicTest, TestSuite } from "@siredvin/soteria";
import { calculateLength, finish, peripheralCall } from "./test_helper";

class PeripheralTest extends BasicTest {
    execute(): void {
        const call = peripheralCall("network_manager");
        asserts.assertEqual(calculateLength(call("getGroups")), 0, "manager should start empty");
        asserts.assert(call("addGroup", "test"), "group was not added");
        call("add", "test", "missing");
        call("remove", "test", "missing");
        call("setGroupColor", "test", 0x123456);
        asserts.assertEqual(call("getGroupColor", "test"), 0x123456, "group color mismatch");
        asserts.assert(call("setDelimiter", "/"), "delimiter was not set");
        asserts.assert(call("setRange", 64), "range was not set");
        const configuration = call("getConfiguration");
        asserts.assertEqual(configuration.delimiter, "/", "delimiter configuration mismatch");
        asserts.assertEqual(configuration.range, 64, "range configuration mismatch");
        call("get", "test");
        call("getDistanceBetween", "missing", "missing");
        asserts.assert(call("removeGroup", "test"), "group was not removed");
    }
}

const suite = new TestSuite("Network manager methods");
suite.addTest(new PeripheralTest("executes every method", 60));
finish(suite.run());

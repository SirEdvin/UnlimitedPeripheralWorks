import { asserts, BasicTest, TestSuite } from "@siredvin/soteria";
import { finish, peripheralCall } from "./test_helper";

class PeripheralTest extends BasicTest {
    execute(): void {
        const call = peripheralCall("entity_link");
        asserts.assertFalse(call("isEntityFound"), "unconfigured link found an entity");
        asserts.assertNull(call("inspect"), "unconfigured link inspected an entity");
    }
}

const suite = new TestSuite("Entity link methods");
suite.addTest(new PeripheralTest("executes every method"));
finish(suite.run());

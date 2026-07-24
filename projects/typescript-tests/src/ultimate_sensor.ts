import { asserts, BasicTest, TestSuite } from "@siredvin/soteria";
import { finish, has, peripheralCall } from "./test_helper";

class PeripheralTest extends BasicTest {
    execute(): void {
        const call = peripheralCall("ultimate_sensor");
        asserts.assert(has(call("listAnalyzers"), "dimension"), "dimension analyzer is missing");
        asserts.assert(has(call("analyze", "dimension"), "minecraft:overworld"), "overworld is missing");
        const inspectors = call("listInspectors");
        for (const inspector of ["dimension", "biome", "weather", "orientation", "time", "light", "calendar", "chunk"]) {
            asserts.assert(has(inspectors, inspector), `${inspector} inspector is missing`);
        }
        for (const inspector of ["dimension", "biome", "weather", "orientation", "time", "light", "calendar"]) {
            asserts.assertNotNull(call("inspect", inspector), `${inspector} inspection failed`);
        }
    }
}

const suite = new TestSuite("Ultimate sensor methods");
suite.addTest(new PeripheralTest("executes every method"));
finish(suite.run());

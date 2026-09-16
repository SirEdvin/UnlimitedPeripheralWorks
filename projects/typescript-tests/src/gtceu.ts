import { asserts, BasicTest, TestSuite } from "@siredvin/soteria";
import { Machine } from "@siredvin/typed-peripheral-unlimitedperipheralworks/integrations/gtceu/machine";
import { finish } from "./test_helper";

class SnapshotTest extends BasicTest {
    execute(): void {
        const [wrapped] = peripheral.find("gtceu:machine");
        const machine = wrapped as Machine;
        asserts.assertNotNull(machine, "GTCEu machine missing: " + textutils.serialize(peripheral.getNames()));
        const info = machine.getInfo();
        asserts.assertEqual(info.machine.id, "gtceu:lv_macerator", "machine identity");
        asserts.assertEqual(info.machine.tier, 1, "LV tier");
        asserts.assertNotNull(info.energy, "energy section missing");
        asserts.assertEqual(info.energy!.storedEU, 1234, "numeric energy");
        asserts.assertEqual(info.energy!.storedEUExact, "1234", "exact energy");
        asserts.assertEqual(info.energy!.inputVoltage, 32, "LV voltage");
        asserts.assertEqual(info.status!.state, "idle", "idle state");
        asserts.assertEqual(info.recipe!.hasRecipe, false, "no running recipe");
        asserts.assertEqual(info.maintenance == null, true, "unsupported maintenance must be nil");
        asserts.assertEqual(info.multiblock == null, true, "unsupported multiblock must be nil");
        asserts.assertGreater(machine.getRecipeTypes().length, 0, "legacy recipe helper");
        peripheral.call(peripheral.getName(machine), "setWorkingEnabled", false);
        asserts.assertEqual(machine.getInfo().status!.workingEnabled, false, "fresh control state");
        asserts.assertEqual(info.status!.workingEnabled, true, "previous snapshot must not change");
        peripheral.call(peripheral.getName(machine), "setWorkingEnabled", true);
    }
}

const suite = new TestSuite("GTCEu structured information");
suite.addTest(new SnapshotTest("typed snapshot through real Lua peripheral calls"));
finish(suite.run());

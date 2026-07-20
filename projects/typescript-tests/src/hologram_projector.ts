import { asserts, BasicTest, TestSuite } from "@siredvin/soteria";
import { finish, peripheralCall } from "./test_helper";

class PeripheralTest extends BasicTest {
    execute(): void {
        const call = peripheralCall("hologram_projector");
        const text = call("text", { text: "GameTest" });
        const block = call("block", { Name: "minecraft:stone" });
        const item = call("item", { id: "minecraft:stone", Count: 1 });
        asserts.assertNotNull(text, "text hologram was not created");
        asserts.assertNotNull(block, "block hologram was not created");
        asserts.assertNotNull(item, "item hologram was not created");
        asserts.assertNotNull(call("list", "owned"), "owned hologram list failed");
        asserts.assertNotNull(call("list", "around"), "nearby hologram list failed");
        asserts.assert(call("update", text, { text: "Updated" }, { shadow: true }), "update failed");
        asserts.assert(call("move", text, { x: 0, y: 1, z: 0 }), "move failed");
        asserts.assert(call("rotate", text, 10, 20), "rotate failed");
        asserts.assert(call("ride", item, block), "ride failed");
        asserts.assert(call("unmount", item), "unmount failed");
        for (const id of [item, block, text]) asserts.assert(call("destroy", id), "destroy failed");
    }
}

const suite = new TestSuite("Hologram projector methods");
suite.addTest(new PeripheralTest("executes every method"));
finish(suite.run());

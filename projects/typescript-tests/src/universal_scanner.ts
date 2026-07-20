import { asserts, BasicTest, printReports, TestResult, TestSuite } from "@siredvin/soteria";

interface UniversalScanner extends IPeripheral {
    scan(this: void, mode: "block", radius: number): unknown[];
}

class PeripheralTest extends BasicTest {
    execute(): void {
        // ponytail: attachment is the shared contract; add stateful fixtures for reported behavior regressions.
        const label = os.getComputerLabel() as string;
        const type = label.substring("peripheralworksgametests.".length);
        const [attached] = peripheral.find(type);
        asserts.assertNotNull(attached, `${type} peripheral is missing`);
        if (type === "universal_scanner") {
            asserts.assertNotNull((attached as unknown as UniversalScanner).scan("block", 1), "Universal scanner returned no scan result");
        }
    }
}

const suite = new TestSuite("Core peripherals");
suite.addTest(new PeripheralTest("attaches peripheral"));
const reports = suite.run();
printReports(reports);
const report = reports[0];
if (report.result != TestResult.SUCCESS) throw report.result + ": " + report.message;

import { asserts, BasicTest, printReports, TestResult, TestSuite } from "@siredvin/soteria";

interface UniversalScanner extends IPeripheral {
    scan(mode: "block", radius: number): unknown[];
}

class UniversalScannerTest extends BasicTest {
    execute(): void {
        const [scanner] = peripheral.find("universal_scanner");
        asserts.assertNotNull(scanner, "Universal scanner peripheral is missing");
        asserts.assertNotNull((scanner as unknown as UniversalScanner).scan("block", 1), "Universal scanner returned no scan result");
    }
}

const suite = new TestSuite("Universal scanner");
suite.addTest(new UniversalScannerTest("scans blocks"));
const reports = suite.run();
printReports(reports);
const report = reports[0];
if (report.result != TestResult.SUCCESS) throw report.result + ": " + report.message;

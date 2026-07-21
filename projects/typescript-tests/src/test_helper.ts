import { asserts, printReports, TestResult } from "@siredvin/soteria";
import { calculateLength } from "@siredvin/cc-utils";

export { calculateLength };

export const has = (values: string[], expected: string): boolean => {
    for (const value of values) if (value === expected) return true;
    return false;
};

export const peripheralCall = (peripheralType: string): ((method: string, ...args: any[]) => any) => {
    const [wrapped] = peripheral.find(peripheralType);
    asserts.assertNotNull(wrapped, `${peripheralType} peripheral is missing`);
    const call = (method: string, ...args: any[]): any => peripheral.call("back", method, ...args)[0];
    asserts.assertNotNull(call("getConfiguration"), "configuration is missing");
    return call;
};

export const finish = (reports: any[]): void => {
    printReports(reports);
    const report = reports[0];
    if (report.result != TestResult.SUCCESS) throw report.result + ": " + report.message;
};

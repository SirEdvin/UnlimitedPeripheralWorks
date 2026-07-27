import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";

/** @noSelf **/
export interface ControllableMachine extends ConfigurationAPI<object> {
    isWorkingEnabled(): boolean;
    setWorkingEnabled(value: boolean): void;
    setSuspendAfterFinish(value: boolean): void;
}

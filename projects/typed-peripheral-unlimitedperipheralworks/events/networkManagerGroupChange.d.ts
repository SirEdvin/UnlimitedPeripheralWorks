import * as events from "@siredvin/cc-events";
export declare const NETWORK_MANAGER_GROUP_CHANGE = "network_manager_group_change";
export declare class NetworkManagerGroupChangeEvent extends events.BaseEvent<[
    string,
    "added" | "removed",
    string
]> {
    getGroup(): string;
    getAction(): "added" | "removed";
    getPeripheral(): string;
}

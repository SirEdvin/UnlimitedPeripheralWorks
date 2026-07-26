import * as events from "@siredvin/cc-events";

export const NETWORK_MANAGER_GROUP_CHANGE = "network_manager_group_change";

export class NetworkManagerGroupChangeEvent extends events.BaseEvent<
    [string, "added" | "removed", string]
> {
    getGroup(): string {
        return this.args[0];
    }

    getAction(): "added" | "removed" {
        return this.args[1];
    }

    getPeripheral(): string {
        return this.args[2];
    }
}

events.eventInitializers.set(
    NETWORK_MANAGER_GROUP_CHANGE,
    (name: string, args: [string, "added" | "removed", string]) =>
        new NetworkManagerGroupChangeEvent(name, args)
);

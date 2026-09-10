import * as events from "@siredvin/cc-events";
import { AE2StorageSubscriptionResource } from "../integrations/ae2";

export const AE2_STORAGE_CHANGE = "ae2_storage_change";

export class AE2StorageChangeEvent extends events.BaseEvent<
    [string, AE2StorageSubscriptionResource, number, number]
> {
    getSubscriptionName(): string {
        return this.args[0];
    }

    getResource(): AE2StorageSubscriptionResource {
        return this.args[1];
    }

    getPreviousAmount(): number {
        return this.args[2];
    }

    getCurrentAmount(): number {
        return this.args[3];
    }
}

events.eventInitializers.set(
    AE2_STORAGE_CHANGE,
    (
        name: string,
        args: [string, AE2StorageSubscriptionResource, number, number]
    ) => new AE2StorageChangeEvent(name, args)
);

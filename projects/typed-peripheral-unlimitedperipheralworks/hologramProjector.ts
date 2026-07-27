import { ConfigurationAPI } from "@siredvin/typed-peripheral-api/configuration";
import { IPeripheralProvider } from "@siredvin/typed-peripheral-base";
import { BlockState, Fallible, Position } from "./types";

export type Vector3 = [number, number, number];
export type Quaternion = [number, number, number, number];
export type DisplayOptions = {
    brightness?: number;
    glow_color_override?: number;
    interpolation_duration?: number;
    start_interpolation?: number;
    height?: number;
    width?: number;
    shadow_radius?: number;
    shadow_strength?: number;
    view_range?: number;
    billboard?: string;
    transformation?: {
        scale?: Vector3;
        translation?: Vector3;
        left_rotation?: Quaternion;
        right_rotation?: Quaternion;
    };
};
export type TextDisplayOptions = DisplayOptions & {
    line_width?: number;
    background?: number;
    shadow?: boolean;
    see_through?: boolean;
    default_background?: boolean;
    text_opacity?: number;
    alignment?: string;
};
export type ItemDisplayOptions = DisplayOptions & { item_display?: string };

/** @noSelf **/
export interface HologramProjector
    extends ConfigurationAPI<{ entityLimit: number; distanceLimit: number }> {
    item(item: object, options?: ItemDisplayOptions): Fallible<string>;
    block(blockState: BlockState, options?: DisplayOptions): Fallible<string>;
    text(text: object, options?: TextDisplayOptions): Fallible<string>;
    destroy(uuid: string): Result;
    move(uuid: string, position: Position): Result;
    rotate(uuid: string, xRot?: number, yRot?: number): Result;
    list(mode: "owned" | "around"): Fallible<LuaTable<string, object>>;
    update(uuid: string, content?: object, options?: DisplayOptions): Result;
    ride(riderUUID: string, mountUUID: string): Result;
    unmount(riderUUID: string): Result;
}

export const hologramProjectorProvider =
    new IPeripheralProvider<HologramProjector>("hologram_projector");

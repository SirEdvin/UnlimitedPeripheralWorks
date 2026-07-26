export declare interface EmiEmptyIngredient {
    type: "empty";
}
export declare interface EMIMultiIngredient {
    candidates: EMIIngredient[];
    amount: number;
}
export declare interface EmiTagIngredient {
    type: "tag";
    key: string;
    tag_type: "item" | "fluid" | "unknown";
    amount: number;
}
export declare interface EMIItemIngredient {
    name: string;
    count: number;
    chance: number;
    type: "item";
    [key: string]: any;
}
export declare interface EMIFluidIngredient {
    name: string;
    displayName: string;
    amount: number;
    chance: number;
    type: "fluid";
}
export declare type EMIIngredient = EMIItemIngredient | EMIFluidIngredient | EmiEmptyIngredient | EMIMultiIngredient | EmiTagIngredient;

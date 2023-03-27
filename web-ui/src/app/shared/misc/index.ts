export const nullDate: Date | null = null;
export const nullNumber: number | null = null;
export const nullString: string | null = null;

export const exclusiveCheck = (switchValue: never): never => {
    throw Error("Exclusive check is failed!")
};

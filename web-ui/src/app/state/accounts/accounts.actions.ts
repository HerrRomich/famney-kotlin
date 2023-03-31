import { createActionGroup } from "@ngrx/store";
import { props } from "@ngrx/store/src";

export const accountsActions = createActionGroup({
    source: 'Accounts',
    events: {
       'Select account': props<{accountId: number}>(),
    }
});

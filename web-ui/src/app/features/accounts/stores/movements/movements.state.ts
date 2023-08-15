import { MovementDto } from '@famoney-apis/accounts';
import { DateRange } from '@famoney-features/accounts/stores/accounts/accounts.state';
import { createEntityAdapter, EntityState } from '@ngrx/entity';
import { MultiRange } from 'multi-integer-range';

export type MovementsEntity = {
  readonly pos: number;
  readonly movement?: MovementDto;
};

export interface MovementsState extends EntityState<MovementsEntity> {
  readonly dateRange: DateRange;
  readonly movementsRange: MultiRange;
  error?: MovementsStateError;
}

export type MovementsStateError = {
  readonly message: string;
};

export const movementsAdapter = createEntityAdapter<MovementsEntity>({
  selectId: (movement) => movement.pos,
  sortComparer: (movement1, movement2) =>
    movement1.pos === movement2.pos ? 0 : movement1.pos === movement2.pos ? -1 : 1,
});

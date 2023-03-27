export interface MovementDialogData {
  readonly accountId: number;
  readonly movementId?: number;
}

export interface MovementDate {
  date: Date;
  bookingDate?: Date;
  budgetPeriod?: Date;
}

export interface AccountMovement {
  movementDate?: MovementDate;
}

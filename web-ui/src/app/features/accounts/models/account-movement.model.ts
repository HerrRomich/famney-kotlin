export interface MovementDate {
  date: Date;
  bookingDate?: Date;
  budgetPeriod?: Date;
}

export interface AccountMovement {
  movementDate: MovementDate;
}

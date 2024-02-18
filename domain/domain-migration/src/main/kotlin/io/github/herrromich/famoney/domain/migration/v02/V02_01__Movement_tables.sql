create table entry_category(
  id serial primary key,
  budget_id int not null,
  type varchar(50) not null,
  parent_id int,
  name varchar(50) not null,
  constraint entry_category_parent_uq unique (budget_id, type, parent_id, name),
  constraint entry_category_parent_fk foreign key (parent_id) references entry_category(id),
  constraint entry_category_type_ck check (type in  ('INCOME', 'EXPENSE'))
);
comment on table entry_category is 'Movement entry categories. A hierarchical structure. Could be of 2 types : (INCOME, EXPENSE)';
comment on column entry_category.budget_id is 'Budget ID. Groups users.';
comment on column entry_category.type is 'Movement type. could be "INCOME", "EXPENSE"';
comment on column entry_category.type is 'Movement type. could be "INCOME", "EXPENSE"';
comment on column entry_category.parent_id is 'Reference to a higher level category. Root level has NULL value.';
comment on column entry_category.name is 'Name of category level.';

create table movement(
  id serial primary key,
  account_id int not null,
  type varchar(50) not null,
  date date not null,
  pos real not null,
  booking_date date,
  budget_period date,
  category_id int,
  comments varchar(1000),
  opposite_account_id int,
  amount decimal(13, 2) not null,
  total decimal(13, 2) not null,
  constraint movement_account_pos_uq unique (account_id, date, pos) deferrable initially deferred,
  constraint movement_account_fk foreign key (account_id) references account(id),
  constraint movement_opposite_account_fk foreign key (opposite_account_id) references account(id),
  constraint movement_type_ck check (type in ('ENTRY', 'REFUND', 'TRANSFER', 'BALANCE'))
);
comment on table movement is 'Table with all movements.';
comment on column movement.id is 'Identifier.';
comment on column movement.account_id is 'Account ID.';
comment on column movement.type is 'Movement type. could be "ENTRY", "REFUND", "TRANSFER"';
comment on column movement.date is 'Date of movement.';
comment on column movement.pos is 'Position of movement relative to account.';
comment on column movement.booking_date is 'Date of booking. Could be null, then equals to date of movement';
comment on column movement.budget_period is 'Period in budget. Truncated to start of month. Could be null, then equals to month of movement date.';
comment on column movement.category_id is 'Reference to entry category for entry or refund.';
comment on column movement.comments is 'Comments to entry or refund.';
comment on column movement.opposite_account_id is 'Opposite account for transfer';
comment on column movement.amount is 'Amount of movement.';
comment on column movement.total is 'Total amount of movements including this.';


create table entry_item(
  entry_id int not null,
  pos int not null,
  category_id int,
  comments varchar(1000),
  amount decimal(13, 2) not null,
  constraint entry_item_pk primary key (entry_id, pos),
  constraint entry_item_movement_fk foreign key (entry_id) references movement (id)
);
comment on table entry_item is 'Entry items for multiple scheck positions.';
comment on column entry_item.entry_id is 'Reference to entry.';
comment on column entry_item.pos is 'Position of entry item relative to movement.';
comment on column entry_item.category_id is 'Reference to entry category.';
comment on column entry_item.comments is 'Comments to entry item.';
comment on column entry_item.amount is 'Amount of entry item.';

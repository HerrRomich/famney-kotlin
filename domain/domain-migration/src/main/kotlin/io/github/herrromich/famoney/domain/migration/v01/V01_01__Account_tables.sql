create table account(
  id serial primary key,
  budget_id int not null,
  name varchar(250) not null,
  opening_date date not null,
  constraint account_budget_name_uq unique (budget_id, name)
);
comment on table account is 'Personal accounts.';
comment on column account.budget_id is 'Budget ID. Groups users.';
comment on column account.name is 'Name of an account';
comment on column account.opening_date is 'Date of account opening.';

create table account_tag(
  account_id int not null,
  tag varchar(250) not null,
  constraint account_tag_pk primary key (account_id, tag),
  constraint account_tag_fk foreign key (account_id) references account(id)
);
comment on table account_tag is 'Elements for tagging accounts.';
comment on column account_tag.account_id is 'Reference to an account.';
comment on column account_tag.tag is 'Tag for filtering accounts.';

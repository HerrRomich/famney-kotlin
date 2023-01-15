select count(*)
     , sum(amount)
     , account_id
  from movement
 group by account_id
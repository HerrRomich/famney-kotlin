select count(*)
     , sum(amount)
  from movement
 where date between ? and ?
   and account_id = ?

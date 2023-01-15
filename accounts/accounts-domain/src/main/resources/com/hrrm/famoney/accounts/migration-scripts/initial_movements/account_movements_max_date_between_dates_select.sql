select max(date)
  from movement
 where date between ? and ?
   and account_id = ?

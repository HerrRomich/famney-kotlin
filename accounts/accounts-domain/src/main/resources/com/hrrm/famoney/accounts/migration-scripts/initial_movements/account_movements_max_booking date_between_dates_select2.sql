select max(date)
  from movement
 where booking_date between ? and ?
   and account_id = ?

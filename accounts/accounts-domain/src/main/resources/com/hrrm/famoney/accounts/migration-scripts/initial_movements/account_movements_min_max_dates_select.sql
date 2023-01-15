select min(date)
     , max(date)
     , min(booking_date)
     , max(booking_date)
  from movement
 where account_id = ?

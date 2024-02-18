package io.github.herrromich.famoney.domain.accounts.movement

import io.github.herrromich.famoney.domain.accounts.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate

@Repository
interface MovementRepository : JpaRepository<Movement, Int>, CustomMovementRepository {
    fun countByAccount(account: Account): Long

    @Query(
        nativeQuery = true, value = """
select m.total 
  from movement m
 where m.account_id = ?1
   and m.date < ?2
 order by m.date desc, m.pos desc
 limit 1
    """
    )
    fun getTotalBeforeDate(
        accountId: Int,
        fromDate: LocalDate,
    ): BigDecimal?

    @Modifying
    @Query(
        nativeQuery = true,
        value = """
update
	movement m
set
    pos = t.new_pos,
	total = t.total
from
	(
	select
		p.id,
		case
			p.balance_group
	  when 0 then ?3
			else 0
		end +
	sum(p.amount)over (partition by p.account_id,
		p.balance_group
	order by
			p.date,
			p.pos) total,
       p.new_pos
	from
		(
		select
			m.*,
			sum(case
			m.type when 'BALANCE' then 1
			else 0
		end) over (partition by m.account_id
		order by
			m.date,
			m.pos) balance_group,
            row_number()over (partition by m.account_id, m.date
		order by m.pos) new_pos
		from
			movement m
		where
			m.account_id = ?1
			and m.date >= ?2) p
	order by
		p.date ,
		p.pos )t
where
	m.id = t.id"""
    )
    fun updateTotalAndPos(
        accountId: Int,
        fromDate: LocalDate,
        startTotal: BigDecimal,
    )
}

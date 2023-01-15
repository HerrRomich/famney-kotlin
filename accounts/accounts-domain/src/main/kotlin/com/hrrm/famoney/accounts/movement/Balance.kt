package com.hrrm.famoney.accounts.movement

import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue("balance")
class Balance : Movement()
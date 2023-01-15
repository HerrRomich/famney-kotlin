package com.hrrm.famoney.accounts

import com.hrrm.famoney.commons.persistence.EclipseLinkJpaConfig
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.jta.JtaTransactionManager
import javax.annotation.PostConstruct
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("com.hrrm.famoney.accounts", entityManagerFactoryRef = "accountsEntityManagerFactory")
class AccountsJpaConfig(
    dataSource: DataSource,
    properties: JpaProperties,
    jtaTransactionManager: ObjectProvider<JtaTransactionManager>
) : EclipseLinkJpaConfig(dataSource, properties, jtaTransactionManager) {

    @PostConstruct
    fun migrate() {
        Flyway.configure(this.javaClass.classLoader)
            .dataSource(dataSource)
            .locations("com/hrrm/famoney/accounts/migration")
            .schemas("accounts")
            .load()
            .migrate()
    }

    @Bean("accountsEntityManagerFactory")
    fun localContainerEntityManagerFactory(
        builder: EntityManagerFactoryBuilder, dataSource: DataSource
    ): LocalContainerEntityManagerFactoryBean {
        return builder
            .dataSource(dataSource)
            .packages("com.hrrm.famoney.accounts")
            .persistenceUnit("famoney.accounts")
            .properties(initJpaProperties()).build()
    }

}
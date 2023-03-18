package com.hrrm.famoney.domain

import com.hrrm.famoney.commons.persistence.EclipseLinkJpaConfig
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.jta.JtaTransactionManager
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("com.hrrm.famoney.domain", entityManagerFactoryRef = "entityManagerFactory")
class JpaConfig(
    dataSource: DataSource,
    properties: JpaProperties,
    jtaTransactionManager: ObjectProvider<JtaTransactionManager>,
) : EclipseLinkJpaConfig(dataSource, properties, jtaTransactionManager) {
    @Bean("entityManagerFactory")
    fun localContainerEntityManagerFactory(
        builder: EntityManagerFactoryBuilder, dataSource: DataSource
    ): LocalContainerEntityManagerFactoryBean {
        return builder
            .dataSource(dataSource)
            .packages("com.hrrm.famoney.domain")
            .persistenceUnit("famoney")
            .properties(initJpaProperties()).build()
    }

}
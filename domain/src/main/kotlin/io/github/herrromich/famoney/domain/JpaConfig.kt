package io.github.herrromich.famoney.domain

import io.github.herrromich.famoney.commons.persistence.EclipseLinkJpaConfig
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
@EnableJpaRepositories("io.github.herrromich.famoney.domain", entityManagerFactoryRef = "entityManagerFactory")
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
            .packages("io.github.herrromich.famoney.domain")
            .persistenceUnit("famoney")
            .properties(initJpaProperties()).build()
    }

}
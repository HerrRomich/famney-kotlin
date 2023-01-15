package com.hrrm.famoney.commons.persistence

import org.eclipse.persistence.config.BatchWriting
import org.eclipse.persistence.config.PersistenceUnitProperties
import org.eclipse.persistence.logging.SessionLog
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.jta.JtaTransactionManager
import javax.sql.DataSource

abstract class EclipseLinkJpaConfig(
    dataSource: DataSource,
    properties: JpaProperties,
    jtaTransactionManager: ObjectProvider<JtaTransactionManager>
) :
    JpaBaseConfiguration(dataSource, properties, jtaTransactionManager) {

    override fun createJpaVendorAdapter() = EclipseLinkJpaVendorAdapter()

    override fun getVendorProperties() = mapOf(PersistenceUnitProperties.WEAVING to "false")

    companion object {
        public fun initJpaProperties() = mapOf(
            PersistenceUnitProperties.BATCH_WRITING to BatchWriting.JDBC,
            PersistenceUnitProperties.LOGGING_LEVEL to SessionLog.INFO_LABEL,
            PersistenceUnitProperties.WEAVING to "false"
        )
    }
}
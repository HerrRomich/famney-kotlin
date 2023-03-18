package io.github.herrromich.famoney.commons.persistence

import org.eclipse.persistence.config.BatchWriting
import org.eclipse.persistence.config.PersistenceUnitProperties
import org.eclipse.persistence.logging.SessionLog
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter
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
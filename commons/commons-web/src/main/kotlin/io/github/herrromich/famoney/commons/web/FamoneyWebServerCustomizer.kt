package io.github.herrromich.famoney.commons.web

import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.SecureRequestCustomizer
import org.eclipse.jetty.server.ServerConnector
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Configuration

@Configuration
class FamoneyWebServerCustomizer : WebServerFactoryCustomizer<JettyServletWebServerFactory> {

    override fun customize(factory: JettyServletWebServerFactory) {
        factory.addServerCustomizers(JettyServerCustomizer { server ->
            server.connectors.mapNotNull { it as? ServerConnector }
                .forEach {
                    it.getConnectionFactory(HttpConnectionFactory::class.java)
                        .httpConfiguration.customizers.apply {
                            firstNotNullOfOrNull { customizer -> customizer as? SecureRequestCustomizer }
                                ?.apply { isSniHostCheck = false }
                                ?: add(SecureRequestCustomizer(false))
                        }
                }
        })
    }

}
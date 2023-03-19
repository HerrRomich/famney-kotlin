dependencies {
    api("org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api:5.0.2")
    api("org.springframework.boot:spring-boot-starter-web") {
        exclude("org.springframework.boot", "spring-boot-starter-tomcat")
    }
    api("org.springframework.boot:spring-boot-starter-jetty")
    api("org.eclipse.jetty.http2:http2-server")
    api("org.eclipse.jetty:jetty-alpn-java-server")
}
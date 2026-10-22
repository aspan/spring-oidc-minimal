package org.samuliwritescode.minimal.oidc;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import com.vaadin.flow.theme.aura.Aura;
import jakarta.annotation.security.PermitAll;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication // <-- So that you may run this as a Spring Boot application
@StyleSheet(Aura.STYLESHEET)
public class Application implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args); // <-- So that you may run this directly as a Java application
    }

    @Route("secured")
    @PermitAll
    public static class SecuredRoute extends Div {
        public SecuredRoute(AuthenticationContext authenticationContext) {
            OidcUser user = (OidcUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            add(new Paragraph("This is a secured route and you are the user '%s'".formatted(user.getName())));
            Button logout = new Button("logout");
            logout.addClickListener(event -> authenticationContext.logout());
            add(logout);
        }
    }

    @Route("unsecured")
    @RouteAlias("")
    @AnonymousAllowed
    public static class UnsecuredRoute extends Div {
        public UnsecuredRoute() {
            add(new Paragraph("Welcome to unsecured route. This you may access without logging in."));
            Anchor linkToSecuredPage = new Anchor("/secured", "This route will require you to login");
            linkToSecuredPage.setRouterIgnore(true); // <-- So that spring security web filter will catch it
            add(linkToSecuredPage);
        }
    }

    @EnableWebSecurity
    @Configuration
    public static class SecurityConfiguration {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) {
            return http.with(VaadinSecurityConfigurer.vaadin().oauth2LoginPage(
                            "/oauth2/authorization/keycloak",
                            "{baseUrl}/unsecured"),
                             Customizer.withDefaults())
                    .build();
        }


    }
}

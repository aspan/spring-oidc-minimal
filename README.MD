# A minimal OpenID Connect (OIDC) enforced Vaadin Flow application with Spring Boot

[![Watch in Youtube](http://img.youtube.com/vi/kzQ0CR4dVrs/0.jpg)](https://youtu.be/kzQ0CR4dVrs)

[Watch this in Youtube](https://youtu.be/kzQ0CR4dVrs)

So you want or need to build a Java based web application that is secured by Single Sign-On (SSO) login. This is very
typical use case that you have part of application that is not public, but requires authenticated user to access it. You
could build login mechanism by yourself, but implementing security by yourself would be handling all security issues,
that will eventually rise, by yourself and if there are no compelling reasons to do so, you should go with existing
tools.

The tech stack might look like this:

- OpenID Connect or OIDC
    - Identity authentication protocol that is an extension of open authorisation 2 or oauth2.
- Keycloak
    - Free and open source identity and access management system that supports OIDC
- Spring boot
    - Convention over configuration extension for Spring java platform.
- Vaadin
    - Open source Java framework for building web applications.

## But you don’t know where to start from

Examples that you will find online are either incomplete or overwhelming. If they are incomplete, then they are not
fully working apps that you can play around. If they are overwhelming, you can play around, but you easily get lost
what does exactly what.

## Start simple

Ideally it would be really nice if you have a starting point that gives you minimal but working application. Then you
are much closer to the point of starting from scratch, where you understand every line that goes into your application.
This minimalism themed tutorial is all about taking something complex and making it simple and understandable.

So let’s try to make it simple.

## In practical terms

Project layout contains 3 necessary files:

- [Maven pom.xml with minimal deps](pom.xml)
- [Application entry point](src/main/java/org/samuliwritescode/minimal/oidc/Application.java)
- [Properties for Keycloak server](src/main/resources/application.properties)

Everything else will be either generated or are not strictly required.

What application does, it has 2 routes. First is unsecured main page and second secured page that displays username of
the logged-in user.

### Routes

The first route is for secured part that should be login enforced. Annotation @PermitAll = any logged-in user.

Second route for unsecured part that is public and accessing it does not require user to login. Annotation
@AnonymousAllowed = not necessary to be logged in.

```java
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
```

### Security configuration

Spring configuration is required to configure OIDC and VaadinWebSecurity base class is inherited here because it
configures Vaadin to play along with Spring
Security. [Vaadin.com documentation page](https://vaadin.com/docs/latest/security/enabling-security) describes the usage
and details of this base class.

If logout route is navigated, then user would stay on the final page, but we want to redirect back to /unsecured route
and logout success handler here does exactly that.

Spring security will add filters that will catch unauthorised access and redirect to Keycloak endpoints. Finally, the
Oauth2 login is with default configuration by convention values.

```java
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
```

### Keycloak properties

Most interesting properties are client_id and issuer-uri which will be pointing to Keycloak configuration that is to be
done on the next chapter. Issuer-uri contains both the address of the Keycloak server and the realm as a part in the
uri.

```properties
spring.security.oauth2.client.registration.keycloak.client-id=minimal-spring-oidc-client
spring.security.oauth2.client.registration.keycloak.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.keycloak.scope=openid
spring.security.oauth2.client.provider.keycloak.issuer-uri=http://localhost:8081/realms/minimal-spring-oidc
spring.security.oauth2.client.provider.keycloak.user-name-attribute=preferred_username
```

## To run it

application.properties that was defined in previous chapter expects Keycloak server running with client ID "
minimal-spring-oidc-client" at URL http://localhost:8081/realms/minimal-spring-oidc

Run the application by

`mvn spring-boot:run`

It will spin up the keycloak server with docker compose and provision it with two accounts 'admin:admin' and
'user:user' then open your browser at http://localhost:8080/.
// System Design - Backend
// Chapter 17, Backend Security -> 16 OAuth 2.0 & OIDC
// Java 21 / Spring Boot 3.3 / Spring Security 6

package com.example.security.oidc;

@Configuration
class OidcConfig {

    // spring-boot-starter-oauth2-client performs the whole dance: the
    // redirect, the state parameter, the code exchange, the ID-token
    // signature check, and the expiry and audience checks.
    //
    // application.yml is the entire configuration:
    // spring:
    // security:
    // oauth2:
    // client:
    // registration:
    // google:
    // client-id: ${GOOGLE_CLIENT_ID}
    // client-secret: ${GOOGLE_CLIENT_SECRET}
    // scope: openid,email,profile
    // provider:
    // google:
    // issuer-uri: https://accounts.google.com

    @Bean
    SecurityFilterChain oauth(HttpSecurity http) throws Exception {
        http.oauth2Login(login -> login
                .userInfoEndpoint(u -> u.oidcUserService(oidcUserService())));
        return http.build();
    }

    // What you write is only what happens AFTER a verified login.
    @Bean
    OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        OidcUserService delegate = new OidcUserService();

        return request -> {
            OidcUser oidcUser = delegate.loadUser(request);

            // The token is already verified: signature, issuer, audience
            // and expiry were checked before this runs.
            String subject = oidcUser.getSubject(); // stable id
            String email = oidcUser.getEmail();

            upsertUser(subject, email);
            return oidcUser;
        };
    }
}

class OidcConfigDemo {

    void demo() throws Exception {
        // Key the account on `sub`, never on the email address. An email can
        // be reassigned to a different person; sub cannot.
    }
}

1. Here we are going to convert our bank application into a resource server and angular HMI works as a client server

2. Keyclock will work as a auth server

3. Keycloak is an Opensource Identity and access management tool, there are other similar products available in market like Okta (paid), Amazon Cognito (in AWS)

4. Running keycloak docker container

```
        docker run -p 8080:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.1.4 start-dev
```

5. We have to create a realm, realm is an isloated space where we can create our users, credentials, roles and groups.

6. We have to register the client information in the Auth server (keycloak)

7. For that we have to create a client and assign the proper grant type. We will get a client secret after creating a client

8. To make the spring security app a resource server need to add oauth2 resource server dependency

```
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
```

9. As we are doing the authentication using the Keycloak and not doing it locally. So we dont need custom UserDetailsService and AuthenticationProvider

10. We need to create a keycloak role converter

```
    public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        public Collection<GrantedAuthority> convert(Jwt source) {
            Map<String, Object> realmAccess = (Map<String, Object>) source.getClaims().get("realm_access");
            if (realmAccess == null || realmAccess.isEmpty()) {
                return new ArrayList<>();
            }
            Collection<GrantedAuthority> returnValue = ((List<String>) realmAccess.get("roles"))
                    .stream().map(roleName -> "ROLE_" + roleName)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            return returnValue;
        }
    }
```

11. Add the KeycloakRoleConverter config in the SecurityChainFilter
    1.  Here we added a JwtAuthenticationConverter and add the KeycloakRoleConverter to it
    2.  Removed the form login and basic login and add oath2 resource server config

```
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

        CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler = new CsrfTokenRequestAttributeHandler();
        http
                .cors(corsConfig -> corsConfig.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration config = new CorsConfiguration();
                        config.setAllowedOrigins(Collections.singletonList("https://localhost:4200"));
                        config.setAllowedMethods(Collections.singletonList("*"));
                        config.setAllowCredentials(true);
                        config.setAllowedHeaders(Collections.singletonList("*"));
                        config.setExposedHeaders(List.of("Authorization"));
                        config.setMaxAge(3600L);
                        return config;
                    }
                }))
                .sessionManagement(smc -> {
                    smc.invalidSessionUrl("/invalidSession").maximumSessions(1).maxSessionsPreventsLogin(true);
                    smc.sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::changeSessionId);
                    smc.sessionCreationPolicy(SessionCreationPolicy.STATELESS);

                })
                .requiresChannel(rcc -> rcc.anyRequest().requiresSecure())
                .csrf(csrfConfig -> csrfConfig.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/contact","/error","/register"))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/myAccount").hasRole("USER")
                        .requestMatchers("/myBalance").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/myLoans").hasRole("USER")
                        .requestMatchers("/myCards").hasRole("USER")
                .requestMatchers("/contact","/error","/register").permitAll());

        http.oauth2ResourceServer(rsc -> rsc.jwt(jwtConfigurer ->
                jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        http.exceptionHandling(handler -> {
            handler.accessDeniedHandler(new CustomAccessDenialException());
        });
        return http.build();
    }

```

12. Now we have to add config so that the resource server can communicate with the auth server
    - In application.properties we have to following config
    - Copy the URL from the keycloak
      - keycloak - realm settings - openID endpoint configuration - jwks-uri
      - This is the URL of the public key of the auth server, that will be used for the validation of the token

```
  spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${JWK_SET_URIhttp://localhost:8081/realms/eazybankdev/protocol/openid-connect/certs}
```
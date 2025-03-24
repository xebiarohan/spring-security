1. Accepting only HTTPS
   - By default both types of requests HTTPS and HTTP are accepted
   - If we want to accept only the HTTPS then we need to update the SecurityFilterChain to add requiresChannel config
   - For the testing environment we can even set HTTP only using the same config by setting it to requiresInsecure()

```
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .requiresChannel(rcc -> rcc.anyRequest().requiresSecure())
                .csrf(csrfConfig -> csrfConfig.disable())
                .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/myAccount","/myBalance").authenticated()
                .requestMatchers("/myCards","/error","/register").permitAll());
       // http.formLogin(formLoginConfig -> formLoginConfig.disable());
        http.formLogin(Customizer.withDefaults());
        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .requiresChannel(rcc -> rcc.anyRequest().requiresInsecure())
                .csrf(csrfConfig -> csrfConfig.disable())
                .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/myAccount","/myBalance").authenticated()
                .requestMatchers("/myCards","/error","/register").permitAll());
       // http.formLogin(formLoginConfig -> formLoginConfig.disable());
        http.formLogin(Customizer.withDefaults());
        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }

```

2. Exception Handling in spring security framework
   - Inside Spring security authentication and authorization related exception are handled by ExceptionTranslationFilter
   - ExceptionTranslationFilter
     - AuthenticationException (401)
     - AccessDeniedException aka Forbidden(403)
   - Spring security handles only these 2 types of exception
   - In case of AuthenticationException the ExceptionTranslationFilter calls AuthenticationEntryPoint
   - In case of AccessDeniedException it calls AccessDeniedHandler
   - These 2 are the interfaces.

3. Custom AuthenticationEntryPoint
    - we can do it for UI login or for basic authentication
    - Here in this example we are doing it for the basic authentication
    - Create a class and implement the AuthenticationEntryPoint interfaces
    - Implement the method and copy the code from the BasicAuthentucationEntryPoint class (one of the implementation of Authentication interface)
    - we are coping because we are going to replace its default error response logic
    - In the custom class we can upadte the headers and body of the response
    - Add this CustomBasicAuthenticationEntryPoint object in the SecurityChainFilter
    - Replace the withDefaults() with the lambda function to update the authenticationEntryPoint

```
    public class CustomBasicAuthenticationEntryPoint implements AuthenticationEntryPoint {
        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
                throws IOException, ServletException {
            response.setHeader("eazy0bank-error-reason", "Authentication failed");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }
    }

```

```
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .requiresChannel(rcc -> rcc.anyRequest().requiresSecure())
                .csrf(csrfConfig -> csrfConfig.disable())
                .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/myAccount","/myBalance").authenticated()
                .requestMatchers("/myCards","/error","/register").permitAll());
       // http.formLogin(formLoginConfig -> formLoginConfig.disable());
        http.formLogin(Customizer.withDefaults());
        http.httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()));
        return http.build();
    }

```

4. Global exception handling 
    - The difference bweween the exception handling in the httpBasic() and in exeptionHandling function is
    - First one is used while logging in
    - Second one is global and can be user even after the login


```
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .requiresChannel(rcc -> rcc.anyRequest().requiresInsecure())
                .csrf(csrfConfig -> csrfConfig.disable())
                .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/myAccount","/myBalance").authenticated()
                .requestMatchers("/myCards","/error","/register").permitAll());
       // http.formLogin(formLoginConfig -> formLoginConfig.disable());
        http.formLogin(Customizer.withDefaults());
        http.httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()));
        http.exceptionHandling(handler -> handler.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()));
        return http.build();
    }

```

5. Custom AccessDeniedException (403 exception)
   - Used when we want to customize the response when the user gets 403 exception
   - The content of the implemted method is same as the CustomBasicAuthenticationEntryPoint
   - Need to set header, status and content type and to update the body need to write the json object using getWriter() method.

```
public class CustomAccessDenialException implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // Populate dynamic values
        LocalDateTime currentTimeStamp = LocalDateTime.now();
        String message = (accessDeniedException != null && accessDeniedException.getMessage() != null) ?
                accessDeniedException.getMessage() : "Authorization failed";
        String path = request.getRequestURI();

        response.setHeader("eazybank-denied-reason", "Authorization failed");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");

        // Construct the JSON response
        String jsonResponse =
                String.format("{\"timestamp\": \"%s\", \"status\": %d, \"error\": \"%s\", \"message\": \"%s\", \"path\": \"%s\"}",
                        currentTimeStamp, HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                        message, path);
        response.getWriter().write(jsonResponse);
    }
}

```

```

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .requiresChannel(rcc -> rcc.anyRequest().requiresInsecure())
                .csrf(csrfConfig -> csrfConfig.disable())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/myAccount", "/myBalance").authenticated()
                        .requestMatchers("/myCards", "/error", "/register").permitAll());
        // http.formLogin(formLoginConfig -> formLoginConfig.disable());
        http.formLogin(Customizer.withDefaults());
        //http.httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()));
        http.exceptionHandling(handler -> {
            handler.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint());
            handler.accessDeniedHandler(new CustomAccessDenialException());
        });
        return http.build();
    }

```

6. Session timeout and invalid session configuration
   - When ever a user logs in the default timeout of the session is 30 minutes (JSESSION timeout value)
   - We can customize this timeout value using config in application.properties
   - Cannot set time less than 2 minutes
   - It is the idle time and does not includes the time when user is actively doing some actions.
   - So by default after session expiry the user is redirected to login page on any new request
   - We can override this to add a custom endpoint where the user will be redirected on session expiry

```
    server.servlet.session.timeout=${SESSION_TIMEOUT:20m}

```
  
```
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(smc -> smc.invalidSessionUrl("/invalidSession"))
                .requiresChannel(rcc -> rcc.anyRequest().requiresSecure())
                .csrf(csrfConfig -> csrfConfig.disable())
                .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/myAccount","/myBalance").authenticated()
                .requestMatchers("/myCards","/error","/register", "invalidSession").permitAll());
       // http.formLogin(formLoginConfig -> formLoginConfig.disable());
        http.formLogin(Customizer.withDefaults());
        http.httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()));
        http.exceptionHandling(handler -> {
            handler.accessDeniedHandler(new CustomAccessDenialException());
        });
        return http.build();
    }
```

7. Concurrent Session control configuration
   - By default we can have as many concurrent session as we want for a given user
   - We can set the maxSession Config
   - If we set the maximumSession to 1 then we can have max of 1 session per user
   - If we try logging in the 2nd session, the 1st one expires with error "This session has been expired (possibly due to multiple concurrent logins being attempted as the same user)."
   - If we want to prevent the second session login then we can do that using maxSessionPreventsLogin

```
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(smc -> smc.invalidSessionUrl("/invalidSession").maximumSessions(1))
                .requiresChannel(rcc -> rcc.anyRequest().requiresSecure())
                .csrf(csrfConfig -> csrfConfig.disable())
                .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/myAccount","/myBalance").authenticated()
                .requestMatchers("/myCards","/error","/register", "/invalidSession").permitAll());
       // http.formLogin(formLoginConfig -> formLoginConfig.disable());
        http.formLogin(Customizer.withDefaults());
        http.httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()));
        http.exceptionHandling(handler -> {
            handler.accessDeniedHandler(new CustomAccessDenialException());
        });
        return http.build();
    }

```

```
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(smc -> smc.invalidSessionUrl("/invalidSession").maximumSessions(1).maxSessionsPreventsLogin(true))
                .requiresChannel(rcc -> rcc.anyRequest().requiresInsecure())
                .csrf(csrfConfig -> csrfConfig.disable())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/myAccount", "/myBalance").authenticated()
                        .requestMatchers("/myCards", "/error", "/register","/invalidSession").permitAll());
        // http.formLogin(formLoginConfig -> formLoginConfig.disable());
        http.formLogin(Customizer.withDefaults());
        http.httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()));
        http.exceptionHandling(handler -> {
            handler.accessDeniedHandler(new CustomAccessDenialException());
        });
        return http.build();
    }

```

8. Session Hijacking
   - It is stealing the JSESSION id from the browser, URL heades or the network
   - If we use HTTPS then hackers cannot steal it from the network between the client application and the backend application
   - Other approach is to limit the session timeout to a small value like 10 minutes

9. Session fixation
    - In this the malicious attacker creates a session in an application and then persuade another user to login with the same session like by sharing a link containing a session id.
    - By default spring security takes care of the session fixation attack using 3 different strategies
      - changeSessionID - Does not create a new session, just changes the sessionID on login using link with sessionID 
      - newSession - It creates a complete new session
      - migrateSession - Creates a new session and migrate all the information from the session present in the link and gives new session ID
    - By default the spring security uses the changeSessionId strategy

```

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(smc -> {
                    smc.invalidSessionUrl("/invalidSession").maximumSessions(1).maxSessionsPreventsLogin(true);
                    smc.sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::changeSessionId);

                })
                .requiresChannel(rcc -> rcc.anyRequest().requiresSecure())
                .csrf(csrfConfig -> csrfConfig.disable())
                .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/myAccount","/myBalance").authenticated()
                .requestMatchers("/myCards","/error","/register", "/invalidSession").permitAll());
       // http.formLogin(formLoginConfig -> formLoginConfig.disable());
        http.formLogin(Customizer.withDefaults());
        http.httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()));
        http.exceptionHandling(handler -> {
            handler.accessDeniedHandler(new CustomAccessDenialException());
        });
        return http.build();
    }

```

10. Authentication events
    - When a user is successfully logged in or failed to login then we can catch an authentication event to perform some actions like sending email

```
    @Component
    @Slf4j  // from lombok library
    public class AuthenticationEvents {

        @EventListener
        public void onSuccess(AuthenticationSuccessEvent event) {
            log.info("Login successful for the user : {}", event.getAuthentication().getName());
        }

        @EventListener
        public void onFailure(AbstractAuthenticationFailureEvent failureEvent) {
            log.error("Login failed for the user : {} due to : {}", failureEvent.getAuthentication().getName(),
                    failureEvent.getException().getMessage());
        }
    }
```

11. Form login configuration
    - Configuring different login config using SecurityFilterChain

```
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

        http.csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests((requests) -> requests.requestMatchers("/dashboard").authenticated()
                        .requestMatchers("/", "/home", "/holidays/**", "/contact", "/saveMsg",
                                "/courses", "/about", "/assets/**", "/login/**").permitAll())
                .formLogin(flc -> flc.loginPage("/login").usernameParameter("userid").passwordParameter("secretPwd")
                        .defaultSuccessUrl("/dashboard").failureUrl("/login?error=true")
                        .successHandler(authenticationSuccessHandler).failureHandler(authenticationFailureHandler))
                .logout(loc -> loc.logoutSuccessUrl("/login?logout=true").invalidateHttpSession(true).clearAuthentication(true)
                        .deleteCookies("JSESSIONID"))
                .httpBasic(Customizer.withDefaults());


        return http.build();
    }

```

12. SecurityContext
    - Stores the authenticated user details with its JSESSION ID
    - Authentication object contains principal, credentials, authorities and isAuthenticated
    - SecurityContext is maintained by the SecurityContextHolder class (CRUD operations and other utility methods)
    - Holding strategy for the security SecurityContext 
        - MODE_THREADLOCAL (default) - Allows each thread to store its own details in security context 
        - MODE_INHERITABLETHREADLOCAL - One thread can inhirit the security context details of another one
        - MODE_GLOBAL - All the threads see the same Security context instance (not for web development)

13. Loading login user details
    - Using th SecurityContextHolder class
    - Or take Authentication as the parameter in controller class

```
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    authentication.getName();

    OR

    @GetMapping("/username)
    public String currentUsername(Authentication authentication) {
        return authentication.getName();
    }
```
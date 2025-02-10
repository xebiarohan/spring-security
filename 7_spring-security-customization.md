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
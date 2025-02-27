1. Cross origin resource sharing and cross site request forgery

2. CORS
   - It is an protocol that enables script running on a browser client to interact with resources from a different origin
   - Origin consist of 3 things
     - Protocol (HTTP or HTTPS)
     - domain
     - port
   - By default a browser will not allow it (CORS communication)
   - Example frontend (running on 4200 port) communicating with backend (running on 8080 port)

3. Handling CORS
    - If we have some public APIs then we can use @CrossOrigin annotation on the Controller class
    - This approach is not good as we have to put the annotation on each controller 
    - Spring security provides a better approach
    - Set the CORS security details in the SecurityFilterChain

```
@CrossOrigin(origins = "http://localhost:4200")
  OR
@CrossOrigin(origins = "*")
```

```

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.cors(corsConfig -> corsConfig.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration config = new CorsConfiguration();
                        config.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
                        config.setAllowedMethods(Collections.singletonList("*"));
                        config.setAllowCredentials(true);
                        config.setAllowedHeaders(Collections.singletonList("*"));
                        config.setMaxAge(3600L);
                        return config;
                    }
                }))
                .sessionManagement(smc -> {
                    smc.invalidSessionUrl("/invalidSession").maximumSessions(1).maxSessionsPreventsLogin(true);
                    smc.sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::changeSessionId);

                })
                .requiresChannel(rcc -> rcc.anyRequest().requiresSecure())
                .csrf(csrfConfig -> csrfConfig.disable())
                .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/myAccount","/myBalance","/user").authenticated()
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

4. CSRF (Cross site request forgery)
   - By default spring security will block all the requests that alters the data like POST,PUT, DELETE, PATCH
   - Either we need to pass a CSRF token with each request or disable the csrf security (not recommended)
   - CSRF is an attack that aims to perform an operation in the web application on behalf of a user without their concent
   - Example
     - User uses a website like Netflix.com
     - On login a cookie (JSESSIONID) is created that will be sent to all the further request to Netflix.com
     - Then the user moved to a different website (maintened by a hacker)
     - User clicks on a link, which can submit a form that is not visible to the user
     - That form can send a request to Netflix website with the cookie present in the browser
     - Hacker can change the password of netflix, change the email id, etc 

5. Solution of CSRF attack
    - By default the backend servers dont know the request comes from the original website or any other source
    - So we can use the CSRF token
    - CSRF token is a unique (per session) large random value
    - It is also known as XSRF token
    - How it works
      - User logs in Netflix.com
      - This time 2 cookies are generated (JSESSIONID and CSRF token)
      - From then on any further request to Netflix, these 2 cookies are attached in the request
      - And the backend also expects the CSRF token to be present either in header or the payload (etra security)
      - When the user clicks on a bad link on a evil website that is trying to update some data on netflix
      - the browser will automatically adds the 2 cookies in the request
      - but the CSRF token cannot be added to the payload or the header so the request will be rejected with 403
  
6. Implementing CSRF in spring security
    - CsrfToken is an interface
    - There are many implementation but by default spring security uses DefaultCsrfToken implementation
    - CsrfFilter is used to validate the request that they contain the correct CSRF token
    - implementation
      - Need to add config in SecurityFilterChain
      - Here CsrfCookieFilter is a custom filter that we created and calling it after the BasicAuthenticationFilter to generate a CSRF token
      - CsrdTokenRequestAttributeHandler is used to create an request object with CSRF token in it, that is used by the CsrfFilter for validating token
```
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler = new CsrfTokenRequestAttributeHandler();
        http.cors(corsConfig -> corsConfig.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration config = new CorsConfiguration();
                        config.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
                        config.setAllowedMethods(Collections.singletonList("*"));
                        config.setAllowCredentials(true);
                        config.setAllowedHeaders(Collections.singletonList("*"));
                        config.setMaxAge(3600L);
                        return config;
                    }
                }))
                .sessionManagement(smc -> smc.invalidSessionUrl("/invalidSession").maximumSessions(3).maxSessionsPreventsLogin(false))
                .requiresChannel(rcc -> rcc.anyRequest().requiresInsecure())
                .csrf(csrfConfig -> csrfConfig.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/myAccount", "/myBalance","/user").authenticated()
                        .requestMatchers("/myCards", "/error", "/register","/invalidSession","/notices").permitAll());
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
public class CsrfCookieFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        csrfToken.getToken();
        filterChain.doFilter(request,response);
    }
}

```

7. If we are using a custom login page then by default the spring security will not share the JSESSIONID bewteen request after login
   - We need to set session management config in the SecurityFilterChain
   - We have to set Session creation policy to Always

```
                .sessionManagement(smc -> {
                    smc.invalidSessionUrl("/invalidSession").maximumSessions(1).maxSessionsPreventsLogin(true);
                    smc.sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::changeSessionId);
                    smc.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);

                })

```

8. By default the Spring security will not store the JSESSIONID in security contect for that we need to again update the SecurityFilterChain

```
http.securityContext(context -> context.requireExplicitSave(false))

```

9. CSRF working
    - When ever the users login and sends the first request, the CSRF token is generated
    - we can see in the response cookie session (with name XSRF token)
    - We can ignore the CSRF token check for public APIs
  
10. Ignoring CSRF token for public APIs
    - The APIs that we are trying to ignore CSRF token chcek must be a part of permitAll in SecurityFilterChain config
    -  Config

```
                .csrf(csrfConfig -> csrfConfig.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/contact","/error","/register"))

```
1. By default spring-security tries to secure all the endpoints in our application.
    - we can update this configuration
    - this default config is present in SpringBootWebSecurityConfiguration

```
Default config in SpringBootWebSecurityConfiguration

		@Bean
		@Order(SecurityProperties.BASIC_AUTH_ORDER)
		SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
			http.authorizeHttpRequests((requests) -> requests.anyRequest().authenticated());
			http.formLogin(withDefaults());
			http.httpBasic(withDefaults());
			return http.build();
		}
```

2. We can override this basic config in a configuration file
    -  here we have different methods on the object returned by anyRequest() like authenticated(), permitAll(), denyAll(), etc
    -  permitAll() will bypass the spring security check for given URLs.
    -  denyAll() will deny all the requests, user gets the login page first then even on entering the right credentials the user will get denied access (403)
    -  We can use request matchers to define a set of endpoints
    -  when we get an error, the spring security sends us to /error endpoint and by default even that is secured by spring-security So we have to add it in permitAll().


```
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((requests) -> requests
                .requestMatchers("myAccount").authenticated()
                .requestMatchers("/myCards","/error").permitAll());
        http.formLogin(Customizer.withDefaults());
        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }
```

3. By default we get both types of authentication
    -  http.formLogin(Customizer.withDefaults()); this is used to enable the login form based authentication
    -  http.httpBasic(Customizer.withDefaults()); this is used to enable basic authentication (called by other applications or components with authorization header)
    -  we can enable or disable these as per our requirement
    - To disable it remove the withDefaults() method and disable it
    - we can more other different types of login ways like with JWT token.

```
    http.formLogin(formLoginConfig -> formLoginConfig.disable());
```
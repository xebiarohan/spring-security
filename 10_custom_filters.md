1. Filters
   - Are used to intercept all the requests that are coming to the application
   - If there are multiple filters then they get executed in a sequential manner
   - we can have our own custom filter and can add it in filter chain
   - Custom filters for like loggin, auditing, input validation, Encryption and decryption
   - To check the internal working of the filters, we can add an EnableWebSecurity annotation on the main application class like
   - After adding of the annotation we can see all the filters in the logs on any request execution in ordered manner

```
    @SpringBootApplication
    @EnableWebSecurity(debug = true)
    public class EasyBankApplication {

        public static void main(String[] args) {
            SpringApplication.run(EasyBankApplication.class, args);  
        }

    }
```

2. Creating custom filter
   - 1st way is to implement Filter interface (jakarta.servlet.Filter)
     - the init method of the Filter is called at the startup of the web application
     - the destroy method is called at the shutdown process of the application
   - 2nd way to generate a custom filter is to extend GenericFilterBean abstract class
     - GenericFilterBean is implementing Filter interface
     - This abstract class gives many methods to work with servlet information
   - 3rd way is to extend OncePerRequestFilter abstract class
     - It is extending GenericFilterBean
     - This filter will be executed maximum 1 type per request
     - we have methods like shouldNotFilter where we can write the logic to filter some requests for which we do not want to execute the filter
   - we need to write our code in doFilter method

3. Injecting custom filter in Filter chain
   - we have to use SecurityFilterChain to add our custom filter in the filter chain
   - we have methods like addFilterAfter, addFilterBefore, addFilter, addFilterAt
   - addFilter (this method is not recommended) method will read the order number from the custom filter and execute it at that place

```
        .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
        .addFilterBefore(new RequestValidationFilter(),BasicAuthenticationFilter.class)
        .addFilterAfter(new AuthoritiesLoggingAfterFilter(), BasicAuthenticationFilter.class)
        .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/myAccount").hasRole("USER")
                .requestMatchers("/myBalance").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/myLoans").hasRole("USER")
                .requestMatchers("/myCards").hasRole("USER")
        .requestMatchers("/contact","/error","/register", "/invalidSession").permitAll());

```

4. Example of custom filter
   - @Slf4j is used for adding log
   
```
@Slf4j
public class AuthoritiesLoggingAfterFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(null != authentication) {
            log.info("User " + authentication.getName() + " is successfully authenticated and "
                    + "has the authorities " + authentication.getAuthorities().toString());
        }
        chain.doFilter(request,response);
    }
}
```
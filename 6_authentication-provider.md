1. DaoAuthenticationProvider is the default authentication provider

2. We can have our own custom authentication provider

3. We need to have our own authentication provider when we need to provide login access using mutiple ways like
   - Through login page (username-password)
   - Using OAuth2 
   - Using JAAS

4. For each type we need to have a seperate authentication provider. based on the request the Authentication manager will invoke the specific authentication provider. Authentication manager selects the authentication provider based on the Authentication object

5. AuthenticationProvider interface has 2 methods
   - authenticate: here it calls the UserDetailsService and PasswordEncoder for password validation
   - supports: Tells what kind of authentication this provider supports like UsernamePasswordAuthenticationToken, OidcLogoutAuthenticationToken, etc

6. Authentication manager reads the supports method of all the providers to see which provider supports what kind of authentication and that is how it selects the authentication provider for authentication

7. In case there are multiple Authentication providers of same type then if any one can authenticates the user. need not be authenticated will all the providers

8. Creating custom Authentication provider
   - Create a class and implement AuthenticationProvider interface
   - @Component annottation on the class
   - Set the supports method to UsernamePasswordAuthenticationToken
   - Implement the authentication function

```

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if(passwordEncoder.matches(password, userDetails.getPassword())) {
            return new UsernamePasswordAuthenticationToken(username, password, userDetails.getAuthorities());
        } else {
            throw new BadCredentialsException("Invalid credentials");
        }
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

```

9. Profiling
    -  We can have different environment like dev,stg, prod, etc
    -  We can have different profiles for different environment
    -  We can have different configuration and different set of beans for different environment using profiles
    -  We need to create new application properties file like application_<env-name>.properties
    -  example application_prod.properties
    -  and need to mention those file names in application.properties
    -  and add activate config in prod properties
    -  we can change the profile using the environment variables as well, then the spring.profiles.active value will be ignored
    -  Environment variable SPRING_PROFILES_ACTIVE = prod
    -  Set the environment variable by right click on the run button in the Application.java
       -  Select Modify Run configuration
       -  add the environment variable in Environment variables input

```
      spring.config.import = application_prod.properties, application_dev.properties
      spring.profiles.active=prod


      In application_prod.properties
      spring.config.activate.on-profile= prod
```

10. we can then set the bean in a particular environment using @Profile annottation

```

    @Component
    @Profile("prod")
    @RequiredArgsConstructor
    public class EazyBankProdUsernamePasswordAuthenticationProvider implements AuthenticationProvider {
        ...
    }

    @Component
    @Profile("!prod")
    @RequiredArgsConstructor
    public class EazyBankUsernamePasswordAuthenticationProvider implements AuthenticationProvider {
        ...
    }


```
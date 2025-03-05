1. Authentication Vs Authorization
   - Authentication is if a user is valid or not (401 is failed case)
   - Authorization is what are the authority of the user example readonly, read-write and the action that user can take (403 in failed case)

2. How Authorities are stored in Spring security
    - GrantedAuthority is an interface that needs to be followed if we want to store any roles or authorities
    - SimpleGrantedAuthority is an example of implemetation
    - When some needs to store the authorities/roles then need to create an object of SimpleGrantedAuthority class
    - At the login time we loads the user stored in database using getUserByUsername, there we have to pass the authorities
    - example below
    - We can have multiple roles/authorities for a single user
    - Once the authentication is completed the UserDetails object is converted into Authentication object
    - Authenication object also handles the authorities (like in custom authentication provider)

```
@Service
@RequiredArgsConstructor
public class EazyBankUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User details not found for user: " + username));
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(customer.getRole()));
        return new User(customer.getEmail(), customer.getPwd(), authorities);
    }
}

```

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

```

3. Using authorities
   - Till now if a user is authenticated it can access all the endpoints that is present in the authenticated block of SecurityFilterChain
   - Now we have to use hasAuthority and hasAnyAuthority
   - we can use access method when we have complex condition to resolve authority (rarely used)

```Till now
    .authorizeHttpRequests((requests) -> requests
        .requestMatchers("/myAccount","/myBalance","/user","/myCards").authenticated()
        .requestMatchers("/contact","/error","/register", "/invalidSession").permitAll());
```

```NOW
    /*.requestMatchers("/myAccount").hasAuthority("VIEWACCOUNT")
    .requestMatchers("/myBalance").hasAnyAuthority("VIEWBALANCE", "VIEWACCOUNT")
    .requestMatchers("/myLoans").hasAuthority("VIEWLOANS")
    .requestMatchers("/myCards").hasAuthority("VIEWCARDS")*/

```


4. There is a slightly difference between the Authority and Role
   - Authority is the access to do something like creating user, deleting user, reading user details, etc
   - Role is the designation like ADMIN, READ_ONLY_USER, SUPER_USER, etc
   - Authorities are assigned to a role
   - In Spring security we have to store the roles with ROLE_ as prefix example ROLE_ADMIN, ROLE_USER, etc
   - We can update this prefix if we want by creating a bean of GrantedAuthorityDefaults
   - In hasRole and hasAnyRole we dont need to add the prefix (need to add it in database)


```with Role
    .authorizeHttpRequests((requests) -> requests
            .requestMatchers("/myAccount").hasRole("USER")
            .requestMatchers("/myBalance").hasAnyRole("USER", "ADMIN")
            .requestMatchers("/myLoans").hasRole("USER")
            .requestMatchers("/myCards").hasRole("USER")
            .requestMatchers("/user").authenticated()
            //  .requestMatchers("/myAccount", "/myBalance","/user","/contact").authenticated()
            .requestMatchers("/myCards", "/error", "/register","/invalidSession","/notices").permitAll());
```

5. Listening to authorization event
   - When ever the authorization fails there is an event published (AuthorizationDeniedEvent) by the spring security
   - we can listen to it and can perform some action
   - we can also listen to the AuthorizationGrantedEvent but it will be too much logs to handle.

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
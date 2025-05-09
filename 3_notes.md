1. If we add the user and password in the application.properties then we can define only 1 user but mostly we need more than 1 user in our applicaiton.

2. To make application feasible for multiple user we have to remove the used details from the application.properies and add config in a configuration file in form of a bean
   - It will store the user details in the local application memory

3. Config
    - Here we are creating 2 different users
    - Here we can define their authorities as well
    - We want to store the users in the application memory that is why we are returning InMemoryUserDetailsManager object
    - InMemoryUserDetailsManager object can take any number of UserDetails object
    - Internally all the users are stored in a HashMap that gets loaded at the startup of the application
    - UserDetailsService is an interface, here we are using the InMenoryDetailsManager but we can store the details at several places like in database, other application, etc

```
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("user").password("12345").authorities("read").build();
        UserDetails admin = User.withUsername("admin").password("54321").authorities("admin").build();
        return new InMemoryUserDetailsManager(user,admin);
    }

```

4.  Password encoder
    - The problem with above apporach is that we are storing the password in plain text and in the code.
    - To use password in encrypted form we needs a password encoder (a bean in a configuration file)
    - First encrypt the plain password using bcrypt (like using https://bcrypt-generator.com/)
    - For 1 user we encrypted the password and add the type in front of it (so that password encoder knows how to read it)
    - For second we kept it in plain text (used noop to tell password encoder that it is plain text)
    - We are using createDelegatingPasswordEncoder method as its default value is bcrypt encryption
    - Password encoder is used to match the entered password and the stored password in the user details

```
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("user").password("{noop}12345").authorities("read").build();
        UserDetails admin = User.withUsername("admin").password("{bcrypt}$2a$12$XsJ4IuuKrJLnYNknBhDqdOLQSuksmYjgg/juRsaDVNrLM8Y3FjyiW").authorities("admin").build();
        return new InMemoryUserDetailsManager(user,admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
```

1. Restrict user from using simple passwords
    - We can use a bean called CompromisedPasswordChecker
    - It checks if a password is compromised in any data breach (means its a very simple passowrd to hack)

```
    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

```

6. User Management (UserDetails)
   - Divided into 2 parts UserDetailsService and UserDetailsManager
   - UserDetailsService loads the user based on the username entered by the user while login (loadUserByUsername API)
   - UserDetailsManager provides some extra endpoints like updateUser, changePassword, deleteUser, etc.
   - UserDetailsManager extends the UserDetailsService
   - Based on the requirement of an application we can either implement UserDetailsService or UserDetailsManager
   - Some of the implentations of UserDetailsManager
     - InMemoryUserDetailsManager
     - JdbcUserDetailsManager
     - LdapUserDetailsManager
   - we can also implement our own implementation of UserDetailsManager
   - UserDetails is also an interface that has many implemntations like User. It holds the user details and is used by the UserDetailsService and UserDetailsManager.

7. There are 2 types of contracts (Object) used in the flow
   - Authentication
   - UserDetails
   - Authentication is created by the Authentication interface (UsernamePasswordAuthenticationToken is one of its concrete class) and shared till the Authentication provider
   - Then Authentication provider extracts the user name and calls the loadUserByUsername API of UserDetailsService
   - The UserDetailsService returns the UserDetails object (User is one of the concrete class of UserDetails interface)
   - Authentication provider extracts the details from UserDetails and Authentication objects and passes it to the Password proxy
   - Then updates the isAuthenticated value of the Authentication object based on the output of Password proxy and returns it back to the Authentication manager -> filters.
  
8. If we dont provide a custom Authentication provider then by default it uses DaoAuthenticationProvider
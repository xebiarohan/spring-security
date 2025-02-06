1. Docker container
   - docker run -p 3306:3306 --name springsecurity -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=eazybank -d mysql
   - Downloaded SQL client: SQl electron
  
2. When we are using database to store the user details then we have to use JdbcUserDetailsManager class

3. Tables that we can create in the database is defined in
   - users.ddl
   - Need to change varchar_ignorecase to varchar
```
create table users(username varchar_ignorecase(50) not null primary key,password varchar_ignorecase(500) not null,enabled boolean not null);
create table authorities (username varchar_ignorecase(50) not null,authority varchar_ignorecase(50) not null,constraint fk_authorities_users foreign key(username) references users(username));
create unique index ix_auth_username on authorities (username,authority);

```

4. Need to add the below dependencies
   - JDBC API
   - MySQL driver
   - Spring Data JPA

```
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>

    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <scope>runtime</scope>
    </dependency>
```

5. Adding config in application.properties
    - Database name
    - Database username
    - Database password
    - Do we need to see the SQL statement generated using JPA
    - SQL statement logged in hibernate format

```
    spring.datasource.url=jdbc:mysql://${DATABASE_HOST:localhost}:${DATABASE_PORT:3306}/${DATABASE_NAME:eazybank}
    spring.datasource.username=${DATABASE_USERNAME:root}
    spring.datasource.password=${DATABASE_PASSWORD:root}
    spring.jpa.show-sql=${JPA_SHOW_SQL:true}
    spring.jpa.properties.hibernate.format_sql=${HIBERNATE_FORMAT_SQL:true}

```

6. We can use the custom table format that the spring security JdbcUserDetailsManager provider or we can have our own table structure as well.
   
7. Add the lombok dependency. It gives annotations like
    - @Getter
    - @Setter
    - @RequiredArgsConstructor : It is used for autowiring

8. Using custom class
  - First create a table in database

```
    CREATE TABLE `customer` (
      `id` int NOT NULL AUTO_INCREMENT,
      `email` varchar(45) NOT NULL,
      `pwd` varchar(200) NOT NULL,
      `role` varchar(45) NOT NULL,
      PRIMARY KEY (`id`)
    );

```

  - Then create a model
```
    @Entity
    @Table(name="customer")
    @Getter
    @Setter
    public class Customer {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long id;
        private String email;

        @Column(name="pwd")
        private String pwd;
        private String role;
    }
```

  - Then create a JPA repository
```
    @Repository
    public interface CustomerRepository extends CrudRepository<Customer, Long> {

        Optional<Customer> findByEmail(String email);
    }

```

8. Updating UserDetailsManager
   - when we stores the users inmemory then we user InMemoryUserDetailsManager
   - When we stored in the predefined database classes then we used JdbcUserDetailsManager
   - For custom class we have to provide our own implementation
   - Implementation should implement either UserDetailsService(better approach) or the UserDetailsManager
   - If we implement UserDetailsService then we have to implement only 1 method loadUserByUsername()
   - Then the Authentication provider (DaoAuthentication provider by default) will use our custom class to load the user details
   - Make sure there is no UserDetailsService bean present in the configuration class

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

9. Spring security adds CSRF protection for data posting endpoints like POST, PUT, PATCH, etc
    - If we add a POST request to permitAll
    - It will still fail with 403 
    - But a GET request in permitAll will work fine
    - Either we can implement the CSRF protection properly or just diable it

```
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrfConfig -> csrfConfig.disable())
                .authorizeHttpRequests((requests) -> requests
                .requestMatchers("myAccount","/myBalance").authenticated()
                .requestMatchers("/myCards","/error","/register").permitAll());
       // http.formLogin(formLoginConfig -> formLoginConfig.disable());
        http.formLogin(Customizer.withDefaults());
        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }

```

10. Creating a new user using rest endpoint
    - we have to expose a rest endpoint 
    - Need to use PasswordEncoder to convert the plain text password entered by user into encrypted form
```
  @RestController
  @RequiredArgsConstructor
  public class UserController {

      private final CustomerRepository customerRepository;
      private final PasswordEncoder passwordEncoder;
      @PostMapping("/register")
      public ResponseEntity<String> regiterUser(@RequestBody Customer customer) {
          try {
              String hasPwd = passwordEncoder.encode(customer.getPwd());
              customer.setPwd(hasPwd);
              Customer savedCustomer = customerRepository.save(customer);
              if(savedCustomer.getId() > 0) {
                  return ResponseEntity.status(HttpStatus.CREATED)
                          .body("Given user details are successfully registered");
              } else {
                  return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                          .body("User registration failed");
              }
          } catch (Exception ex) {
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                      .body("An exception occurred: " + ex.getMessage());
          }
      }
  }
```
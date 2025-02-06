1. How Password encoder encodes the password when we create a new user
   - we use passwordencoder.encode(<plain-text-password>);
   - And the password encoder bean decided which enryption we are using
   - createDelegatingPasswordEncoder() method default value is bcrypt

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

```
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
```

2. Different ways of data privacy
   - Encoding
       - Converting data from one form to another and nothing to do with Cryptography. No secrets and completely reversible example Base64, Unicode, ASCII, etc
   - Encryption
       - Converting data using key that guarentees confidentiality, key can be used to extract the data 
       - need to use the same encryption algo and same key
       - types of encryption : Symmetric encryption and Asymmetric encryption
       - Symmetric encryption when same key is used for encryption and decryption
       - Asymmetric encryption when we have public-private key. public key for encryption and private for decryption
   - Hashing
       - Data is converted to hash value using some hash function
       - Data once hashed cannot be reversed
       - For same value hash function will always produce the same hash value
       - For example if we use SHA-256 hashing function then it will always produce 256 bits hash value

3. Drawbacks of hashing
     - Hashing algo generates the same hash value for the same input (password) which can allow Brute force attack or dictionary table attack
     - If 2 user has same passoword then they will get same Hash value

4. How to overcome drawbacks
   - Use Salts using password Hashing
     - It is a random value that gets added the the plain password text and then the whole value is hashed
     - Salt is stored with the hashed value in database
     - When that user logs back in then again first the salt value is added to the entered password, hash fn then converts the value and then comparison happen
     - So even if 2 users keeps the same password their Salt value will be different that will create different hash value.
     - Application should lock user on 3 wrong passwords
     - Simple passwords should not be allowed
     - Make Hash functionn slower, so that hacker cannot try millions of random passowrd to guess the right password
     - Bcrypt is one such algorithms (Scrypt, argon2 are other algo) that intentionally slows the hashing.
     - These algo needs lot of CPU and RAM memory
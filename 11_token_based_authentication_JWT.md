1. Token formats
   - Opaque tokens
     - random string value with no inherit meaning like JSESSIONID, XSRF-TOKEN
     - When a application  A needs to interact with another application (say B) then it first generates the token from an Auth server
     - Then it sends the request to application B, B will validate the token again with Auth server
     - Application B cannot validate the token itself
   - JSON web tokens (JWT)
     - token with details like signature, user details and token details
     - In this scenario the application B can validate the token itself by matching the token signature using a public key

2. Tokens advantages
   - Security
   - Expiration
   - Self-contained
   - Reuseability
   - Cross platform compatible
   - Statelessness

3. JWT tokens 
  - JWT tokens can be used for authorization as well as authentication
  - JWT can be of 2 parts or 3 parts divided by doit (.)
    - Header
    - Payload
    - Signature (optional)
  - Header contains the meta data that includes the type of the algo, type of token, expiration time, etc (Base64 encoded)
  - Payload contains the user information (Base64 encoded)
  - Signature is used for validating that the token is tempered or not (Digital signature)

4. Digital Signature is created using the an hashing algorithm
   - Hashing algo uses base 64 encoding value of header, base 64 encoding value of payload and a secret value to generate a digital signature

5. Dependencieies in Spring boot project

```
		<dependency>
			<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt-api</artifactId>
			<version>0.12.5</version>
		</dependency>
		<dependency>
			<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt-impl</artifactId>
			<version>0.12.5</version>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt-jackson</artifactId>
			<version>0.12.5</version>
			<scope>runtime</scope>
		</dependency>
```

6. Making session stateless
   - So that the backend will never store the token anywhere
   - backend will generate token and send it to the frontend, frontend needs to send the same token with every request
   - backend validate the token using the token content itself
   - Updating the session management config in SecurityFilterChain
   - Removing the security context as it is used for generating the JSESSIONID
   - Then in CORS config, we have to expose the Authorization header from backend to frontend, so that frontend can get the authorization token

```
      .sessionManagement(smc -> smc
              .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
              .invalidSessionUrl("/invalidSession").maximumSessions(3).maxSessionsPreventsLogin(false))

      Remove
      .securityContext(context -> context.requireExplicitSave(false))
```
```
      .cors(corsConfig -> corsConfig.configurationSource(new CorsConfigurationSource() {
          @Override
          public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
              CorsConfiguration config = new CorsConfiguration();
              config.setAllowedOrigins(Collections.singletonList("https://localhost:4200"));
              config.setAllowedMethods(Collections.singletonList("*"));
              config.setAllowCredentials(true);
              config.setAllowedHeaders(Collections.singletonList("*"));
              config.setExposedHeaders(List.of("Authorization"));
              config.setMaxAge(3600L);
              return config;
          }
      }))
```

7. Generating JWT token and validation
   - On user authentication, we can create a JWT token using a Filter
   - In this filter we will exclude all the login path, so that no token is generated when user is trying to login
   - Then we can have another filter where we can validate the token

```
public class JwtTokenGenerationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null != authentication) {
            Environment env = getEnvironment();
            if (null != env) {
                String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
                        ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                String jwt = Jwts.builder().issuer("Eazy Bank").subject("JWT Token")
                        .claim("username", authentication.getName())
                        .claim("authorities", authentication.getAuthorities().stream().map(
                                GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                        .issuedAt(new Date())
                        .expiration(new Date((new Date()).getTime() + 30000000))
                        .signWith(secretKey).compact();
                response.setHeader(ApplicationConstants.JWT_HEADER, jwt);
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !request.getServletPath().equals("/user");
    }
}
```

```
public class JWTTokenValidatorFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = request.getHeader(ApplicationConstants.JWT_HEADER);
        if(null != jwt) {
            try {
                Environment env = getEnvironment();
                if (null != env) {
                    String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
                            ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                    SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                    if(null !=secretKey) {
                        Claims claims = Jwts.parser().verifyWith(secretKey)
                                .build().parseSignedClaims(jwt).getPayload();
                        String username = String.valueOf(claims.get("username"));
                        String authorities = String.valueOf(claims.get("authorities"));
                        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null,
                                AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }

            } catch (Exception exception) {
                throw new BadCredentialsException("Invalid Token received!");
            }
        }
        filterChain.doFilter(request,response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getServletPath().equals("/user"); // user login path
    }
}
```

```
    .addFilterAfter(new JwtTokenGenerationFilter(), BasicAuthenticationFilter.class)
    .addFilterAfter(new JWTTokenValidatorFilter(),BasicAuthenticationFilter.class)
```
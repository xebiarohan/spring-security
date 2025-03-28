1. SecurityFilterChain 

```
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests((requests) -> requests.requestMatchers("/secure").authenticated()
                        .anyRequest().permitAll())
                .formLogin(Customizer.withDefaults())
                .oauth2Login(Customizer.withDefaults());
        return httpSecurity.build();
    }

```

2. As we are using the social login auth server, so we have to tell this to the SecurityFilterChain.
    - For that we have to create a bean of ClientRegistrationRepository
    - we can get the client id and client secret by registring on github and google respectively
    - For github- go to settings -> develop settings -> oAuth2 app -> register
    - For facebook we have to go to developers.facebook.com
    - Either can create a bean of ClientRegistration like below or we can use the configuration in the applicaiton.properties file

```
    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration github = githubClientRegistration();
        ClientRegistration facebook = facebookClientRegistration();
        return new InMemoryClientRegistrationRepository(github, facebook);
    }

    private ClientRegistration githubClientRegistration() {
        return CommonOAuth2Provider.GITHUB.getBuilder("github").clientId("Ov23liCBLLUjii41pS7k")
                .clientSecret("9da8734b56aad52d91b268fe6834a8df12447d95").build();
    }

    private ClientRegistration facebookClientRegistration() {
        return CommonOAuth2Provider.FACEBOOK.getBuilder("facebook").clientId("974042741122392")
                .clientSecret("36d48c25c1767d58b3101551513d7e1e").build();
    }
```

OR

```
spring.security.oauth2.client.registration.github.client-id=${GITHUB_CLIENT_ID:Ov23liCBLLUjii41pS7k}
spring.security.oauth2.client.registration.github.client-secret=${GITHUB_CLIENT_SECRET:9da8734b56aad52d91b268fe6834a8df12447d95}

spring.security.oauth2.client.registration.facebook.client-id=${FACEBOOK_CLIENT_ID:974042741122392}
spring.security.oauth2.client.registration.facebook.client-secret=${FACEBOOK_CLIENT_SECRET:36d48c25c1767d58b3101551513d7e1e}
```
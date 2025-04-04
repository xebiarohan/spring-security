1. Here we are going to convert our bank application into a resource server and angular HMI works as a client server

2. Keyclock will work as a auth server

3. Keycloak is an Opensource Identity and access management tool, there are other similar products available in market like Okta (paid), Amazon Cognito (in AWS)

4. Running keycloak docker container

```
        docker run -p 8080:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.1.4 start-dev
```

5. We have to create a realm, realm is an isloated space where we can create our users, credentials, roles and groups.

6. We have to register the client information in the Auth server (keycloak)

7. For that we have to create a client and assign the proper grant type. We will get a client secret after creating a client

8. To make the spring security app a resource server need to add oauth2 resource server dependency

```
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
```

9. As we are doing the authentication using the Keycloak and not doing it locally. So we dont need custom UserDetailsService and AuthenticationProvider
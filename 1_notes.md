1. Github repo
    - https://github.com/eazybytes/spring-security

2. In application.properties we can have dynamic application name
    - here if the APRING_APP_NAME is passed then that will be the app name else springsecsection1 will be the app name
    - we can do this for all the properties in application.properties file
```
spring.application.name=${SPRING_APP_NAME:springsecsection1}
```

3. To add the spring-security

```
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>

```

4. If you add the above dependency and run the application, then when you enter any url, you will get a login page
    - the default credentials are username : user and the password will come in the logs on startup
    - The code of how the default user and password is generated is present in SecurityProperties.java (spring framework class)
    - Password is random UUID

5. We can override the default username and password in the application.properties

```
    spring.security.user.name=${SECURITY_USERNAME:admin}
    spring.security.user.password=${SECURITY_PASSWORD:12345}
```

6. Security
    - We need security to protect important information like credit card details, customer personal information, etc
    - Only authenticated user can able to see their own information
    - Sometimes the application is secured to protect there internal flows like OPT generation flow, etc
    - With security we can avoid CSRF attacks, Session fixation attacks, XSS, CORS, etc.

7. Servlets and filters
    - Whenever we sends a request from a browser to backend, the HTTP/HTTPS request is converted by the Servlet container (web server like tomcat, JBoss) into ServletRequest object and hands over it to the corresponding Servlet as a parameter
    - Then servlet sends ServletResponse response object, then Servlet container changes this object back into HTTP/HTTPS response object and sends back to the browser.
    - Role of filters - All the requests and response between the client (browser) and the servlets are intercepted using the filters.
        - here we can add our logic 
        - Filters are part of the servlet contianer (web server)
        - Spring security uses these same filters to implement security
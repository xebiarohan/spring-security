1. Github repo
    - https://github.com/eazybytes/spring-security

2. In application.properties we can have dynamic application name
    - here if the SPRING_APP_NAME is passed then that will be the app name else springsecsection1 will be the app name
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

4. If you add the above dependency and run the application, then for everyurl you we try to access, we will get a login page
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
        - Filters are part of the servlet containers (web server)
        - Spring security uses these same filters to implement security

8. Spring security internal flow
    - When the user enters a URL and hits enter
    - The request is received by the filters of Servlet container
    - Then the filters checks for the given session id (JSessionID) is there any Authentication object present in component called Security Context
    - If present then forwards the request to its specific servlet else
    - Filters extracts the user credentials from the request (from header or body based on the configuration)
    - Converts the credetials into an Authentication object using Authentication interface (it has many implementation)
    - Authentication object contains fields like username, password, isAuthenticated, etc
    - At the time of object creation the isAuthenticated value is set to false.
    - This Authentication object is a common contract between all the components of Spring security
    
    - Then the filters sends this Authentication object to Authentication manager
    - Authentication manager sends the object further to Authentication provider
    - We can configure any Authentication provider as per our need
    - We can have more than 1 Authentication providers
    - Authentication manager sends the Authentication object to all the providers to check if user is valid

    - Authentication provider also takes help of 2 other component like User details manager and Password Encoder
    - User details manager is used to load the user details based on the username entered by the user.
    - Then the Authentication provider sends the loaded user details and the password entered by the user to the password encoder to compare the passwords.
    - When the password matches, the Authentication provider sends the Authentication object back to the Authentication manager with isAuthenticated as true.
    
    - Authentication manager sends the same object to the Spring security filters
    - Filters then store the Authentication result in a component named Security Context irrespective of the result
    - Result is stored in a key value pair with Session id (for a given browser session) as key and the Authentication object as value.
    - So that if the authentication is successful then from second request the whole authentication flow should not get executed.
    - Then the filters sends the request furtherr to the servlet to handle the request


9. AuthorizationFilter and DefaultLoginPageGeneratingFilter
    - It is the filter that checks if a user is trying to access an endpoint without a valid session
    - If user is not having valid credentials then it throws a Access denied exception
    - Then the flow reaches another filter DefaultLoginPageGeneratingFilter and redirects the user to the login page
    - Once we enters the credentials and presses enter then the flow goes to AbstractAuthenticationProcessionFilter (abstract class)

10. How HMI handles requests after successful login
    - In the cookies there is a cookie named JSESSIONID
    - With every request from the HMI the ID goes to the backend filters
    - Backend filters checks if there is a valid session present for this given ID in security context
    - If it is present then it forwards the request to the corresponding servlet
    - Else it will forwards the request to DefaultLoginPageGeneratingFilter and returns the login page.
1. One common Authentication server
   1. If we want to login in 1 application and want to use multiple applications just like (gmail, google maps, photos, etc)
   2. we need to maintain a seperate authentication server that can be used by these applications
   3. So that if a user logs in 1 application then there should not be a need to login again even if a different application is accessed by the user

2. The other reason to have a seperate authentication server is to keep the security related code at a single place. Other component need not handle the same boilerplace code.

3. The OAuth2 also solves the problem where we want to provide a limited access to a 3rd party to access data from our account. So using OAuth2 we can create a temporary token and can provide it to the 3rd party application. Token provides a limited read only access for some limited time period.

4. OAuth -> Open Authorization is a security statndard used for authentication and authorization

5. OAuth 2 terminology
   1. Resource owner - The end user who is the owner of the resource
   2. Client - The one which want access to the resource (example 3rd party application)
   3. Authorization server - The server that validates the credentials of the resource owner before generating a temporary token for 3rd party (example google auth server)
   4. Resource server - where the resources are stored
   5. Scopes - the granular permission that client wants to access the resources example reading email, reading username, accessing photos, etc

6. Auth2 sample flow
   1. PhotoEditor applicaition wants to use google login as a way of login in there application
   2. PhotoEditor shares the details of there application with google and gets a Client id and client Secret
   3. User comes on the PhotoEditor application and clicks on the login with Google button
   4. User redirected to the google login page
   5. Once the login is successful the user displayed a concent page where the user details that will be shared with the PhotoEditor application is listed
   6. User needs to give concent by clicking on the continue
   7. Google Auth server issues an access token and refresh token to the PhotoEditor application
   8. PhotoEditior App uses this access token to interact with the resource server
   9. Resource server validates the token coming with request with the Auth server and then returns the response object
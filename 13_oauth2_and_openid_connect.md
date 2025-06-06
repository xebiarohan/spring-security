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
   1. Let say we want to give access to the PhotoEditor application to access photos from our google photos account.
   2. PhotoEditor applicaition wants to use google login as a way of login in there application
   3. PhotoEditor shares its with google and gets a Client id and client Secret (one time thing per application, not per user)
   4. User comes on the PhotoEditor application and clicks on the login with Google button
   5. User redirected to the google login page
   6. Once the login is successful the user displayed a concent page where the user details that will be shared with the PhotoEditor application is listed
   7. User needs to give concent by clicking on the continue
   8. Google Auth server issues an access token and refresh token to the PhotoEditor application
   9. PhotoEditior App uses this access token to interact with the resource server
   10. Resource server validates the token coming with request with the Auth server and then returns the response object

7.  Grant types
    1.  Authorization grant type
        1.  Used when end user is involved, when ever end user enters some credentials
   
8.  Authorization Grant type
    1.  UUser tries to access the resource present in resource server through client application
    2.  Client ask to select one authentication server to authenticate the identity, so that the client app can get a token
    3.  Then user selects an auth server (like google auth server)
    4.  User enters the credetials, the auth server validates the credentials and returns an AUTHORIZATION_CODE to the client application
    5.  Then the client application sends a request with the client credentials (client id and secret value) with the AITHORIZATION_CODE to get an access token
    6. If the values are valid then auth server will issue an access token
    7. Then client application sends a request to the resource server with the access token to access the resources.
    8. Resource server validates the access token itself (digital signature) and if valid then returns the requested resource
    9. When client sends the request to get AUTHORIZATION_CODE, it sends some extra information
      - client_id - id of the client
      - redirect_url - where the auth server needs to redirect after authentication
      - scope -  what are the level of permissions that the client wants from the auth server
      - state - CSRF token value to protect from CSRF attacks
      - response_type - with the 'code' value to indicate that we want to follow authorization code grant
    10. When client sends a request to get the access token, it sends some extra information
      - code - authorization code value
      - client id and client secret
      - grant_type - 'authorization_code' value
      - redirect_uri - where the client wants to redirect after getting the access token

9. Implicit grant flow (depricated)
   1.  User tries to access the resource present in resource server through client application
   2.  Client application redirects the user to login page of some Auth server (based on auth server selected by the user)
   3.  When user enters the valid credentials
   4.  Auth server issues directly the access token (no AUTHORIZATION_CODE)
   5.  Client application then uses the access token to send a request to resource server
   6.  If access token is valid then the resource server is going to share the requested resource

10. PKCE grant type (Proof key for code exchange)
    1.  It is similar to the Authorization grant type flow with a minor change
    2.  It is mainly used for the javascript based client application that cannot store client secret value
    3. Working
         - When the client application goes to the Auth server to authenticate, client derives code_verifier and code_challenge
         - code_challenge string is derived from code_verifier using SHA256 (hash value)
         - When the client is redirecting to the login page of Auth server, it shares code_challenge value
         - Auth server stores the code_challenge value and on authentication shares the AUTHORIZATION_CODE with the client
         - When the client server sends the request to get the access token, it sends the code_verifier with the AUTHORIZATION_CODE
         - Auth server uses the same encoding algo (SHA256) and matches it with stored code_challenge value

11. Client credentials Grant type
    1. When no user is involved and when 2 backend application wants to communicate
    2. Client sends the request to the Auth server with client ID and client secret
    3. Auth server validates the client credentials and issues an access token
    4. Client application uses that token to communicate with the resource server
    5. Resource server validates the token and sends the response to the client application

12. Refresh token Grant type
    1.  When we use the Authorization grant type flow or PKCE grant type flow, we get refresh token along with access token
    2.  When the access token gets expired then this refresh token is used to generate a new access token using the refresh grant type flow
    3.  If we send offline_access in the scope object in the request to Auth server then it will generate a refresh token that never expires
    4.  Flow
        -  Client application sends a request to the resource server with the access token
        -  Resource server validates the access token, in case it is expired, it returns 403 forbidden error message
        -  Now the client application sends a request to the Auth server with the refresh token to get a new set of tokens
        -  Auth server validates the refresh token and shares a new pair of access token and refresh token
        -  Client application can use the new access token to send a new request to the resource server
  
13. How resource server validates the token 
    - There are 2 ways we can validate the access token at the resource server end
      - Validating it remotely (not the best approach)
        - Resource server sends the access token to the Auth server for validation
      - Validating it locally
        - Token must be of type JWT
        - Auth server uses a private key to generate the tokens
        - Corresponding to that private key there will be a public key, which the resource server needs to download during the startup of the application
        - Using this public key the resource server can validate the digital signature of the token is valid or not
        - At the startup of the application the resource server sends a request to the Auth server to download the public key

14. OpenID connect
    - It is a protocol that sits on top OAuth 2.0
    - OAuth 2.0 primiary role is to provide Authorization using the scopes
    - To use the OAuth 2.0 for authentication, a wrapper was intorduced - OpenID connect
    - To enable the OpenID connect, we just have to add 'openid' as a scope value
    - Auth server will start sending ID token along with access token and refresh token
    - ID token contains the user details that can be used for the authorization
    - OIDC exposes an '/userinfo' endpoint to get the user details (same as in the ID token)
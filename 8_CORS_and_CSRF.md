1. Cross origin resource sharing and cross site request forgery

2. CORS
   - It is an protocol that enables script running on a browser client to interact with resources from a different origin
   - Origin consist of 3 things
     - Protocol (HTTP or HTTPS)
     - domain
     - port
   - By default a browser will not allow it (CORS communication)
   - Example frontend (running on 4200 port) communicating with backend (running on 8080 port)
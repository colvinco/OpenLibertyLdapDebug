1. Create the LDAP server using the OpenLDAP docker image by running the `createLdapContainer.bat` or run the following commands directly
    ```
    docker run -p 389:389 -p 636:636 --name my-openldap-container --detach osixia/openldap:1.5.0
    docker cp testing.ldif my-openldap-container:/container
    docker exec my-openldap-container ldapadd -x -D "cn=admin,dc=example,dc=org" -w admin -f /container/testing.ldif -H ldap://localhost -ZZ
    docker exec my-openldap-container ldapsearch -x -H ldap://localhost -b dc=example,dc=org -D "cn=admin,dc=example,dc=org" -w admin -ZZ
    ```
2. Run the server application using `mvn liberty:run`
3. Connect to http://localhost:9080/system/properties using `Ollie:Passw0rd` as the credentials.
4. Observe that the response contains the following (amongst other things):
   ```json
   {
    "summary": {
        "callerPrincipal": "Ollie's Display Name",
        "securityName": "Ollie's Display Name",
        "securityNameFoundInRegistry": false,
        "uniqueSecurityName": "Ollie@example.org",
        "uniqueSecurityNameFoundInRegistry": true
    },
    "usersFromRegistry": [
        ...
        {
            "id": "Ollie@example.org",
            "userName": "Ollie",
            "displayName": "Ollie's Display Name"
        }
       ...
       "callerSubject": {
            "principals": [
                {
                    "name": "Ollie"
                }
            ],
            "publicCredentials": [
                {
                    "securityName": "Ollie's Display Name",
                    "uniqueSecurityName": "Ollie@example.org"
                }
            ]
        }
   ```
   - The securityName / callerPrincipal returned from the Subject are the display name.
   - The values returned from the registry are correct
   - The securityName on the subject `Principal` is correct but the `securityName` on the `WSCredential` is the display name.
   

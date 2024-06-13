docker stop my-openldap-container
docker rm my-openldap-container
docker run -p 389:389 -p 636:636 --name my-openldap-container --detach osixia/openldap:1.5.0
docker cp testing.ldif my-openldap-container:/container
@ping -n 2 -w 1 0.0.0.0 > nul
docker exec my-openldap-container ldapadd -x -D "cn=admin,dc=example,dc=org" -w admin -f /container/testing.ldif -H ldap://localhost -ZZ
docker exec my-openldap-container ldapsearch -x -H ldap://localhost -b dc=example,dc=org -D "cn=admin,dc=example,dc=org" -w admin -ZZ

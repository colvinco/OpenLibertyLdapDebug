// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2017, 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
// end::copyright[]
package io.openliberty.sample.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.security.auth.Subject;

import com.ibm.websphere.security.UserRegistry;
import com.ibm.websphere.security.auth.WSSubject;
import com.ibm.websphere.security.cred.WSCredential;
import com.ibm.wsspi.security.registry.RegistryHelper;

import jakarta.enterprise.context.RequestScoped;
import jakarta.json.bind.annotation.JsonbPropertyOrder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/properties")
public class SystemResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProperties() throws Exception {
        final List<RegistryUser> usersFromRegistry = getRegistryUsers();
        final Set<String> userIds = usersFromRegistry.stream().map(u -> u.id).collect(Collectors.toSet());
        final Set<String> userNames = usersFromRegistry.stream().map(u -> u.userName).collect(Collectors.toSet());
        final Set<String> userDisplayNames = usersFromRegistry.stream().map(u -> u.displayName).collect(Collectors.toSet());

        final String callerPrincipal = WSSubject.getCallerPrincipal();
        final Subject callerSubject = WSSubject.getCallerSubject();

        final Map<String, Object> summary = new TreeMap<>();
        summary.put("callerPrincipal", callerPrincipal);
        callerSubject.getPublicCredentials(WSCredential.class).forEach(cred -> {
            try {
                String securityName = cred.getSecurityName();
                if (securityName != null) {
                    final boolean matched = userNames.contains(securityName);
                    summary.put("securityName", securityName);
                    summary.put("securityNameFoundInRegistry", matched);
                }
                String uniqueSecurityName = cred.getUniqueSecurityName();
                if (uniqueSecurityName != null) {
                    final boolean matched = userIds.contains(uniqueSecurityName);
                    summary.put("uniqueSecurityName", uniqueSecurityName);
                    summary.put("uniqueSecurityNameFoundInRegistry", matched);
                }
            } catch (Exception ex) {
            }
        });

        final var result = new HashMap<>();
        result.put("summary", summary);
        result.put("usersFromRegistry", usersFromRegistry);
        result.put("callerSubject", callerSubject);
        return Response.ok(result).build();
    }

    private List<RegistryUser> getRegistryUsers() throws Exception {
        final UserRegistry userRegistry = RegistryHelper.getUserRegistry(null);
        final List<RegistryUser> users = new ArrayList<>();

        for (Object o : userRegistry.getUsers("*", 0).getList()) {
            final String userName = (String) o;
            final RegistryUser user = new RegistryUser();
            user.id = userRegistry.getUniqueUserId(userName);
            user.userName = userName;
            user.displayName = userRegistry.getUserDisplayName(userName);
            if (user.displayName == null) {
                user.displayName = "<not provided>";
            }
            users.add(user);
        }

        return users;
    }

    @JsonbPropertyOrder({ "id", "userName", "displayName" })
    public static class RegistryUser {
        public String id;
        public String userName;
        public String displayName;
    }

}

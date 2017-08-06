package com.alexhilman.dlink.dcs936;

import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Basic access point credentials.
 */
public class AccessCredentials {
    private final String username;
    private final String password;
    private final URL endpoint;

    public AccessCredentials(final String username, final String password, final URL endpoint) {
        this.username = checkNotNull(username, "username cannot be null");
        this.password = checkNotNull(password, "password cannot be null");
        this.endpoint = checkNotNull(endpoint, "endpoint cannot be null");
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public URL getEndpoint() {
        return endpoint;
    }
}

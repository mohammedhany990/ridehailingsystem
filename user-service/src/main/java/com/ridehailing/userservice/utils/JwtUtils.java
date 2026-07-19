package com.ridehailing.userservice.utils;

import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.UUID;

@Component
public class JwtUtils {

    public UUID getUserId(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString("userId"));
    }
}

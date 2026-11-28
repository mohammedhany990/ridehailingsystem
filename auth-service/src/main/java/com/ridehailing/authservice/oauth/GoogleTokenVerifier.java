package com.ridehailing.authservice.oauth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.ridehailing.authservice.exception.GoogleLoginNotConfiguredException;
import com.ridehailing.authservice.exception.InvalidGoogleTokenException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleTokenVerifier {

    private final String clientId;
    private GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${app.google.client-id:}") String clientId) {
        this.clientId = clientId;
    }

    @PostConstruct
    void init() {
        if (clientId == null || clientId.isBlank()) {
            return;
        }
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public boolean isConfigured() {
        return verifier != null;
    }

    public GoogleUserInfo verify(String idToken) {
        if (!isConfigured()) {
            throw new GoogleLoginNotConfiguredException("Google login is not configured on the server");
        }
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new InvalidGoogleTokenException("Invalid Google ID token");
            }
            GoogleIdToken.Payload payload = token.getPayload();
            String subject = payload.getSubject();
            String email = payload.getEmail();
            if (subject == null || email == null || email.isBlank()) {
                throw new InvalidGoogleTokenException("Google ID token is missing required claims");
            }
            Object name = payload.get("name");
            String displayName = name instanceof String s && !s.isBlank() ? s : email;
            return new GoogleUserInfo(subject, email, displayName);
        } catch (InvalidGoogleTokenException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidGoogleTokenException("Failed to verify Google ID token");
        }
    }
}

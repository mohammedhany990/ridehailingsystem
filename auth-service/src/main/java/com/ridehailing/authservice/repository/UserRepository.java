package com.ridehailing.authservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import com.ridehailing.authservice.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);
}

package com.BEProjects.url_shortener.domain.repositories;

import com.BEProjects.url_shortener.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.support.CustomSQLErrorCodesTranslation;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

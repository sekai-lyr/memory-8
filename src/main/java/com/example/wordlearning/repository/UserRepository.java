package com.example.wordlearning.repository;

import com.example.wordlearning.dataobject.UserDO;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserDO, Long> {
    Optional<UserDO> findByUsername(String username);

    boolean existsByUsername(String username);
}


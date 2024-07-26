package cc.tonyhook.berry.backend.dao.security;

import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.security.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUsername(String username);

}

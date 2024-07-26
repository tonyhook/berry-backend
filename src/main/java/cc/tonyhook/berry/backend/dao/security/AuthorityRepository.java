package cc.tonyhook.berry.backend.dao.security;

import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.security.Authority;

public interface AuthorityRepository extends JpaRepository<Authority, Integer> {

}

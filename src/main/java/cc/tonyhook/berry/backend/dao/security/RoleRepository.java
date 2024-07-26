package cc.tonyhook.berry.backend.dao.security;

import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.security.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {

}

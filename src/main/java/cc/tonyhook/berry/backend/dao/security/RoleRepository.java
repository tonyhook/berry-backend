package cc.tonyhook.berry.backend.dao.security;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import cc.tonyhook.berry.backend.entity.security.Role;

public interface RoleRepository extends ListCrudRepository<Role, Integer>, PagingAndSortingRepository<Role, Integer> {

}

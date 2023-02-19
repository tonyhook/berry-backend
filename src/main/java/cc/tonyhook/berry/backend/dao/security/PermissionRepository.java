package cc.tonyhook.berry.backend.dao.security;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import cc.tonyhook.berry.backend.entity.security.Permission;

public interface PermissionRepository extends ListCrudRepository<Permission, Integer>, PagingAndSortingRepository<Permission, Integer> {

    List<Permission> findByResourceTypeAndResourceIdAndRoleId(String resourceType, Integer resourceId, Integer roleId);
    List<Permission> deleteByResourceTypeAndResourceId(String resourceType, Integer resourceId);

}

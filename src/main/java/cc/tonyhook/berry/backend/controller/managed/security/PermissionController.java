package cc.tonyhook.berry.backend.controller.managed.security;

import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.berry.backend.entity.ManagedResource;
import cc.tonyhook.berry.backend.entity.security.Permission;
import cc.tonyhook.berry.backend.service.security.PermissionService;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.transaction.Transactional;

@RestController
public class PermissionController {

    @Autowired
    private EntityManagerFactory emf;
    @Autowired
    private PermissionService permissionService;

    @RequestMapping(value = "/api/managed/permission/resourceType", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<String>> getResourceTypeList() {
        List<String> resourceTypeList = new ArrayList<String>();

        Metamodel mm = emf.getMetamodel();
        mm.getManagedTypes().forEach(entityType -> {
            if (ManagedResource.class.isAssignableFrom(entityType.getJavaType())) {
                if (!Modifier.isAbstract(entityType.getJavaType().getModifiers())) {
                    resourceTypeList.add(entityType.getJavaType().getSimpleName().toLowerCase());
                }
            }
        });

        return ResponseEntity.ok().body(resourceTypeList);
    }

    @RequestMapping(value = "/api/managed/permission/resourceType/{resourceType}/resourceId/{resourceId}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Permission>> getItemPermissionList(
            @PathVariable String resourceType,
            @PathVariable Integer resourceId) {
        List<Permission> permissionList = permissionService.getItemPermissionList(resourceType, resourceId);

        return ResponseEntity.ok().body(permissionList);
    }

    @RequestMapping(value = "/api/managed/permission/resourceType/{resourceType}/resourceId/{resourceId}/inherited", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Permission>> getInheritedPermissionList(
            @PathVariable String resourceType,
            @PathVariable Integer resourceId) {
        List<Permission> permissionList = permissionService.getInheritedPermissionList(resourceType, resourceId);

        return ResponseEntity.ok().body(permissionList);
    }

    @RequestMapping(value = "/api/managed/permission/resourceType/{resourceType}/resourceId/{resourceId}/full", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Permission>> getFullPermissionList(
            @PathVariable String resourceType,
            @PathVariable Integer resourceId) {
        List<Permission> permissionList = permissionService.getFullPermissionList(resourceType, resourceId);

        return ResponseEntity.ok().body(permissionList);
    }

    @RequestMapping(value = "/api/managed/permission/resourceType/{resourceType}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Permission>> getClassPermissionList(
            @PathVariable String resourceType) {
        List<Permission> permissionList = permissionService.getClassPermissionList(resourceType);

        return ResponseEntity.ok().body(permissionList);
    }

    @RequestMapping(value = "/api/managed/permission", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<PagedModel<Permission>> getPermissionList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        PagedModel<Permission> permissionPage = permissionService.getPermissionList(pageable);

        return ResponseEntity.ok().body(permissionPage);
    }

    @RequestMapping(value = "/api/managed/permission/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Permission> getPermission(
            @PathVariable Integer id) {
        Permission permission = permissionService.getPermission(id);

        if (permission != null) {
            return ResponseEntity.ok().body(permission);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/managed/permission", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<Permission> addPermission(
            @RequestBody Permission newPermission) throws URISyntaxException {
        Permission updatedPermission = permissionService.addPermission(newPermission);

        return ResponseEntity
                .created(new URI("/api/managed/permission/" + updatedPermission.getId()))
                .body(updatedPermission);
    }

    @RequestMapping(value = "/api/managed/permission/{id}", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<?> updatePermission(
            @PathVariable Integer id,
            @RequestBody Permission newPermission) {
        if (!id.equals(newPermission.getId())) {
            return ResponseEntity.badRequest().build();
        }

        Permission targetPermission = permissionService.getPermission(id);
        if (targetPermission == null) {
            return ResponseEntity.notFound().build();
        }

        permissionService.updatePermission(id, newPermission);

        return ResponseEntity.ok().build();
    }

    @Transactional
    @RequestMapping(value = "/api/managed/permission/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removePermission(
            @PathVariable Integer id) {
        Permission deletedPermission = permissionService.getPermission(id);
        if (deletedPermission == null) {
            return ResponseEntity.notFound().build();
        }

        permissionService.removePermission(id);

        return ResponseEntity.ok().build();
    }

}

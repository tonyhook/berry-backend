package cc.tonyhook.berry.backend.controller.managed.security;

import java.net.URI;
import java.net.URISyntaxException;

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

import cc.tonyhook.berry.backend.entity.security.Role;
import cc.tonyhook.berry.backend.service.security.RoleService;
import jakarta.transaction.Transactional;

@RestController
public class RoleController {

    @Autowired
    private RoleService roleService;

    @RequestMapping(value = "/api/managed/role", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<PagedModel<Role>> getRoleList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        PagedModel<Role> rolePage = roleService.getRoleList(pageable);

        return ResponseEntity.ok().body(rolePage);
    }

    @RequestMapping(value = "/api/managed/role/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Role> getRole(
            @PathVariable Integer id) {
        Role role = roleService.getRole(id);

        if (role != null) {
            return ResponseEntity.ok().body(role);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/managed/role", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<Role> addRole(
            @RequestBody Role newRole) throws URISyntaxException {
        Role updatedRole = roleService.addRole(newRole);

        return ResponseEntity
                .created(new URI("/api/managed/role/" + updatedRole.getId()))
                .body(updatedRole);
    }

    @RequestMapping(value = "/api/managed/role/{id}", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateRole(
            @PathVariable Integer id,
            @RequestBody Role newRole) {
        if (!id.equals(newRole.getId())) {
            return ResponseEntity.badRequest().build();
        }

        Role targetRole = roleService.getRole(id);
        if (targetRole == null) {
            return ResponseEntity.notFound().build();
        }

        roleService.updateRole(id, newRole);

        return ResponseEntity.ok().build();
    }

    @Transactional
    @RequestMapping(value = "/api/managed/role/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeRole(
            @PathVariable Integer id) {
        Role deletedRole = roleService.getRole(id);
        if (deletedRole == null) {
            return ResponseEntity.notFound().build();
        }

        roleService.removeRole(id);

        return ResponseEntity.ok().build();
    }

}

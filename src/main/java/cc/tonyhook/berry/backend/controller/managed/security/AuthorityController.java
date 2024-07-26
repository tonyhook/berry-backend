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

import cc.tonyhook.berry.backend.entity.security.Authority;
import cc.tonyhook.berry.backend.service.security.AuthorityService;
import jakarta.transaction.Transactional;

@RestController
public class AuthorityController {

    @Autowired
    private AuthorityService authorityService;

    @RequestMapping(value = "/api/managed/authority", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<PagedModel<Authority>> getAuthorityList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        PagedModel<Authority> authorityPage = authorityService.getAuthorityList(pageable);

        return ResponseEntity.ok().body(authorityPage);
    }

    @RequestMapping(value = "/api/managed/authority/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Authority> getAuthority(
            @PathVariable Integer id) {
        Authority authority = authorityService.getAuthority(id);

        if (authority != null) {
            return ResponseEntity.ok().body(authority);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/managed/authority", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<Authority> addAuthority(
            @RequestBody Authority newAuthority) throws URISyntaxException {
        Authority updatedAuthority = authorityService.addAuthority(newAuthority);

        return ResponseEntity
                .created(new URI("/api/managed/authority/" + updatedAuthority.getId()))
                .body(updatedAuthority);
    }

    @RequestMapping(value = "/api/managed/authority/{id}", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateAuthority(
            @PathVariable Integer id,
            @RequestBody Authority newAuthority) {
        if (!id.equals(newAuthority.getId())) {
            return ResponseEntity.badRequest().build();
        }

        Authority targetAuthority = authorityService.getAuthority(id);
        if (targetAuthority == null) {
            return ResponseEntity.notFound().build();
        }

        authorityService.updateAuthority(id, newAuthority);

        return ResponseEntity.ok().build();
    }

    @Transactional
    @RequestMapping(value = "/api/managed/authority/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeAuthority(
            @PathVariable Integer id) {
        Authority deletedAuthority = authorityService.getAuthority(id);
        if (deletedAuthority == null) {
            return ResponseEntity.notFound().build();
        }

        authorityService.removeAuthority(id);

        return ResponseEntity.ok().build();
    }

}

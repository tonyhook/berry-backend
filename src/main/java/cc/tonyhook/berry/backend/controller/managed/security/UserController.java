package cc.tonyhook.berry.backend.controller.managed.security;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.berry.backend.entity.security.User;
import cc.tonyhook.berry.backend.service.security.UserService;
import jakarta.transaction.Transactional;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/api/managed/user", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Page<User>> getUserList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        Page<User> userPage = userService.getUserList(pageable);

        return ResponseEntity.ok().body(userPage);
    }

    @RequestMapping(value = "/api/managed/user/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<User> getUser(
            @PathVariable Integer id) {
        User user = userService.getUser(id);

        if (user != null) {
            return ResponseEntity.ok().body(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/managed/user", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<User> addUser(
            @RequestBody User newUser) throws URISyntaxException {
        newUser.setPassword(BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt()));

        User updatedUser = userService.addUser(newUser);

        return ResponseEntity
                .created(new URI("/api/managed/user/" + updatedUser.getId()))
                .body(updatedUser);
    }

    @RequestMapping(value = "/api/managed/user/{id}", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer id,
            @RequestBody User newUser) {
        if (!id.equals(newUser.getId())) {
            return ResponseEntity.badRequest().build();
        }

        User targetUser = userService.getUser(id);
        if (targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        if (newUser.getPassword() == null) {
            newUser.setPassword(targetUser.getPassword());
        } else {
            newUser.setPassword(BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt()));
        }
        userService.updateUser(id, newUser);

        return ResponseEntity.ok().build();
    }

    @Transactional
    @RequestMapping(value = "/api/managed/user/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeUser(
            @PathVariable Integer id) {
        User deletedUser = userService.getUser(id);
        if (deletedUser == null) {
            return ResponseEntity.notFound().build();
        }

        userService.removeUser(id);

        return ResponseEntity.ok().build();
    }

}

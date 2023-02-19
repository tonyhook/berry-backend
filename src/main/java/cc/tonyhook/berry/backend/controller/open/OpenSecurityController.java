package cc.tonyhook.berry.backend.controller.open;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenSecurityController {

    @RequestMapping(value = "/api/open/security/user-details", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public UserDetails getUserDetails() {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return userDetails;
        } catch (Exception e) {
            return null;
        }
    }

}

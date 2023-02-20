package cc.tonyhook.berry.backend.controller.open;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.berry.backend.service.analysis.PhoneService;

@RestController
public class OpenPhoneController {

    @Autowired
    private PhoneService phoneService;

    @RequestMapping(value = "/api/open/phone", method = RequestMethod.GET, produces = "plain/text; charset=UTF-8")
    public ResponseEntity<String> getPhoneInfo(
            @RequestParam(defaultValue = "") String prefix) {
        return ResponseEntity.ok().body(phoneService.getPhoneInfo(prefix));
    }

}

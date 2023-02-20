package cc.tonyhook.berry.backend.controller.open;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.berry.backend.service.analysis.IPAddressService;

@RestController
public class OpenIPAddressController {

    @Autowired
    private IPAddressService ipAddressService;

    @RequestMapping(value = "/api/open/ip", method = RequestMethod.GET, produces = "plain/text; charset=UTF-8")
    public ResponseEntity<String> resolveIPAddressRegion(
            @RequestParam(defaultValue = "") String ip,
            @RequestParam(defaultValue = "1") Integer level) {
        return ResponseEntity.ok().body(ipAddressService.resolveIPAddressRegion(ip, level));
    }

}

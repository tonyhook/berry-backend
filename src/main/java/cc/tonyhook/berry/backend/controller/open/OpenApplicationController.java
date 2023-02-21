package cc.tonyhook.berry.backend.controller.open;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenApplicationController {

    @Value("${app.base-url}")
    private String baseUrl;
    @Value("${app.file.server-path}")
    private String serverPath;

    @RequestMapping(value = "/api/open/application/baseurl", method = RequestMethod.GET, produces = "text/plain; charset=UTF-8")
    public ResponseEntity<String> getBaseUrl() {
        return ResponseEntity.ok().body(baseUrl);
    }

    @RequestMapping(value = "/api/open/application/serverpath", method = RequestMethod.GET, produces = "text/plain; charset=UTF-8")
    public ResponseEntity<String> getStorageServerPath() {
        return ResponseEntity.ok().body(serverPath);
    }

}

package cc.tonyhook.berry.backend.controller.open;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.node.ObjectNode;

import cc.tonyhook.berry.backend.service.upload.FileUploadService;

@RestController
public class OpenFileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @RequestMapping(value = "/api/open/upload/{type}/{id}", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity<String> upload(
            @PathVariable String type,
            @PathVariable String id,
            @RequestPart(name = "upload") MultipartFile upload) {
        ObjectNode node = fileUploadService.upload(type, id, upload);

        return ResponseEntity.ok(node.toString());
    }

    @RequestMapping(value = "/api/open/upload/{type}/{id}/{group}/{total}/{page}", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity<String> uploadEx(
            @PathVariable String type,
            @PathVariable String id,
            @PathVariable String group,
            @PathVariable Integer total,
            @PathVariable Integer page,
            @RequestPart(name = "upload") MultipartFile upload) {
        ObjectNode node = fileUploadService.uploadEx(type, id, group, total, page, upload);

        return ResponseEntity.ok(node.toString());
    }

}

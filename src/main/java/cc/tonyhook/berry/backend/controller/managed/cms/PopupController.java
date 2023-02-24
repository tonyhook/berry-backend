package cc.tonyhook.berry.backend.controller.managed.cms;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.berry.backend.entity.cms.Popup;
import cc.tonyhook.berry.backend.service.cms.PopupService;
import cc.tonyhook.berry.backend.service.upload.FileUploadService;

@RestController
public class PopupController {

    @Autowired
    private PopupService popupService;
    @Autowired
    private FileUploadService fileUploadService;

    @RequestMapping(value = "/api/managed/popup", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Popup>> getPopupList(
            @RequestParam(defaultValue = "") String list) {
        List<Popup> popupList = popupService.getPopupList(list);

        return ResponseEntity.ok().body(popupList);
    }

    @RequestMapping(value = "/api/managed/popup/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Popup> getPopup(
            @PathVariable Integer id) {
        Popup popup = popupService.getPopup(id);

        if (popup != null) {
            return ResponseEntity.ok().body(popup);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/managed/popup", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<Popup> addPopup(
            @RequestBody Popup newPopup) throws URISyntaxException {
        Popup updatedPopup = popupService.addPopup(newPopup);

        return ResponseEntity
                .created(new URI("/api/managed/popup/" + updatedPopup.getId()))
                .body(updatedPopup);
    }

    @RequestMapping(value = "/api/managed/popup/{id}", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<?> updatePopup(
            @PathVariable Integer id,
            @RequestBody Popup newPopup) {
        if (!id.equals(newPopup.getId())) {
            return ResponseEntity.badRequest().build();
        }

        Popup targetPopup = popupService.getPopup(id);
        if (targetPopup == null) {
            return ResponseEntity.notFound().build();
        }

        if (targetPopup.getImage() != null && !targetPopup.getImage().equals(newPopup.getImage())) {
            fileUploadService.delete("popup", String.valueOf(id), targetPopup.getImage());
            fileUploadService.delete("popup", String.valueOf(id), targetPopup.getImage() + ".thumbnail");
        }

        popupService.updatePopup(id, newPopup);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/popup/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removePopup(
            @PathVariable Integer id) {
        Popup deletedPopup = popupService.getPopup(id);
        if (deletedPopup == null) {
            return ResponseEntity.notFound().build();
        }

        if (deletedPopup.getImage() != null) {
            fileUploadService.delete("popup", String.valueOf(id), deletedPopup.getImage());
            fileUploadService.delete("popup", String.valueOf(id), deletedPopup.getImage() + ".thumbnail");
        }

        popupService.removePopup(id);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/popup/cleanup", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<String>> cleanupPopup(
            @RequestParam(defaultValue = "false") Boolean delete) {
        List<Popup> popupList = popupService.getPopupList();
        Map<Integer, Popup> popupMap = new HashMap<Integer, Popup>();
        List<String> action = new ArrayList<String>();

        for (Popup popup : popupList) {
            popupMap.put(popup.getId(), popup);
        }

        Set<String> popupIDSet = fileUploadService.listId("popup");

        for (String popupID : popupIDSet) {
            if (NumberUtils.isCreatable(popupID)) {
                if (!popupMap.containsKey(NumberUtils.toInt(popupID))) {
                    if (delete) {
                        fileUploadService.delete("popup", popupID);
                    }
                    action.add("DELETE popup: " + popupID);
                } else {
                    Boolean imageFound = false;
                    Boolean thumbnailFound = false;

                    Set<String> popupFileSet = fileUploadService.listFile("popup", popupID);
                    for (String popupFile : popupFileSet) {
                        if (popupFile.equals(popupMap.get(NumberUtils.toInt(popupID)).getImage())) {
                            imageFound = true;
                        } else if (popupFile.equals(popupMap.get(NumberUtils.toInt(popupID)).getImage() + ".thumbnail")) {
                            thumbnailFound = true;
                        } else {
                            if (delete) {
                                fileUploadService.delete("popup", popupID, popupFile);
                            }
                            action.add("DELETE popup: " + popupID + "/" + popupFile);
                        }
                    }

                    if (imageFound && !thumbnailFound) {
                        byte[] image = fileUploadService.download("popup", popupID, popupMap.get(NumberUtils.toInt(popupID)).getImage());
                        byte[] thumbnail = fileUploadService.getThumbnail(image);
                        fileUploadService.upload("popup", popupID, popupMap.get(NumberUtils.toInt(popupID)).getImage() + ".thumbnail", thumbnail);
                        action.add("CREATE popup: " + popupID + "/" + popupMap.get(NumberUtils.toInt(popupID)).getImage() + ".thumbnail");
                    }
                }
            }
        }

        return ResponseEntity.ok().body(action);
    }

}

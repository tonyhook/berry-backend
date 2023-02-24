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

import cc.tonyhook.berry.backend.entity.cms.Gallery;
import cc.tonyhook.berry.backend.entity.cms.Picture;
import cc.tonyhook.berry.backend.service.cms.GalleryService;
import cc.tonyhook.berry.backend.service.cms.PictureService;
import cc.tonyhook.berry.backend.service.upload.FileUploadService;

@RestController
public class PictureController {

    @Autowired
    private GalleryService galleryService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private FileUploadService fileUploadService;

    @RequestMapping(value = "/api/managed/picture", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Picture>> getPictureList(
            @RequestParam(defaultValue = "0") Integer galleryId) {
        Gallery gallery = galleryService.getGallery(galleryId);

        if (gallery == null) {
            return ResponseEntity.notFound().build();
        }

        List<Picture> pictureList = pictureService.getPictureList(gallery);

        return ResponseEntity.ok().body(pictureList);
    }

    @RequestMapping(value = "/api/managed/picture/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Picture> getPicture(
            @PathVariable Integer id) {
        Picture picture = pictureService.getPicture(id);

        if (picture != null) {
            return ResponseEntity.ok().body(picture);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/managed/picture", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<Picture> addPicture(
            @RequestBody Picture newPicture) throws URISyntaxException {
        Picture updatedPicture = pictureService.addPicture(newPicture);

        return ResponseEntity
                .created(new URI("/api/managed/picture/" + updatedPicture.getId()))
                .body(updatedPicture);
    }

    @RequestMapping(value = "/api/managed/picture/{id}", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<?> updatePicture(
            @PathVariable Integer id,
            @RequestBody Picture newPicture) {
        if (!id.equals(newPicture.getId())) {
            return ResponseEntity.badRequest().build();
        }

        Picture targetPicture = pictureService.getPicture(id);
        if (targetPicture == null) {
            return ResponseEntity.notFound().build();
        }

        if (targetPicture.getImage() != null && !targetPicture.getImage().equals(newPicture.getImage())) {
            fileUploadService.delete("picture", String.valueOf(id), targetPicture.getImage());
            fileUploadService.delete("picture", String.valueOf(id), targetPicture.getImage() + ".thumbnail");
        }

        pictureService.updatePicture(id, newPicture);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/picture/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removePicture(
            @PathVariable Integer id) {
        Picture deletedPicture = pictureService.getPicture(id);
        if (deletedPicture == null) {
            return ResponseEntity.notFound().build();
        }

        if (deletedPicture.getImage() != null) {
            fileUploadService.delete("picture", String.valueOf(id), deletedPicture.getImage());
            fileUploadService.delete("picture", String.valueOf(id), deletedPicture.getImage() + ".thumbnail");
        }

        pictureService.removePicture(id);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/picture/cleanup", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<String>> cleanupPicture(
            @RequestParam(defaultValue = "false") Boolean delete) {
        List<Picture> pictureList = pictureService.getPictureList();
        Map<Integer, Picture> pictureMap = new HashMap<Integer, Picture>();
        List<String> action = new ArrayList<String>();

        for (Picture picture : pictureList) {
            pictureMap.put(picture.getId(), picture);
        }

        Set<String> pictureIDSet = fileUploadService.listId("picture");

        for (String pictureID : pictureIDSet) {
            if (NumberUtils.isCreatable(pictureID)) {
                if (!pictureMap.containsKey(NumberUtils.toInt(pictureID))) {
                    if (delete) {
                        fileUploadService.delete("picture", pictureID);
                    }
                    action.add("DELETE picture: " + pictureID);
                } else {
                    Boolean imageFound = false;
                    Boolean thumbnailFound = false;

                    Set<String> pictureFileSet = fileUploadService.listFile("picture", pictureID);
                    for (String pictureFile : pictureFileSet) {
                        if (pictureFile.equals(pictureMap.get(NumberUtils.toInt(pictureID)).getImage())) {
                            imageFound = true;
                        } else if (pictureFile.equals(pictureMap.get(NumberUtils.toInt(pictureID)).getImage() + ".thumbnail")) {
                            thumbnailFound = true;
                        } else {
                            if (delete) {
                                fileUploadService.delete("picture", pictureID, pictureFile);
                            }
                            action.add("DELETE picture: " + pictureID + "/" + pictureFile);
                        }
                    }

                    if (imageFound && !thumbnailFound) {
                        byte[] image = fileUploadService.download("picture", pictureID, pictureMap.get(NumberUtils.toInt(pictureID)).getImage());
                        byte[] thumbnail = fileUploadService.getThumbnail(image);
                        fileUploadService.upload("picture", pictureID, pictureMap.get(NumberUtils.toInt(pictureID)).getImage() + ".thumbnail", thumbnail);
                        action.add("CREATE picture: " + pictureID + "/" + pictureMap.get(NumberUtils.toInt(pictureID)).getImage() + ".thumbnail");
                    }
                }
            }
        }

        return ResponseEntity.ok().body(action);
    }

}

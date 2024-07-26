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

import cc.tonyhook.berry.backend.entity.cms.Gallery;
import cc.tonyhook.berry.backend.entity.cms.Tag;
import cc.tonyhook.berry.backend.entity.cms.Topic;
import cc.tonyhook.berry.backend.service.cms.GalleryService;
import cc.tonyhook.berry.backend.service.cms.TagService;
import cc.tonyhook.berry.backend.service.cms.TopicService;
import cc.tonyhook.berry.backend.service.upload.FileUploadService;

@RestController
public class GalleryController {

    @Autowired
    private GalleryService galleryService;
    @Autowired
    private TagService tagService;
    @Autowired
    private TopicService topicService;
    @Autowired
    private FileUploadService fileUploadService;

    @RequestMapping(value = "/api/managed/gallery/type", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<PagedModel<Gallery>> getGalleryListByType(
            @RequestParam(defaultValue = "") String type,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        PagedModel<Gallery> galleryPage = galleryService.getGalleryList(type, pageable);

        return ResponseEntity.ok().body(galleryPage);
    }

    @RequestMapping(value = "/api/managed/gallery/tag", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<PagedModel<Gallery>> getGalleryListByTag(
            @RequestParam(defaultValue = "0") Integer tagId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Tag tag = tagService.getTag(tagId);
        if (tag == null) {
            return ResponseEntity.notFound().build();
        }

        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        PagedModel<Gallery> galleryPage = galleryService.getGalleryList(tag, pageable);

        return ResponseEntity.ok().body(galleryPage);
    }

    @RequestMapping(value = "/api/managed/gallery/topic", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<PagedModel<Gallery>> getGalleryListByTopic(
            @RequestParam(defaultValue = "0") Integer topicId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Topic topic = topicService.getTopic(topicId);
        if (topic == null) {
            return ResponseEntity.notFound().build();
        }

        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        PagedModel<Gallery> galleryPage = galleryService.getGalleryList(topic, pageable);

        return ResponseEntity.ok().body(galleryPage);
    }

    @RequestMapping(value = "/api/managed/gallery/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Gallery> getGallery(
            @PathVariable Integer id) {
        Gallery gallery = galleryService.getGallery(id);

        if (gallery != null) {
            return ResponseEntity.ok().body(gallery);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/managed/gallery", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<Gallery> addGallery(
            @RequestBody Gallery newGallery) throws URISyntaxException {
        Gallery updatedGallery = galleryService.addGallery(newGallery);

        return ResponseEntity
                .created(new URI("/api/managed/gallery/" + updatedGallery.getId()))
                .body(updatedGallery);
    }

    @RequestMapping(value = "/api/managed/gallery/{id}", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateGallery(
            @PathVariable Integer id,
            @RequestBody Gallery newGallery) {
        if (!id.equals(newGallery.getId())) {
            return ResponseEntity.badRequest().build();
        }

        Gallery targetGallery = galleryService.getGallery(id);
        if (targetGallery == null) {
            return ResponseEntity.notFound().build();
        }

        if (targetGallery.getImage() != null && !targetGallery.getImage().equals(newGallery.getImage())) {
            fileUploadService.delete("gallery", String.valueOf(id), targetGallery.getImage());
            fileUploadService.delete("gallery", String.valueOf(id), targetGallery.getImage() + ".thumbnail");
        }

        galleryService.updateGallery(id, newGallery);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/gallery/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeGallery(
            @PathVariable Integer id) {
        Gallery deletedGallery = galleryService.getGallery(id);
        if (deletedGallery == null) {
            return ResponseEntity.notFound().build();
        }

        if (deletedGallery.getImage() != null) {
            fileUploadService.delete("gallery", String.valueOf(id), deletedGallery.getImage());
            fileUploadService.delete("gallery", String.valueOf(id), deletedGallery.getImage() + ".thumbnail");
        }

        galleryService.removeGallery(id);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/gallery/cleanup", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<String>> cleanupGallery(
            @RequestParam(defaultValue = "false") Boolean delete) {
        List<Gallery> galleryList = galleryService.getGalleryList();
        Map<Integer, Gallery> galleryMap = new HashMap<Integer, Gallery>();
        List<String> action = new ArrayList<String>();

        for (Gallery gallery : galleryList) {
            galleryMap.put(gallery.getId(), gallery);
        }

        Set<String> galleryIDSet = fileUploadService.listId("gallery");

        for (String galleryID : galleryIDSet) {
            if (NumberUtils.isCreatable(galleryID)) {
                if (!galleryMap.containsKey(NumberUtils.toInt(galleryID))) {
                    if (delete) {
                        fileUploadService.delete("gallery", galleryID);
                    }
                    action.add("DELETE gallery: " + galleryID);
                } else {
                    Boolean imageFound = false;
                    Boolean thumbnailFound = false;

                    Set<String> galleryFileSet = fileUploadService.listFile("gallery", galleryID);
                    for (String galleryFile : galleryFileSet) {
                        if (galleryFile.equals(galleryMap.get(NumberUtils.toInt(galleryID)).getImage())) {
                            imageFound = true;
                        } else if (galleryFile.equals(galleryMap.get(NumberUtils.toInt(galleryID)).getImage() + ".thumbnail")) {
                            thumbnailFound = true;
                        } else {
                            if (delete) {
                                fileUploadService.delete("gallery", galleryID, galleryFile);
                            }
                            action.add("DELETE gallery: " + galleryID + "/" + galleryFile);
                        }
                    }

                    if (imageFound && !thumbnailFound) {
                        byte[] image = fileUploadService.download("gallery", galleryID, galleryMap.get(NumberUtils.toInt(galleryID)).getImage());
                        byte[] thumbnail = fileUploadService.getThumbnail(image);
                        fileUploadService.upload("gallery", galleryID, galleryMap.get(NumberUtils.toInt(galleryID)).getImage() + ".thumbnail", thumbnail);
                        action.add("CREATE gallery: " + galleryID + "/" + galleryMap.get(NumberUtils.toInt(galleryID)).getImage() + ".thumbnail");
                    }
                }
            }
        }

        return ResponseEntity.ok().body(action);
    }

}

package cc.tonyhook.berry.backend.controller.managed.cms;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.berry.backend.entity.cms.Tag;
import cc.tonyhook.berry.backend.service.cms.TagService;

@RestController
public class TagController {

    @Autowired
    private TagService tagService;

    @RequestMapping(value = "/api/managed/tag/type", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Page<Tag>> getTagList(
            @RequestParam(defaultValue = "") String type,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        Page<Tag> tagPage = tagService.getTagList(type, pageable);

        return ResponseEntity.ok().body(tagPage);
    }

    @RequestMapping(value = "/api/managed/tag/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Tag> getTag(
            @PathVariable Integer id) {
        Tag tag = tagService.getTag(id);

        if (tag != null) {
            return ResponseEntity.ok().body(tag);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/managed/tag", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<Tag> addTag(
            @RequestBody Tag newTag) throws URISyntaxException {
        Tag updatedTag = tagService.addTag(newTag);

        return ResponseEntity
                .created(new URI("/api/managed/tag/" + updatedTag.getId()))
                .body(updatedTag);
    }

    @RequestMapping(value = "/api/managed/tag/{id}", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateTag(
            @PathVariable Integer id,
            @RequestBody Tag newTag) {
        if (!id.equals(newTag.getId())) {
            return ResponseEntity.badRequest().build();
        }

        Tag targetTag = tagService.getTag(id);
        if (targetTag == null) {
            return ResponseEntity.notFound().build();
        }

        tagService.updateTag(id, newTag);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/tag/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeTag(
            @PathVariable Integer id) {
        Tag deletedTag = tagService.getTag(id);
        if (deletedTag == null) {
            return ResponseEntity.notFound().build();
        }

        tagService.removeTag(id);

        return ResponseEntity.ok().build();
    }

}

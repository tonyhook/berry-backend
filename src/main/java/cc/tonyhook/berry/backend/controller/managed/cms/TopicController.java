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

import cc.tonyhook.berry.backend.entity.cms.Topic;
import cc.tonyhook.berry.backend.service.cms.TopicService;
import cc.tonyhook.berry.backend.service.upload.FileUploadService;

@RestController
public class TopicController {

    @Autowired
    private TopicService topicService;
    @Autowired
    private FileUploadService fileUploadService;

    @RequestMapping(value = "/api/managed/topic/type", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Page<Topic>> getTopicList(
            @RequestParam(defaultValue = "") String type,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        Page<Topic> topicPage = topicService.getTopicList(type, pageable);

        return ResponseEntity.ok().body(topicPage);
    }

    @RequestMapping(value = "/api/managed/topic/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Topic> getTopic(
            @PathVariable Integer id) {
        Topic topic = topicService.getTopic(id);

        if (topic != null) {
            return ResponseEntity.ok().body(topic);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/managed/topic", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<Topic> addTopic(
            @RequestBody Topic newTopic) throws URISyntaxException {
        Topic updatedTopic = topicService.addTopic(newTopic);

        return ResponseEntity
                .created(new URI("/api/managed/topic/" + updatedTopic.getId()))
                .body(updatedTopic);
    }

    @RequestMapping(value = "/api/managed/topic/{id}", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateTopic(
            @PathVariable Integer id,
            @RequestBody Topic newTopic) {
        if (!id.equals(newTopic.getId())) {
            return ResponseEntity.badRequest().build();
        }

        Topic targetTopic = topicService.getTopic(id);
        if (targetTopic == null) {
            return ResponseEntity.notFound().build();
        }

        if (targetTopic.getImage() != null && !targetTopic.getImage().equals(newTopic.getImage())) {
            fileUploadService.delete("topic", String.valueOf(id), targetTopic.getImage());
            fileUploadService.delete("topic", String.valueOf(id), targetTopic.getImage() + ".thumbnail");
        }

        topicService.updateTopic(id, newTopic);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/topic/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeTopic(
            @PathVariable Integer id) {
        Topic deletedTopic = topicService.getTopic(id);
        if (deletedTopic == null) {
            return ResponseEntity.notFound().build();
        }

        if (deletedTopic.getImage() != null) {
            fileUploadService.delete("topic", String.valueOf(id), deletedTopic.getImage());
            fileUploadService.delete("topic", String.valueOf(id), deletedTopic.getImage() + ".thumbnail");
        }

        topicService.removeTopic(id);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/topic/cleanup", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<String>> cleanupTopic(
            @RequestParam(defaultValue = "false") Boolean delete) {
        List<Topic> topicList = topicService.getTopicList();
        Map<Integer, Topic> topicMap = new HashMap<Integer, Topic>();
        List<String> action = new ArrayList<String>();

        for (Topic topic : topicList) {
            topicMap.put(topic.getId(), topic);
        }

        Set<String> topicIDSet = fileUploadService.listId("topic");

        for (String topicID : topicIDSet) {
            if (NumberUtils.isCreatable(topicID)) {
                if (!topicMap.containsKey(NumberUtils.toInt(topicID))) {
                    if (delete) {
                        fileUploadService.delete("topic", topicID);
                    }
                    action.add("DELETE topic: " + topicID);
                } else {
                    Boolean imageFound = false;
                    Boolean thumbnailFound = false;

                    Set<String> topicFileSet = fileUploadService.listFile("topic", topicID);
                    for (String topicFile : topicFileSet) {
                        if (topicFile.equals(topicMap.get(NumberUtils.toInt(topicID)).getImage())) {
                            imageFound = true;
                        } else if (topicFile.equals(topicMap.get(NumberUtils.toInt(topicID)).getImage() + ".thumbnail")) {
                            thumbnailFound = true;
                        } else {
                            if (delete) {
                                fileUploadService.delete("topic", topicID, topicFile);
                            }
                            action.add("DELETE topic: " + topicID + "/" + topicFile);
                        }
                    }

                    if (imageFound && !thumbnailFound) {
                        byte[] image = fileUploadService.download("topic", topicID, topicMap.get(NumberUtils.toInt(topicID)).getImage());
                        byte[] thumbnail = fileUploadService.getThumbnail(image);
                        fileUploadService.upload("topic", topicID, topicMap.get(NumberUtils.toInt(topicID)).getImage() + ".thumbnail", thumbnail);
                        action.add("CREATE topic: " + topicID + "/" + topicMap.get(NumberUtils.toInt(topicID)).getImage() + ".thumbnail");
                    }
                }
            }
        }

        return ResponseEntity.ok().body(action);
    }

}

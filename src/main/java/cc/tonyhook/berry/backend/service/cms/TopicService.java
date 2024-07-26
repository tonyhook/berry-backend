package cc.tonyhook.berry.backend.service.cms;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.cms.TopicRepository;
import cc.tonyhook.berry.backend.dao.security.PermissionRepository;
import cc.tonyhook.berry.backend.entity.cms.Topic;
import jakarta.transaction.Transactional;

@Service
public class TopicService {

    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private PermissionRepository permissionRepository;

    @PostFilter("hasPermission(filterObject, 'r')")
    public List<Topic> getTopicList() {
        List<Topic> topicList = topicRepository.findAll();

        return topicList;
    }

    public PagedModel<Topic> getTopicList(String type, Pageable pageable) {
        PagedModel<Topic> topicPage = new PagedModel<>(topicRepository.findByType(type, pageable));

        return topicPage;
    }

    public PagedModel<Topic> getTopicList(String type, Boolean disabled, Pageable pageable) {
        PagedModel<Topic> topicPage = new PagedModel<>(topicRepository.findByTypeAndDisabled(type, disabled, pageable));

        return topicPage;
    }

    @PreAuthorize("hasPermission(#id, 'topic', 'r')")
    public Topic getTopic(Integer id) {
        Topic topic = topicRepository.findById(id).orElse(null);

        return topic;
    }

    @PreAuthorize("hasPermission(#id, 'topic', 'r')")
    public Topic getTopic(Integer id, Boolean disabled) {
        Topic topic = topicRepository.findByIdAndDisabled(id, disabled);

        return topic;
    }

    @PreAuthorize("hasPermission(#id, 'topic', 'r')")
    public Topic getTopic(String name, Boolean disabled) {
        Topic topic = topicRepository.findByNameAndDisabled(name, disabled);

        return topic;
    }

    @PreAuthorize("hasPermission(#newTopic, 'c')")
    public Topic addTopic(Topic newTopic) {
        newTopic.setCreateTime(new Timestamp(System.currentTimeMillis()));
        newTopic.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        Topic updatedTopic = topicRepository.save(newTopic);

        return updatedTopic;
    }

    @PreAuthorize("hasPermission(#id, 'topic', 'u')")
    public void updateTopic(Integer id, Topic newTopic) {
        newTopic.setUpdateTime(new Timestamp(System.currentTimeMillis()));

        topicRepository.save(newTopic);
    }

    @Transactional
    @PreAuthorize("hasPermission(#id, 'topic', 'd')")
    public void removeTopic(Integer id) {
        Topic deletedTopic = topicRepository.findById(id).orElse(null);

        deletedTopic.getColumns().clear();
        deletedTopic.getGalleries().clear();
        topicRepository.save(deletedTopic);

        permissionRepository.deleteByResourceTypeAndResourceId("topic", id);
        topicRepository.delete(deletedTopic);
    }

}

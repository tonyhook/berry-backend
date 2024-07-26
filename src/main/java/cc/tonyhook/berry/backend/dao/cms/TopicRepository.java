package cc.tonyhook.berry.backend.dao.cms;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.cms.Topic;

public interface TopicRepository extends JpaRepository<Topic, Integer> {

    Page<Topic> findByType(String type, Pageable pageable);
    Page<Topic> findByTypeAndDisabled(String type, Boolean disabled, Pageable pageable);
    Topic findByIdAndDisabled(Integer id, Boolean disabled);
    Topic findByNameAndDisabled(String name, Boolean disabled);

}

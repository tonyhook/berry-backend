package cc.tonyhook.berry.backend.dao.cms;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.cms.Column;
import cc.tonyhook.berry.backend.entity.cms.Topic;

public interface ColumnRepository extends JpaRepository<Column, Integer> {

    List<Column> findByParentIdOrderBySequence(Integer parentId);
    List<Column> findByParentIdAndDisabledOrderBySequence(Integer parentId, Boolean disabled);
    List<Column> findByParentIdIsNullOrderBySequence();
    List<Column> findByParentIdIsNullAndDisabledOrderBySequence(Boolean disabled);
    List<Column> findByTopicAndDisabledOrderByUpdateTimeDesc(Topic topic, Boolean disabled);
    Column findByIdAndDisabled(Integer id, Boolean disabled);
    Column findByNameAndDisabled(String name, Boolean disabled);

}

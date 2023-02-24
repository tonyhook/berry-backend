package cc.tonyhook.berry.backend.dao.cms;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import cc.tonyhook.berry.backend.entity.cms.Column;

public interface ColumnRepository extends ListCrudRepository<Column, Integer> {

    List<Column> findByParentIdOrderBySequence(Integer parentId);
    List<Column> findByParentIdIsNullOrderBySequence();

}

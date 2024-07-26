package cc.tonyhook.berry.backend.dao.cms;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.cms.Column;
import cc.tonyhook.berry.backend.entity.cms.Content;

public interface ContentRepository extends JpaRepository<Content, Integer> {

    List<Content> findByColumnOrderBySequence(Column column);
    Page<Content> findByColumnAndDisabledOrderBySequence(Column column, Boolean disabled, Pageable pageable);
    Content findByIdAndDisabled(Integer id, Boolean disabled);

}

package cc.tonyhook.berry.backend.dao.cms;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.cms.Tag;

public interface TagRepository extends JpaRepository<Tag, Integer>{

    Page<Tag> findByType(String type, Pageable pageable);
    Page<Tag> findByTypeAndDisabled(String type, Boolean disabled, Pageable pageable);
    Tag findByIdAndDisabled(Integer id, Boolean disabled);

}

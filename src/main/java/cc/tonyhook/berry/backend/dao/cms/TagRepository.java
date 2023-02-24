package cc.tonyhook.berry.backend.dao.cms;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import cc.tonyhook.berry.backend.entity.cms.Tag;

public interface TagRepository extends ListCrudRepository<Tag, Integer>, PagingAndSortingRepository<Tag, Integer> {

    Page<Tag> findByType(String type, Pageable pageable);

}

package cc.tonyhook.berry.backend.dao.cms;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import cc.tonyhook.berry.backend.entity.cms.Column;
import cc.tonyhook.berry.backend.entity.cms.Content;

public interface ContentRepository extends ListCrudRepository<Content, Integer>, PagingAndSortingRepository<Content, Integer> {

    List<Content> findByColumnOrderBySequence(Column column);

}

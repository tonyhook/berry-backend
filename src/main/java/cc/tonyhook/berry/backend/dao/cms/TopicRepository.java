package cc.tonyhook.berry.backend.dao.cms;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import cc.tonyhook.berry.backend.entity.cms.Topic;

public interface TopicRepository extends ListCrudRepository<Topic, Integer>, PagingAndSortingRepository<Topic, Integer> {

    Page<Topic> findByType(String type, Pageable pageable);

}

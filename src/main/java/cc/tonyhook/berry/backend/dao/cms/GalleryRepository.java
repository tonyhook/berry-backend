package cc.tonyhook.berry.backend.dao.cms;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import cc.tonyhook.berry.backend.entity.cms.Gallery;
import cc.tonyhook.berry.backend.entity.cms.Tag;
import cc.tonyhook.berry.backend.entity.cms.Topic;

public interface GalleryRepository extends ListCrudRepository<Gallery, Integer>, PagingAndSortingRepository<Gallery, Integer> {

    Page<Gallery> findByType(String type, Pageable pageable);
    Page<Gallery> findByTypeAndDisabled(String type, Boolean disabled, Pageable pageable);
    Page<Gallery> findByTopic(Topic topic, Pageable pageable);
    Page<Gallery> findByTopicAndDisabledOrderByUpdateTimeDesc(Topic topic, Boolean disabled, Pageable pageable);
    Page<Gallery> findByTagsContains(Tag tag, Pageable pageable);
    Page<Gallery> findByTagsContainsAndDisabledOrderByUpdateTimeDesc(Tag tag, Boolean disabled, Pageable pageable);
    Gallery findByIdAndDisabled(Integer id, Boolean disabled);

}

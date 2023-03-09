package cc.tonyhook.berry.backend.dao.cms;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import cc.tonyhook.berry.backend.entity.cms.Carousel;

public interface CarouselRepository extends ListCrudRepository<Carousel, Integer> {

    List<Carousel> findByListOrderBySequence(String list);
    List<Carousel> findByListAndDisabledOrderBySequence(String list, Boolean disabled);
    Carousel findByIdAndDisabled(Integer id, Boolean disabled);

}

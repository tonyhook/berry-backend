package cc.tonyhook.berry.backend.dao.cms;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.cms.Carousel;

public interface CarouselRepository extends JpaRepository<Carousel, Integer> {

    List<Carousel> findByListOrderBySequence(String list);
    List<Carousel> findByListAndDisabledOrderBySequence(String list, Boolean disabled);
    Carousel findByIdAndDisabled(Integer id, Boolean disabled);

}

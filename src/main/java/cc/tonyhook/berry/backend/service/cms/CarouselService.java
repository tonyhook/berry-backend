package cc.tonyhook.berry.backend.service.cms;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.cms.CarouselRepository;
import cc.tonyhook.berry.backend.dao.security.PermissionRepository;
import cc.tonyhook.berry.backend.entity.cms.Carousel;
import jakarta.transaction.Transactional;

@Service
public class CarouselService {

    @Autowired
    private CarouselRepository carouselRepository;
    @Autowired
    private PermissionRepository permissionRepository;

    @PostFilter("hasPermission(filterObject, 'r')")
    public List<Carousel> getCarouselList() {
        List<Carousel> carouselList = carouselRepository.findAll();

        return carouselList;
    }

    @PostFilter("hasPermission(filterObject, 'r')")
    public List<Carousel> getCarouselList(String list) {
        List<Carousel> carouselList = carouselRepository.findByListOrderBySequence(list);

        return carouselList;
    }

    @PostFilter("hasPermission(filterObject, 'r')")
    public List<Carousel> getCarouselList(String list, Boolean disabled) {
        List<Carousel> carouselList = carouselRepository.findByListAndDisabledOrderBySequence(list, disabled);

        return carouselList;
    }

    @PreAuthorize("hasPermission(#id, 'carousel', 'r')")
    public Carousel getCarousel(Integer id) {
        Carousel carousel = carouselRepository.findById(id).orElse(null);

        return carousel;
    }

    @PreAuthorize("hasPermission(#id, 'carousel', 'r')")
    public Carousel getCarousel(Integer id, Boolean disabled) {
        Carousel carousel = carouselRepository.findByIdAndDisabled(id, disabled);

        return carousel;
    }

    @PreAuthorize("hasPermission(#newCarousel, 'c')")
    public Carousel addCarousel(Carousel newCarousel) {
        newCarousel.setCreateTime(new Timestamp(System.currentTimeMillis()));
        newCarousel.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        Carousel updatedCarousel = carouselRepository.save(newCarousel);

        return updatedCarousel;
    }

    @PreAuthorize("hasPermission(#id, 'carousel', 'u')")
    public void updateCarousel(Integer id, Carousel newCarousel) {
        newCarousel.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        carouselRepository.save(newCarousel);
    }

    @Transactional
    @PreAuthorize("hasPermission(#id, 'carousel', 'd')")
    public void removeCarousel(Integer id) {
        Carousel deletedCarousel = carouselRepository.findById(id).orElse(null);

        permissionRepository.deleteByResourceTypeAndResourceId("carousel", id);
        carouselRepository.delete(deletedCarousel);
    }

}

package cc.tonyhook.berry.backend.dao.cms;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.cms.Gallery;
import cc.tonyhook.berry.backend.entity.cms.Picture;

public interface PictureRepository extends JpaRepository<Picture, Integer> {

    List<Picture> findByGalleryOrderBySequence(Gallery gallery);
    List<Picture> findByGalleryAndDisabledOrderBySequence(Gallery gallery, Boolean disabled);
    Picture findByIdAndDisabled(Integer id, Boolean disabled);

}

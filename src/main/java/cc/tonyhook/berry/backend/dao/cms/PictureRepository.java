package cc.tonyhook.berry.backend.dao.cms;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import cc.tonyhook.berry.backend.entity.cms.Gallery;
import cc.tonyhook.berry.backend.entity.cms.Picture;

public interface PictureRepository extends ListCrudRepository<Picture, Integer> {

    List<Picture> findByGalleryOrderBySequence(Gallery gallery);
    List<Picture> findByGalleryAndDisabledOrderBySequence(Gallery gallery, Boolean disabled);
    Picture findByIdAndDisabled(Integer id, Boolean disabled);

}

package cc.tonyhook.berry.backend.service.cms;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.cms.PictureRepository;
import cc.tonyhook.berry.backend.dao.security.PermissionRepository;
import cc.tonyhook.berry.backend.entity.cms.Gallery;
import cc.tonyhook.berry.backend.entity.cms.Picture;
import jakarta.transaction.Transactional;

@Service
public class PictureService {

    @Autowired
    private PictureRepository pictureRepository;
    @Autowired
    private PermissionRepository permissionRepository;

    @PostFilter("hasPermission(filterObject, 'r')")
    public List<Picture> getPictureList() {
        List<Picture> pictureList = pictureRepository.findAll();

        return pictureList;
    }

    @PostFilter("hasPermission(filterObject, 'r')")
    public List<Picture> getPictureList(Gallery gallery) {
        List<Picture> pictureList = null;
        if (gallery != null) {
            pictureList = pictureRepository.findByGalleryOrderBySequence(gallery);
        } else {
            pictureList = new ArrayList<Picture>();
        }

        return pictureList;
    }

    @PreAuthorize("hasPermission(#id, 'picture', 'r')")
    public Picture getPicture(Integer id) {
        Picture picture = pictureRepository.findById(id).orElse(null);

        return picture;
    }

    @PreAuthorize("hasPermission(#newPicture, 'c')")
    public Picture addPicture(Picture newPicture) {
        newPicture.setCreateTime(new Timestamp(System.currentTimeMillis()));
        newPicture.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        Picture updatedPicture = pictureRepository.save(newPicture);

        return updatedPicture;
    }

    @PreAuthorize("hasPermission(#id, 'picture', 'u')")
    public void updatePicture(Integer id, Picture newPicture) {
        newPicture.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        pictureRepository.save(newPicture);
    }

    @Transactional
    @PreAuthorize("hasPermission(#id, 'picture', 'd')")
    public void removePicture(Integer id) {
        Picture deletedPicture = pictureRepository.findById(id).orElse(null);

        permissionRepository.deleteByResourceTypeAndResourceId("picture", id);
        pictureRepository.delete(deletedPicture);
    }

}

package cc.tonyhook.berry.backend.service.cms;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.cms.PopupRepository;
import cc.tonyhook.berry.backend.dao.security.PermissionRepository;
import cc.tonyhook.berry.backend.entity.cms.Popup;
import jakarta.transaction.Transactional;

@Service
public class PopupService {

    @Autowired
    private PopupRepository popupRepository;
    @Autowired
    private PermissionRepository permissionRepository;

    @PostFilter("hasPermission(filterObject, 'r')")
    public List<Popup> getPopupList() {
        List<Popup> popupList = popupRepository.findAll();

        return popupList;
    }

    @PostFilter("hasPermission(filterObject, 'r')")
    public List<Popup> getPopupList(String list) {
        List<Popup> popupList = popupRepository.findByListOrderBySequence(list);

        return popupList;
    }

    @PreAuthorize("hasPermission(#id, 'popup', 'r')")
    public Popup getPopup(Integer id) {
        Popup popup = popupRepository.findById(id).orElse(null);

        return popup;
    }

    @PreAuthorize("hasPermission(#newPopup, 'c')")
    public Popup addPopup(Popup newPopup) {
        newPopup.setCreateTime(new Timestamp(System.currentTimeMillis()));
        newPopup.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        Popup updatedPopup = popupRepository.save(newPopup);

        return updatedPopup;
    }

    @PreAuthorize("hasPermission(#id, 'popup', 'u')")
    public void updatePopup(Integer id, Popup newPopup) {
        newPopup.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        popupRepository.save(newPopup);
    }

    @Transactional
    @PreAuthorize("hasPermission(#id, 'popup', 'd')")
    public void removePopup(Integer id) {
        Popup deletedPopup = popupRepository.findById(id).orElse(null);

        permissionRepository.deleteByResourceTypeAndResourceId("popup", id);
        popupRepository.delete(deletedPopup);
    }

}

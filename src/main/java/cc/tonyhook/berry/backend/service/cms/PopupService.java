package cc.tonyhook.berry.backend.service.cms;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.cms.PopupRepository;
import cc.tonyhook.berry.backend.dao.security.PermissionRepository;
import cc.tonyhook.berry.backend.entity.cms.Popup;
import cc.tonyhook.berry.backend.entity.visitor.ProfileLog;
import cc.tonyhook.berry.backend.service.visitor.ProfileLogService;
import jakarta.transaction.Transactional;

@Service
public class PopupService {

    @Autowired
    private ProfileLogService profileLogService;

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

    @PostFilter("hasPermission(filterObject, 'r')")
    public List<Popup> getPopupList(String list, Boolean disabled, String openid) {
        List<Popup> popupList = popupRepository.findByListAndDisabledOrderBySequence(list, disabled);

        Iterator<Popup> iterator = popupList.iterator();
        while (iterator.hasNext()) {
            Popup popup = iterator.next();
            Boolean show = true;

            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (now.before(popup.getStartTime()) || now.after(popup.getEndTime())) {
                show = false;
            } else {
                Integer actionTerminate = ProfileLog.PROFILE_LOG_ACTION_TYPE_VIEW;
                if (popup.getTerminate().equals("click")) {
                    actionTerminate = ProfileLog.PROFILE_LOG_ACTION_TYPE_CLICK;
                }
                if (popup.getTerminate().equals("action")) {
                    actionTerminate = ProfileLog.PROFILE_LOG_ACTION_TYPE_ACTION;
                }

                ProfileLog profileLogFreq = profileLogService.getTopProfileLog(openid, "popup", popup.getId(), ProfileLog.PROFILE_LOG_ACTION_TYPE_VIEW);
                if (profileLogFreq != null) {
                    ZoneId zoneId = ZoneId.of("Asia/Shanghai");

                    if (popup.getFreq().equals("day")) {
                        Timestamp lastviewed = profileLogFreq.getUpdateTime();
                        if (lastviewed != null) {
                            String lastDateStr = lastviewed.toInstant().atZone(zoneId).toString().substring(0, 10);
                            String nowDateStr = now.toInstant().atZone(zoneId).toString().substring(0, 10);
                            if (nowDateStr.compareTo(lastDateStr) <= 0) {
                                show = false;
                            }
                        }
                    }
                    if (popup.getFreq().equals("halfday")) {
                        Timestamp lastviewed = profileLogFreq.getUpdateTime();
                        if (lastviewed != null) {
                            String lastDateStr = lastviewed.toInstant().atZone(zoneId).toString().substring(0, 10);
                            String nowDateStr = now.toInstant().atZone(zoneId).toString().substring(0, 10);
                            if (nowDateStr.compareTo(lastDateStr) < 0) {
                                show = false;
                            }
                            if (nowDateStr.compareTo(lastDateStr) == 0) {
                                int lastHour = lastviewed.toInstant().atZone(zoneId).getHour();
                                int nowHour = now.toInstant().atZone(zoneId).getHour();
                                if (nowHour / 12 <= lastHour / 12) {
                                    show = false;
                                }
                            }
                        }
                    }

                    if (show) {
                        if (actionTerminate == ProfileLog.PROFILE_LOG_ACTION_TYPE_VIEW) {
                            show = false;
                        } else {
                            ProfileLog profileLogTerminate = profileLogService.getTopProfileLog(openid, "popup", popup.getId(), actionTerminate);
                            if (profileLogTerminate != null) {
                                show = false;
                            }
                        }
                    }
                }
            }

            if (!show) {
                iterator.remove();
            }
        }

        return popupList;
    }

    @PreAuthorize("hasPermission(#id, 'popup', 'r')")
    public Popup getPopup(Integer id) {
        Popup popup = popupRepository.findById(id).orElse(null);

        return popup;
    }

    @PreAuthorize("hasPermission(#id, 'popup', 'r')")
    public Popup getPopup(Integer id, Boolean disabled) {
        Popup popup = popupRepository.findByIdAndDisabled(id, disabled);

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

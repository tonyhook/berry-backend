package cc.tonyhook.berry.backend.service.wechat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.wechat.WechatSiteRepository;
import cc.tonyhook.berry.backend.entity.wechat.WechatSite;
import jakarta.transaction.Transactional;

@Service
public class WechatSiteService {

    @Autowired
    private WechatSiteRepository wechatSiteRepository;

    @PreAuthorize("hasAuthority('SOCIAL_MANAGEMENT')")
    public PagedModel<WechatSite> getWechatSiteList(Pageable pageable) {
        PagedModel<WechatSite> wechatSitePage = new PagedModel<>(wechatSiteRepository.findAll(pageable));

        return wechatSitePage;
    }

    @PreAuthorize("hasAuthority('SOCIAL_MANAGEMENT')")
    public WechatSite getWechatSite(Integer id) {
        WechatSite wechatSite = wechatSiteRepository.findById(id).orElse(null);

        return wechatSite;
    }

    public WechatSite getWechatSite(String domain) {
        WechatSite wechatSite = wechatSiteRepository.findByDomain(domain);

        return wechatSite;
    }

    @PreAuthorize("hasAuthority('SOCIAL_MANAGEMENT')")
    public WechatSite addWechatSite(WechatSite newWechatSite) {
        WechatSite updatedWechatSite = wechatSiteRepository.save(newWechatSite);

        return updatedWechatSite;
    }

    @PreAuthorize("hasAuthority('SOCIAL_MANAGEMENT')")
    public void updateWechatSite(Integer id, WechatSite newWechatSite) {
        wechatSiteRepository.save(newWechatSite);
    }

    @Transactional
    @PreAuthorize("hasAuthority('SOCIAL_MANAGEMENT')")
    public void removeWechatSite(Integer id) {
        WechatSite deletedWechatSite = wechatSiteRepository.findById(id).orElse(null);

        wechatSiteRepository.delete(deletedWechatSite);
    }

}

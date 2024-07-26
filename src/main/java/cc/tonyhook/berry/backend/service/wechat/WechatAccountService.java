package cc.tonyhook.berry.backend.service.wechat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.wechat.WechatAccountRepository;
import cc.tonyhook.berry.backend.dao.wechat.WechatSiteRepository;
import cc.tonyhook.berry.backend.entity.wechat.WechatAccount;
import cc.tonyhook.berry.backend.entity.wechat.WechatSite;
import jakarta.transaction.Transactional;

@Service
public class WechatAccountService {

    @Autowired
    private WechatAccountRepository wechatAccountRepository;
    @Autowired
    private WechatSiteRepository wechatSiteRepository;

    @PreAuthorize("hasAuthority('SOCIAL_MANAGEMENT')")
    public PagedModel<WechatAccount> getWechatAccountList(Pageable pageable) {
        PagedModel<WechatAccount> wechatAccountPage = new PagedModel<>(wechatAccountRepository.findAll(pageable));

        return wechatAccountPage;
    }

    @PreAuthorize("hasAuthority('SOCIAL_MANAGEMENT')")
    public WechatAccount getWechatAccount(Integer id) {
        WechatAccount wechatAccount = wechatAccountRepository.findById(id).orElse(null);

        return wechatAccount;
    }

    public WechatAccount getWechatAccountByAppid(String appid) {
        WechatAccount wechatAccount = wechatAccountRepository.findByAppid(appid);

        return wechatAccount;
    }

    public WechatAccount getWechatAccountByVerifyFilename(String filename) {
        WechatAccount wechatAccount = wechatAccountRepository.findByVerifyFilename(filename);

        return wechatAccount;
    }

    @PreAuthorize("hasAuthority('SOCIAL_MANAGEMENT')")
    public WechatAccount addWechatAccount(WechatAccount newWechatAccount) {
        WechatAccount updatedWechatAccount = wechatAccountRepository.save(newWechatAccount);

        return updatedWechatAccount;
    }

    @Transactional
    @PreAuthorize("hasAuthority('SOCIAL_MANAGEMENT')")
    public void updateWechatAccount(Integer id, WechatAccount newWechatAccount) {
        WechatAccount wechatAccount = wechatAccountRepository.findById(id).orElse(null);
        Map<String, WechatSite> wechatSiteMap = new HashMap<String, WechatSite>();
        for (WechatSite wechatSite : wechatAccount.getWechatSites()) {
            wechatSiteMap.put(wechatSite.getDomain(), wechatSite);
        }

        List<WechatSite> addWechatSiteList = new ArrayList<WechatSite>();
        List<WechatSite> retainWechatSiteList = new ArrayList<WechatSite>();
        for (WechatSite wechatSite : newWechatAccount.getWechatSites()) {
            if (!wechatSiteMap.containsKey(wechatSite.getDomain())) {
                addWechatSiteList.add(wechatSite);
            } else {
                retainWechatSiteList.add(wechatSiteMap.get(wechatSite.getDomain()));
                wechatSiteMap.remove(wechatSite.getDomain());
            }
        }

        for (WechatSite wechatSite : wechatSiteMap.values()) {
            wechatSiteRepository.delete(wechatSite);
        }

        newWechatAccount.getWechatSites().clear();
        newWechatAccount.getWechatSites().addAll(retainWechatSiteList);
        for (WechatSite wechatSite : addWechatSiteList) {
            wechatSite.setWechatAccount(newWechatAccount);
            WechatSite updatedWechatSite = wechatSiteRepository.save(wechatSite);

            newWechatAccount.getWechatSites().add(updatedWechatSite);
        }

        wechatAccountRepository.save(newWechatAccount);
    }

    @Transactional
    @PreAuthorize("hasAuthority('SOCIAL_MANAGEMENT')")
    public void removeWechatAccount(Integer id) {
        WechatAccount deletedWechatAccount = wechatAccountRepository.findById(id).orElse(null);

        for (WechatSite wechatSite : deletedWechatAccount.getWechatSites()) {
            wechatSiteRepository.delete(wechatSite);
        }

        wechatAccountRepository.delete(deletedWechatAccount);
    }

}

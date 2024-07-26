package cc.tonyhook.berry.backend.dao.wechat;

import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.wechat.WechatSite;

public interface WechatSiteRepository extends JpaRepository<WechatSite, Integer> {

    WechatSite findByDomain(String domain);

}

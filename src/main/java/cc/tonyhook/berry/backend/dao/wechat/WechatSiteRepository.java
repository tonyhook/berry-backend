package cc.tonyhook.berry.backend.dao.wechat;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import cc.tonyhook.berry.backend.entity.wechat.WechatSite;

public interface WechatSiteRepository extends ListCrudRepository<WechatSite, Integer>, PagingAndSortingRepository<WechatSite, Integer> {

    WechatSite findByDomain(String domain);

}

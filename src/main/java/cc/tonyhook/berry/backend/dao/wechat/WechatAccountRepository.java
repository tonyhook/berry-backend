package cc.tonyhook.berry.backend.dao.wechat;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import cc.tonyhook.berry.backend.entity.wechat.WechatAccount;

public interface WechatAccountRepository extends ListCrudRepository<WechatAccount, Integer>, PagingAndSortingRepository<WechatAccount, Integer> {

    WechatAccount findByAppid(String appid);
    WechatAccount findByVerifyFilename(String filename);

}

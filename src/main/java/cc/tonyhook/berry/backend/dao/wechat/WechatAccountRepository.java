package cc.tonyhook.berry.backend.dao.wechat;

import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.wechat.WechatAccount;

public interface WechatAccountRepository extends JpaRepository<WechatAccount, Integer> {

    WechatAccount findByAppid(String appid);
    WechatAccount findByVerifyFilename(String filename);

}

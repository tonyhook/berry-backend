package cc.tonyhook.berry.backend.dao.wechat;

import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.wechat.WechatUser;

public interface WechatUserRepository extends JpaRepository<WechatUser, String> {

}

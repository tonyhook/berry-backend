package cc.tonyhook.berry.backend.dao.wechat;

import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.wechat.WechatMessage;
import cc.tonyhook.berry.backend.entity.wechat.WechatMessageId;

public interface WechatMessageRepository extends JpaRepository<WechatMessage, WechatMessageId> {

}

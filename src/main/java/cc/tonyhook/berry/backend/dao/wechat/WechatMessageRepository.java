package cc.tonyhook.berry.backend.dao.wechat;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import cc.tonyhook.berry.backend.entity.wechat.WechatMessage;
import cc.tonyhook.berry.backend.entity.wechat.WechatMessageId;

public interface WechatMessageRepository extends ListCrudRepository<WechatMessage, WechatMessageId>, PagingAndSortingRepository<WechatMessage, WechatMessageId> {

}

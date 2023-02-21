package cc.tonyhook.berry.backend.dao.wechat;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import cc.tonyhook.berry.backend.entity.wechat.WechatUser;

public interface WechatUserRepository extends ListCrudRepository<WechatUser, String>, PagingAndSortingRepository<WechatUser, String> {

}

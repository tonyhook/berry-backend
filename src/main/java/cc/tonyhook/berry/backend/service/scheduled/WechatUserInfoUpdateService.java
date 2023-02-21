package cc.tonyhook.berry.backend.service.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cc.tonyhook.berry.backend.dao.wechat.WechatUserRepository;
import cc.tonyhook.berry.backend.entity.wechat.WechatMessage;
import cc.tonyhook.berry.backend.entity.wechat.WechatUser;
import cc.tonyhook.berry.backend.service.wechat.WechatMessageListener;
import cc.tonyhook.berry.backend.service.wechat.WechatMessageService;
import cc.tonyhook.berry.backend.service.wechat.WechatService;
import jakarta.annotation.PostConstruct;

@Component
public class WechatUserInfoUpdateService implements WechatMessageListener {

    @Autowired
    private WechatUserRepository wechatUserRepository;
    @Autowired
    private WechatMessageService wechatMessageService;
    @Autowired
    private WechatService wechatService;

    @PostConstruct
    private void register() {
        wechatMessageService.registerWechatMessageListener(this);
    }

    @Override
    public void process(WechatMessage wechatMessage) {
        if (wechatMessage.getMsgType().equals("event")) {
            if (wechatMessage.getEvent().equals("subscribe")) {
                WechatUser wechatUser = wechatUserRepository.findById(wechatMessage.getId().getFromUserName()).orElse(null);
                if (wechatUser != null) {
                    wechatUser.setSubscribed(true);
                    wechatUserRepository.save(wechatUser);
                }
            }
            if (wechatMessage.getEvent().equals("unsubscribe")) {
                WechatUser wechatUser = wechatUserRepository.findById(wechatMessage.getId().getFromUserName()).orElse(null);
                if (wechatUser != null) {
                    wechatUser.setSubscribed(false);
                    wechatUserRepository.save(wechatUser);
                }
            }
        }
    }

    @Scheduled(cron = "0 0 16 * * 6")
    public void updateWechatUserInfo() {
        wechatUserRepository.findAll().forEach(wechatUser -> {
            String openid = wechatUser.getOpenid();
            WechatUser user = wechatService.getWechatUserInfo(openid);
            if (user != null) {
                if (user.getSubscribed()) {
                    wechatUser.setAvatar(user.getAvatar());
                    wechatUser.setGender(user.getGender());
                    wechatUser.setNickname(user.getNickname());
                    wechatUser.setUnionid(user.getUnionid());
                    wechatUser.setSubscribed(true);
                } else {
                    wechatUser.setSubscribed(false);
                }
                wechatUserRepository.save(wechatUser);
            }
        });
    }

}

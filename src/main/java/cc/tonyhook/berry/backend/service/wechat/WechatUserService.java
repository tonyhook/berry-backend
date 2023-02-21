package cc.tonyhook.berry.backend.service.wechat;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.wechat.WechatUserRepository;
import cc.tonyhook.berry.backend.entity.wechat.WechatUser;
import jakarta.transaction.Transactional;

@Service
public class WechatUserService {

    @Autowired
    private WechatUserRepository wechatUserRepository;

    public Page<WechatUser> getWechatUserList(Pageable pageable) {
        Integer totalElements = Long.valueOf(wechatUserRepository.count()).intValue();
        if (totalElements <= pageable.getPageSize() * pageable.getPageNumber()) {
            pageable = PageRequest.of(
                    (totalElements - 1) / pageable.getPageSize(),
                    pageable.getPageSize(),
                    pageable.getSort());
        }

        Page<WechatUser> wechatUserPage = wechatUserRepository.findAll(pageable);

        return wechatUserPage;
    }

    public List<WechatUser> getWechatUserList() {
        List<WechatUser> wechatUserList = wechatUserRepository.findAll();

        return wechatUserList;
    }

    public WechatUser getWechatUser(String openid) {
        WechatUser wechatUser = wechatUserRepository.findById(openid).orElse(null);

        return wechatUser;
    }

    public WechatUser addWechatUser(WechatUser newWechatUser) {
        WechatUser updatedWechatUser = wechatUserRepository.save(newWechatUser);

        return updatedWechatUser;
    }

    public List<WechatUser> addWechatUsers(List<WechatUser> wechatUserList) {
        List<WechatUser> updatedWechatUserList = wechatUserRepository.saveAll(wechatUserList);

        return updatedWechatUserList;
    }

    public void updateWechatUser(String openid, WechatUser newWechatUser) {
        wechatUserRepository.save(newWechatUser);
    }

    @Transactional
    public void removeWechatUser(String openid) {
        WechatUser deletedWechatUser = wechatUserRepository.findById(openid).orElse(null);

        wechatUserRepository.delete(deletedWechatUser);
    }

    @Transactional
    public void removeWechatUsers(List<WechatUser> wechatUserList) {
        wechatUserRepository.deleteAll(wechatUserList);
    }

}

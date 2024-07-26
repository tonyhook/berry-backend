package cc.tonyhook.berry.backend.service.wechat;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.wechat.WechatMessageRepository;
import cc.tonyhook.berry.backend.entity.wechat.WechatMessage;
import cc.tonyhook.berry.backend.entity.wechat.WechatMessageId;
import jakarta.transaction.Transactional;

@Service
public class WechatMessageService {

    @Autowired
    private WechatMessageRepository wechatMessageRepository;

    private List<WechatMessageListener> wechatMessageListenerList = new ArrayList<WechatMessageListener>();

    public PagedModel<WechatMessage> getWechatMessageList(Pageable pageable) {
        Integer totalElements = Long.valueOf(wechatMessageRepository.count()).intValue();
        if (totalElements <= pageable.getPageSize() * pageable.getPageNumber()) {
            pageable = PageRequest.of(
                    (totalElements - 1) / pageable.getPageSize(),
                    pageable.getPageSize(),
                    pageable.getSort());
        }

        PagedModel<WechatMessage> wechatMessagePage = new PagedModel<>(wechatMessageRepository.findAll(pageable));

        return wechatMessagePage;
    }

    public List<WechatMessage> getWechatMessageList() {
        List<WechatMessage> wechatMessageList = wechatMessageRepository.findAll();

        return wechatMessageList;
    }

    public WechatMessage getWechatMessage(WechatMessageId id) {
        WechatMessage wechatMessage = wechatMessageRepository.findById(id).orElse(null);

        return wechatMessage;
    }

    public WechatMessage addWechatMessage(WechatMessage newWechatMessage) {
        WechatMessage updatedWechatMessage = wechatMessageRepository.save(newWechatMessage);

        notifyWechatMessageListener(updatedWechatMessage);

        return updatedWechatMessage;
    }

    public List<WechatMessage> addWechatMessages(List<WechatMessage> wechatMessageList) {
        List<WechatMessage> updatedWechatMessageList = wechatMessageRepository.saveAll(wechatMessageList);

        return updatedWechatMessageList;
    }

    public void updateWechatMessage(WechatMessageId id, WechatMessage newWechatMessage) {
        wechatMessageRepository.save(newWechatMessage);
    }

    @Transactional
    public void removeWechatMessage(WechatMessageId id) {
        WechatMessage deletedWechatMessage = wechatMessageRepository.findById(id).orElse(null);

        wechatMessageRepository.delete(deletedWechatMessage);
    }

    @Transactional
    public void removeWechatMessages(List<WechatMessage> wechatMessageList) {
        wechatMessageRepository.deleteAll(wechatMessageList);
    }

    public Boolean registerWechatMessageListener(WechatMessageListener wechatMessageListener) {
        if (wechatMessageListenerList.contains(wechatMessageListener)) {
            return false;
        }
        if (wechatMessageListener == null) {
            return false;
        }

        wechatMessageListenerList.add(wechatMessageListener);

        return true;
    }

    public void notifyWechatMessageListener(WechatMessage wechatMessage) {
        wechatMessageListenerList.forEach(wechatMessageListener -> {
            wechatMessageListener.process(wechatMessage);
        });
    }

}

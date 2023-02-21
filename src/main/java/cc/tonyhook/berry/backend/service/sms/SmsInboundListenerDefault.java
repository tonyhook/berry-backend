package cc.tonyhook.berry.backend.service.sms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.entity.sms.SmsInboundLog;
import jakarta.annotation.PostConstruct;

@Service
public class SmsInboundListenerDefault implements SmsInboundListener {

    @Autowired
    private SmsService smsService;

    @PostConstruct
    private void register() {
        smsService.registerInboundListener(this);
    }

    public void process(SmsInboundLog inbound) {
    }

}

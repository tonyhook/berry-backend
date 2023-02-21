package cc.tonyhook.berry.backend.service.scheduled;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cc.tonyhook.berry.backend.dao.sms.SmsOutboundLogRepository;
import cc.tonyhook.berry.backend.entity.sms.SmsOutboundLog;
import cc.tonyhook.berry.backend.service.sms.SmsService;

@Component
public class SmsOutboundMonitorService {

    @Autowired
    private SmsOutboundLogRepository smsOutboundLogRepository;
    @Autowired
    private SmsService smsService;

    @Value("${app.sms.outbound-monitor:false}")
    private boolean monitor;

    @Scheduled(cron = "30 * * * * ?")
    public void getSmsReport() {
        if (monitor) {
            List<SmsOutboundLog> outboundList = smsOutboundLogRepository.findByState(0);

            for (SmsOutboundLog outbound : outboundList) {
                if (outbound.getState() == 0) {
                    smsService.report(outbound);
                }
            }
        }
    }

}

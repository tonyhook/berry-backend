package cc.tonyhook.berry.backend.service.sms;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.QuerySendDetailsRequest;
import com.aliyun.dysmsapi20170525.models.QuerySendDetailsResponse;
import com.aliyun.dysmsapi20170525.models.QuerySendDetailsResponseBody.QuerySendDetailsResponseBodySmsSendDetailDTOsSmsSendDetailDTO;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.tonyhook.berry.backend.dao.sms.SmsOutboundLogRepository;
import cc.tonyhook.berry.backend.entity.sms.SmsInboundLog;
import cc.tonyhook.berry.backend.entity.sms.SmsOutboundLog;
import cc.tonyhook.berry.backend.entity.sms.SmsReport;
import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnProperty(prefix = "app.sms", name = "provider", havingValue = "aliyun")
public class SmsProviderAliyun implements SmsProvider {

    @Value("${app.sms.aliyun.access-key-id}")
    private String accessKeyId;
    @Value("${app.sms.aliyun.access-key-secret}")
    private String accessKeySecret;
    @Value("${app.sms.aliyun.queue-name}")
    private String queueName;

    @Autowired
    private SmsOutboundLogRepository smsOutboundLogRepository;

    private LinkedList<SmsInboundLog> inboundQueue;

    @PostConstruct
    private void initialQueue() {
        inboundQueue = new LinkedList<SmsInboundLog>();
    }

    @Override
    public SmsOutboundLog send(String signature, String templateName, Map<String, Object> templateParam, String extend, String phone) {
        try {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret);
            Client client = new Client(config);

            ObjectMapper mapper = new ObjectMapper();
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(signature)
                    .setTemplateCode(templateName)
                    .setTemplateParam(mapper.writeValueAsString(templateParam))
                    .setSmsUpExtendCode(extend)
                    .setOutId(extend);
            SendSmsResponse response = client.sendSms(request);

            if (response.getBody().getCode().equals("OK")) {
                SmsOutboundLog log = new SmsOutboundLog();
                log.setProvider("Aliyun");
                log.setSendTime(new Timestamp(System.currentTimeMillis()));
                log.setPhone(phone);
                log.setExtend(extend);
                log.setTemplateName(templateName);
                log.setTemplateParam(mapper.writeValueAsString(templateParam));
                log.setState(0);
                log.setMessageId(response.getBody().getBizId());

                return log;
            }
            else {
                System.out.println("SMS ERROR: " + phone + " " + response.getBody().getMessage());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<SmsInboundLog> receive() {
        List<SmsInboundLog> inboundList = new ArrayList<SmsInboundLog>();

        synchronized(inboundQueue) {
            while (inboundQueue.peek() != null) {
                inboundList.add(inboundQueue.poll());
            }
        }

        return inboundList;
    }

    @Override
    public SmsReport report(SmsOutboundLog outbound) {
        try {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret);
            Client client = new Client(config);

            ZoneId zoneId = ZoneId.of("Asia/Shanghai");
            LocalDate sendTime = LocalDate.ofInstant(outbound.getSendTime().toInstant(), zoneId);

            QuerySendDetailsRequest request = new QuerySendDetailsRequest()
                    .setPhoneNumber(com.aliyun.teautil.Common.assertAsString(outbound.getPhone()))
                    .setBizId(outbound.getMessageId())
                    .setSendDate(sendTime.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .setPageSize(10L)
                    .setCurrentPage(1L);
            QuerySendDetailsResponse response = client.querySendDetails(request);
            List<QuerySendDetailsResponseBodySmsSendDetailDTOsSmsSendDetailDTO> dtoList = response.getBody().getSmsSendDetailDTOs().getSmsSendDetailDTO();
            for (QuerySendDetailsResponseBodySmsSendDetailDTOsSmsSendDetailDTO dto : dtoList) {
                SmsReport report = new SmsReport();
                report.setMessageId(outbound.getMessageId());
                report.setState(0);
                report.setDetail("");

                if (dto.getSendStatus().equals(3L)) {
                    report.setState(1);
                    report.setDetail(dto.getErrCode());
                } else if (dto.getSendStatus().equals(2L)) {
                    report.setState(-1);
                    report.setDetail(dto.getErrCode());
                } else {
                    if (System.currentTimeMillis() - outbound.getSendTime().getTime() > 86400000) {
                        report.setState(-2);
                        report.setDetail("TIMEOUT");
                    }
                }

                if (report.getState() != 0) {
                    outbound.setState(report.getState());
                    outbound.setDetail(report.getDetail());
                    smsOutboundLogRepository.save(outbound);
                }

                return report;
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void gather(SmsInboundLog log) {
        synchronized (inboundQueue) {
            inboundQueue.offer(log);
        }
    }

}

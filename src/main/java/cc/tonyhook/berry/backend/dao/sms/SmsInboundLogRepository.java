package cc.tonyhook.berry.backend.dao.sms;

import org.springframework.data.repository.ListCrudRepository;

import cc.tonyhook.berry.backend.entity.sms.SmsInboundLog;

public interface SmsInboundLogRepository extends ListCrudRepository<SmsInboundLog, Integer> {

}

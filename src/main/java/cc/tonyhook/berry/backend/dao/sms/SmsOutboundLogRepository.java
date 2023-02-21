package cc.tonyhook.berry.backend.dao.sms;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import cc.tonyhook.berry.backend.entity.sms.SmsOutboundLog;

public interface SmsOutboundLogRepository extends ListCrudRepository<SmsOutboundLog, Integer> {

    List<SmsOutboundLog> findByState(Integer state);

}

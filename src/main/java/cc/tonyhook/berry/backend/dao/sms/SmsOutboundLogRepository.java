package cc.tonyhook.berry.backend.dao.sms;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.sms.SmsOutboundLog;

public interface SmsOutboundLogRepository extends JpaRepository<SmsOutboundLog, Integer> {

    List<SmsOutboundLog> findByState(Integer state);

}

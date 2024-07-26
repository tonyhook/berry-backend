package cc.tonyhook.berry.backend.dao.sms;

import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.sms.SmsInboundLog;

public interface SmsInboundLogRepository extends JpaRepository<SmsInboundLog, Integer> {

}

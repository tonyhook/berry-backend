package cc.tonyhook.berry.backend.service.audit;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.audit.LogRepository;
import cc.tonyhook.berry.backend.entity.audit.Log;
import jakarta.transaction.Transactional;

@Service
public class LogService {

    @Autowired
    private LogRepository logRepository;

    @PreAuthorize("hasAuthority('AUDIT_MANAGEMENT')")
    public PagedModel<Log> getLogList(String start, String end, Pageable pageable) {
        Integer totalElements = Long.valueOf(logRepository.count()).intValue();
        if (totalElements <= pageable.getPageSize() * pageable.getPageNumber()) {
            pageable = PageRequest.of(
                    (totalElements - 1) / pageable.getPageSize(),
                    pageable.getPageSize(),
                    pageable.getSort());
        }

        PagedModel<Log> logPage;
        if ((start.length() > 0) && (end.length() > 0)) {
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

                logPage = new PagedModel<>(logRepository.findByCreateTimeBetween(
                        new Timestamp(df.parse(start).getTime()),
                        new Timestamp(df.parse(end).getTime()),
                        pageable));
            } catch (ParseException e) {
                logPage = new PagedModel<>(logRepository.findAll(pageable));
            }
        } else {
            logPage = new PagedModel<>(logRepository.findAll(pageable));
        }

        return logPage;
    }

    @PreAuthorize("hasAuthority('AUDIT_MANAGEMENT')")
    public List<Log> getLogList(String start, String end) {
        List<Log> logList;
        if ((start.length() > 0) && (end.length() > 0)) {
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

                logList = logRepository.findByCreateTimeBetween(
                    new Timestamp(df.parse(start).getTime()),
                    new Timestamp(df.parse(end).getTime()));
            } catch (ParseException e) {
                logList = logRepository.findAll();
            }
        } else {
            logList = logRepository.findAll();
        }

        return logList;
    }

    @PreAuthorize("hasAuthority('AUDIT_MANAGEMENT')")
    public Log getLog(Integer id) {
        Log log = logRepository.findById(id).orElse(null);

        return log;
    }

    @PreAuthorize("hasAuthority('AUDIT_MANAGEMENT')")
    public Log addLog(Log newLog) {
        Log updatedLog = logRepository.save(newLog);

        return updatedLog;
    }

    @PreAuthorize("hasAuthority('AUDIT_MANAGEMENT')")
    public void updateLog(Integer id, Log newLog) {
        logRepository.save(newLog);
    }

    @Transactional
    @PreAuthorize("hasAuthority('AUDIT_MANAGEMENT')")
    public void removeLog(Integer id) {
        Log deletedLog = logRepository.findById(id).orElse(null);

        logRepository.delete(deletedLog);
    }

    @Transactional
    @PreAuthorize("hasAuthority('AUDIT_MANAGEMENT')")
    public void removeLogs(List<Log> logList) {
        logRepository.deleteAll(logList);
    }

}

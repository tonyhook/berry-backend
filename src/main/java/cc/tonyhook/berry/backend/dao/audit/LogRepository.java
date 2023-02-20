package cc.tonyhook.berry.backend.dao.audit;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import cc.tonyhook.berry.backend.entity.audit.Log;

public interface LogRepository extends ListCrudRepository<Log, Integer>, PagingAndSortingRepository<Log, Integer> {

    List<Log> findByCreateTimeBetween(Timestamp start, Timestamp end);
    Page<Log> findByCreateTimeBetween(Timestamp start, Timestamp end, Pageable pageable);

}

package cc.tonyhook.berry.backend.dao.audit;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import cc.tonyhook.berry.backend.entity.audit.Log;

public interface LogRepository extends ListCrudRepository<Log, Integer>, PagingAndSortingRepository<Log, Integer> {

}

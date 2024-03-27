package cc.tonyhook.berry.backend.dao.visitor;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import cc.tonyhook.berry.backend.entity.visitor.SearchLog;

public interface SearchLogRepository extends ListCrudRepository<SearchLog, Integer> {

    SearchLog findTopByOpenidAndResourceTypeAndKeywordsOrderByUpdateTimeDesc(String openid, String resourceType, String keywords);
    List<SearchLog> findByOpenidAndResourceTypeOrderByUpdateTimeDesc(String openid, String resourceType);
    List<SearchLog> deleteByOpenidAndResourceType(String openid, String resourceType);

}
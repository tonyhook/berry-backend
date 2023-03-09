package cc.tonyhook.berry.backend.service.visitor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.visitor.SearchLogRepository;
import cc.tonyhook.berry.backend.entity.visitor.SearchLog;
import jakarta.transaction.Transactional;

@Service
public class SearchLogService {

    @Autowired
    private SearchLogRepository searchLogRepository;

    public Page<String> getSearchKeywordsList(String openid, String resourceType, Pageable pageable) {
        List<SearchLog> searchLogList = searchLogRepository.findByOpenidAndResourceTypeOrderByUpdateTimeDesc(openid, resourceType);
        Set<String> keywordsSet = new LinkedHashSet<String>();

        for (SearchLog searchLog : searchLogList) {
            if (!keywordsSet.contains(searchLog.getKeywords())) {
                keywordsSet.add(searchLog.getKeywords());
            }
        }

        Page<String> searchKeywordsPage = new PageImpl<String>(new ArrayList<String>(keywordsSet), pageable, keywordsSet.size());

        return searchKeywordsPage;
    }

    public SearchLog getSearchLog(String openid, String resourceType, String keywords) {
        SearchLog searchLog = searchLogRepository.findTopByOpenidAndResourceTypeAndKeywordsOrderByUpdateTimeDesc(openid, resourceType, keywords);

        return searchLog;
    }

    @Transactional
    public void clearSearchLog(String openid, String resourceType) {
        searchLogRepository.deleteByOpenidAndResourceType(openid, resourceType);
    }

    public List<SearchLog> getSearchLogList() {
        List<SearchLog> searchLogList = searchLogRepository.findAll();

        return searchLogList;
    }

    public SearchLog getSearchLog(Integer id) {
        SearchLog searchLog = searchLogRepository.findById(id).orElse(null);

        return searchLog;
    }

    public SearchLog addSearchLog(SearchLog newSearchLog) {
        SearchLog updatedSearchLog = searchLogRepository.save(newSearchLog);

        return updatedSearchLog;
    }

    public void updateSearchLog(Integer id, SearchLog newSearchLog) {
        searchLogRepository.save(newSearchLog);
    }

    @Transactional
    public void removeSearchLog(Integer id) {
        SearchLog deletedSearchLog = searchLogRepository.findById(id).orElse(null);

        searchLogRepository.delete(deletedSearchLog);
    }

    @Transactional
    public void removeSearchLogs(List<SearchLog> searchLogList) {
        searchLogRepository.deleteAll(searchLogList);
    }

}

package cc.tonyhook.berry.backend.dao.visitor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import cc.tonyhook.berry.backend.entity.visitor.ProfileLog;

public interface ProfileLogRepository extends ListCrudRepository<ProfileLog, Integer>, PagingAndSortingRepository<ProfileLog, Integer> {

    ProfileLog findTopByOpenidAndResourceTypeOrderByUpdateTimeDesc(String openid, String resourceType);
    ProfileLog findTopByOpenidAndResourceTypeAndResourceIdOrderByUpdateTimeDesc(String openid, String resourceType, Integer resourceId);
    ProfileLog findTopByOpenidAndResourceTypeAndActionOrderByUpdateTimeDesc(String openid, String resourceType, Integer action);
    ProfileLog findTopByOpenidAndResourceTypeAndResourceIdAndActionOrderByUpdateTimeDesc(String openid, String resourceType, Integer resourceId, Integer action);
    List<ProfileLog> findByOpenidAndResourceTypeOrderByUpdateTimeDesc(String openid, String resourceType);
    List<ProfileLog> findByOpenidAndResourceTypeAndResourceIdOrderByUpdateTimeDesc(String openid, String resourceType, Integer resourceId);
    List<ProfileLog> findByOpenidAndResourceTypeAndActionOrderByUpdateTimeDesc(String openid, String resourceType, Integer action);
    List<ProfileLog> findByOpenidAndResourceTypeAndResourceIdAndActionOrderByUpdateTimeDesc(String openid, String resourceType, Integer resourceId, Integer action);
    Page<ProfileLog> findByOpenidAndResourceTypeOrderByUpdateTimeDesc(String openid, String resourceType, Pageable pageable);
    Page<ProfileLog> findByOpenidAndResourceTypeAndResourceIdOrderByUpdateTimeDesc(String openid, String resourceType, Integer resourceId, Pageable pageable);
    Page<ProfileLog> findByOpenidAndResourceTypeAndActionOrderByUpdateTimeDesc(String openid, String resourceType, Integer action, Pageable pageable);
    Page<ProfileLog> findByOpenidAndResourceTypeAndResourceIdAndActionOrderByUpdateTimeDesc(String openid, String resourceType, Integer resourceId, Integer action, Pageable pageable);

}

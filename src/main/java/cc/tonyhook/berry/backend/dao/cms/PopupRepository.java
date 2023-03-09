package cc.tonyhook.berry.backend.dao.cms;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import cc.tonyhook.berry.backend.entity.cms.Popup;

public interface PopupRepository extends ListCrudRepository<Popup, Integer> {

    List<Popup> findByListOrderBySequence(String list);
    List<Popup> findByListAndDisabledOrderBySequence(String list, Boolean disabled);
    Popup findByIdAndDisabled(Integer id, Boolean disabled);
    List<Popup> findByListAndDisabledAndCode(String list, Boolean disabled, String code);

}

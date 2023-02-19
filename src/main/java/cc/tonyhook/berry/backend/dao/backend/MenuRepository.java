package cc.tonyhook.berry.backend.dao.backend;

import org.springframework.data.repository.ListCrudRepository;

import cc.tonyhook.berry.backend.entity.backend.Menu;

public interface MenuRepository extends ListCrudRepository<Menu, Integer> {

}

package cc.tonyhook.berry.backend.dao.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.backend.Menu;

public interface MenuRepository extends JpaRepository<Menu, Integer> {

}

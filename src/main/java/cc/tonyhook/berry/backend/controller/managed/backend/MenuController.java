package cc.tonyhook.berry.backend.controller.managed.backend;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.berry.backend.entity.backend.Menu;
import cc.tonyhook.berry.backend.service.backend.MenuService;

@RestController
public class MenuController {

    @Autowired
    private MenuService menuService;

    @RequestMapping(value = "/api/managed/menu", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Menu>> getMenuList() {
        List<Menu> menuList = menuService.getMenuList();

        return ResponseEntity.ok().body(menuList);
    }

    @RequestMapping(value = "/api/managed/menu/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Menu> getMenu(
            @PathVariable Integer id) {
        Menu menu = menuService.getMenu(id);

        if (menu != null) {
            return ResponseEntity.ok().body(menu);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/managed/menu", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<Menu> addMenu(
            @RequestBody Menu newMenu) throws URISyntaxException {
        Menu updatedMenu = menuService.addMenu(newMenu);

        return ResponseEntity
                .created(new URI("/api/managed/menu/" + updatedMenu.getId()))
                .body(updatedMenu);
    }

    @RequestMapping(value = "/api/managed/menu/{id}", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateMenu(
            @PathVariable Integer id,
            @RequestBody Menu newMenu) {
        if (!id.equals(newMenu.getId())) {
            return ResponseEntity.badRequest().build();
        }

        Menu targetMenu = menuService.getMenu(id);
        if (targetMenu == null) {
            return ResponseEntity.notFound().build();
        }

        menuService.updateMenu(id, newMenu);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/menu/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeMenu(
            @PathVariable Integer id) {
        Menu deletedMenu = menuService.getMenu(id);
        if (deletedMenu == null) {
            return ResponseEntity.notFound().build();
        }

        menuService.removeMenu(id);

        return ResponseEntity.ok().build();
    }

}

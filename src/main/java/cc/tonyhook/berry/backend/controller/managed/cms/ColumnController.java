package cc.tonyhook.berry.backend.controller.managed.cms;

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

import cc.tonyhook.berry.backend.entity.cms.Column;
import cc.tonyhook.berry.backend.service.cms.ColumnService;

@RestController
public class ColumnController {

    @Autowired
    private ColumnService columnService;

    @RequestMapping(value = "/api/managed/column", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<Column>> getColumnList() {
        List<Column> columnList = columnService.getColumnList();

        return ResponseEntity.ok().body(columnList);
    }

    @RequestMapping(value = "/api/managed/column/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Column> getColumn(
            @PathVariable Integer id) {
        Column column = columnService.getColumn(id);

        if (column != null) {
            return ResponseEntity.ok().body(column);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/managed/column", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<Column> addColumn(
            @RequestBody Column newColumn) throws URISyntaxException {
        Column updatedColumn = columnService.addColumn(newColumn);

        return ResponseEntity
                .created(new URI("/api/managed/column/" + updatedColumn.getId()))
                .body(updatedColumn);
    }

    @RequestMapping(value = "/api/managed/column/{id}", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateColumn(
            @PathVariable Integer id,
            @RequestBody Column newColumn) {
        if (!id.equals(newColumn.getId())) {
            return ResponseEntity.badRequest().build();
        }

        Column targetColumn = columnService.getColumn(id);
        if (targetColumn == null) {
            return ResponseEntity.notFound().build();
        }

        columnService.updateColumn(id, newColumn);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/column/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeColumn(
            @PathVariable Integer id) {
        Column deletedColumn = columnService.getColumn(id);
        if (deletedColumn == null) {
            return ResponseEntity.notFound().build();
        }

        columnService.removeColumn(id);

        return ResponseEntity.ok().build();
    }

}

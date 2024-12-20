package cc.tonyhook.berry.backend.controller.managed.audit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cc.tonyhook.berry.backend.entity.audit.Log;
import cc.tonyhook.berry.backend.service.audit.LogService;

@RestController
public class LogController {

    @Autowired
    private LogService logService;

    @RequestMapping(value = "/api/managed/log/download", method = RequestMethod.GET, produces = "application/vnd.ms-excel")
    public ResponseEntity<byte[]> download(
            @RequestParam(defaultValue = "false") Boolean clear,
            @RequestParam(defaultValue = "") String start,
            @RequestParam(defaultValue = "") String end) {
        List<Log> logList = logService.getLogList(start, end);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Log");
        Row header = sheet.createRow(0);

        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("Time");
        headerCell = header.createCell(1);
        headerCell.setCellValue("Level");
        headerCell = header.createCell(2);
        headerCell.setCellValue("User ID");
        headerCell = header.createCell(3);
        headerCell.setCellValue("Username");
        headerCell = header.createCell(4);
        headerCell.setCellValue("Request Method");
        headerCell = header.createCell(5);
        headerCell.setCellValue("Request Type");
        headerCell = header.createCell(6);
        headerCell.setCellValue("Resource ID");
        headerCell = header.createCell(7);
        headerCell.setCellValue("Parmeter");
        headerCell = header.createCell(8);
        headerCell.setCellValue("Request Body");
        headerCell = header.createCell(9);
        headerCell.setCellValue("Response Code");
        headerCell = header.createCell(10);
        headerCell.setCellValue("Response Body");

        Integer line = 0;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        for (Log log : logList) {
            line++;
            Row row = sheet.createRow(line);
            Cell cell = row.createCell(0);
            cell.setCellValue(df.format(log.getCreateTime()));
            cell = row.createCell(1);
            cell.setCellValue(log.getLevel());
            cell = row.createCell(2);
            cell.setCellValue(log.getUserId());
            cell = row.createCell(3);
            cell.setCellValue(log.getUsername());
            cell = row.createCell(4);
            cell.setCellValue(log.getRequestMethod());
            cell = row.createCell(5);
            cell.setCellValue(log.getRequestResourceType());
            cell = row.createCell(6);
            cell.setCellValue(log.getRequestResourceId());
            cell = row.createCell(7);
            cell.setCellValue(log.getRequestParmeter());
            cell = row.createCell(8);
            cell.setCellValue(new String(log.getRequestBody()));
            cell = row.createCell(9);
            cell.setCellValue(log.getResponseCode());
            cell = row.createCell(10);
            cell.setCellValue(new String(log.getResponseBody()));
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            if (clear) {
                logService.removeLogs(logList);
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .header("Content-Disposition", "attachment; filename=log.xlsx")
                .body(outputStream.toByteArray());
        } catch (IOException e) {
            return null;
        }
    }

    @RequestMapping(value = "/api/managed/log", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<PagedModel<Log>> getLogList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "") String start,
            @RequestParam(defaultValue = "") String end) {
        Direction direction = order.equals("desc") ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, direction, sort);

        PagedModel<Log> logPage = logService.getLogList(start, end, pageable);

        return ResponseEntity.ok().body(logPage);
    }

    @RequestMapping(value = "/api/managed/log/{id}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Log> getLog(
            @PathVariable Integer id) {
        Log Log = logService.getLog(id);

        if (Log != null) {
            return ResponseEntity.ok().body(Log);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/api/managed/log", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<Log> addLog(
            @RequestBody Log newLog) throws URISyntaxException {
        Log updatedLog = logService.addLog(newLog);

        return ResponseEntity
                .created(new URI("/api/managed/log/" + updatedLog.getId()))
                .body(updatedLog);
    }

    @RequestMapping(value = "/api/managed/log/{id}", method = RequestMethod.PUT, consumes = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateLog(
            @PathVariable Integer id,
            @RequestBody Log newLog) {
        if (!id.equals(newLog.getId())) {
            return ResponseEntity.badRequest().build();
        }

        Log targetLog = logService.getLog(id);
        if (targetLog == null) {
            return ResponseEntity.notFound().build();
        }

        logService.updateLog(id, newLog);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/api/managed/log/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeLog(
            @PathVariable Integer id) {
        Log deletedLog = logService.getLog(id);
        if (deletedLog == null) {
            return ResponseEntity.notFound().build();
        }

        logService.removeLog(id);

        return ResponseEntity.ok().build();
    }

}

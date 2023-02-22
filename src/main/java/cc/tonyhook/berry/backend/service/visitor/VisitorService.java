package cc.tonyhook.berry.backend.service.visitor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.visitor.VisitorRepository;
import cc.tonyhook.berry.backend.entity.visitor.Visitor;
import jakarta.transaction.Transactional;

@Service
public class VisitorService {

    @Autowired
    private VisitorRepository visitorRepository;

    public Page<Visitor> getVisitorList(Pageable pageable) {
        Integer totalElements = Long.valueOf(visitorRepository.count()).intValue();
        if (totalElements <= pageable.getPageSize() * pageable.getPageNumber()) {
            pageable = PageRequest.of(
                    (totalElements - 1) / pageable.getPageSize(),
                    pageable.getPageSize(),
                    pageable.getSort());
        }

        Page<Visitor> visitorList = visitorRepository.findAll(pageable);

        return visitorList;
    }

    public List<Visitor> getVisitorList() {
        List<Visitor> visitorList = visitorRepository.findAll();

        return visitorList;
    }

    public Visitor getVisitor(String openid) {
        Visitor visitor = visitorRepository.findById(openid).orElse(null);

        return visitor;
    }

    public Visitor addVisitor(Visitor newVisitor) {
        Visitor updatedVisitor = visitorRepository.save(newVisitor);

        return updatedVisitor;
    }

    public void addVisitors(List<Visitor> visitorList) {
        visitorRepository.saveAll(visitorList);
    }

    public void updateVisitor(String openid, Visitor newVisitor) {
        visitorRepository.save(newVisitor);
    }

    @Transactional
    public void removeVisitor(String openid) {
        Visitor deletedVisitor = visitorRepository.findById(openid).orElse(null);

        visitorRepository.delete(deletedVisitor);
    }

    @Transactional
    public void removeVisitors(List<Visitor> visitorList) {
        visitorRepository.deleteAll(visitorList);
    }

}

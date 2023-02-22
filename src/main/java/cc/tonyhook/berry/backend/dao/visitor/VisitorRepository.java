package cc.tonyhook.berry.backend.dao.visitor;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import cc.tonyhook.berry.backend.entity.visitor.Visitor;

public interface VisitorRepository extends ListCrudRepository<Visitor, String>, PagingAndSortingRepository<Visitor, String> {

}

package cc.tonyhook.berry.backend.dao.visitor;

import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.visitor.Visitor;

public interface VisitorRepository extends JpaRepository<Visitor, String> {

}

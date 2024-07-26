package cc.tonyhook.berry.backend.dao.visitor;

import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.visitor.Agreement;

public interface AgreementRepository extends JpaRepository<Agreement, Integer> {

   Agreement findTopByNameOrderByVersionDesc(String name);
   Agreement findByNameAndVersion(String name, Integer version);

}

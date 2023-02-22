package cc.tonyhook.berry.backend.dao.visitor;

import org.springframework.data.repository.ListCrudRepository;

import cc.tonyhook.berry.backend.entity.visitor.Agreement;

public interface AgreementRepository extends ListCrudRepository<Agreement, Integer> {

   Agreement findTopByNameOrderByVersionDesc(String name);
   Agreement findByNameAndVersion(String name, Integer version);

}

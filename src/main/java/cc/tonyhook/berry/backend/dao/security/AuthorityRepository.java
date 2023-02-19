package cc.tonyhook.berry.backend.dao.security;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import cc.tonyhook.berry.backend.entity.security.Authority;

public interface AuthorityRepository extends ListCrudRepository<Authority, Integer>, PagingAndSortingRepository<Authority, Integer> {

}

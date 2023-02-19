package cc.tonyhook.berry.backend.dao.security;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import cc.tonyhook.berry.backend.entity.security.User;

public interface UserRepository extends ListCrudRepository<User, Integer>, PagingAndSortingRepository<User, Integer> {

}

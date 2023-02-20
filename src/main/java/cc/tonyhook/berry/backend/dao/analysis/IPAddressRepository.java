package cc.tonyhook.berry.backend.dao.analysis;

import org.springframework.data.repository.ListCrudRepository;

import cc.tonyhook.berry.backend.entity.analysis.IPAddress;

public interface IPAddressRepository extends ListCrudRepository<IPAddress, Integer> {

}

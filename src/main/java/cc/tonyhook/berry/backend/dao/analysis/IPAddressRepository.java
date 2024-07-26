package cc.tonyhook.berry.backend.dao.analysis;

import org.springframework.data.jpa.repository.JpaRepository;

import cc.tonyhook.berry.backend.entity.analysis.IPAddress;

public interface IPAddressRepository extends JpaRepository<IPAddress, Integer> {

}

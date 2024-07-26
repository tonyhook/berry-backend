package cc.tonyhook.berry.backend.service.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.security.AuthorityRepository;
import cc.tonyhook.berry.backend.dao.security.RoleRepository;
import cc.tonyhook.berry.backend.entity.security.Authority;
import cc.tonyhook.berry.backend.entity.security.Role;
import jakarta.transaction.Transactional;

@Service
public class AuthorityService {

    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private RoleRepository roleRepository;

    @PreAuthorize("hasAuthority('SECURITY_MANAGEMENT')")
    public PagedModel<Authority> getAuthorityList(Pageable pageable) {
        Integer totalElements = Long.valueOf(authorityRepository.count()).intValue();
        if (totalElements <= pageable.getPageSize() * pageable.getPageNumber()) {
            pageable = PageRequest.of(
                    (totalElements - 1) / pageable.getPageSize(),
                    pageable.getPageSize(),
                    pageable.getSort());
        }

        PagedModel<Authority> authorityPage = new PagedModel<>(authorityRepository.findAll(pageable));

        return authorityPage;
    }

    @PreAuthorize("hasAuthority('SECURITY_MANAGEMENT')")
    public Authority getAuthority(Integer id) {
        Authority authority = authorityRepository.findById(id).orElse(null);

        return authority;
    }

    @PreAuthorize("hasAuthority('SECURITY_MANAGEMENT')")
    public Authority addAuthority(Authority newAuthority) {
        Authority updatedAuthority = authorityRepository.save(newAuthority);

        return updatedAuthority;
    }

    @PreAuthorize("hasAuthority('SECURITY_MANAGEMENT')")
    public void updateAuthority(Integer id, Authority newAuthority) {
        authorityRepository.save(newAuthority);
    }

    @Transactional
    @PreAuthorize("hasAuthority('SECURITY_MANAGEMENT')")
    public void removeAuthority(Integer id) {
        Authority deletedAuthority = authorityRepository.findById(id).orElse(null);

        List<Role> roleList = roleRepository.findAll();
        for (Role role : roleList) {
            if (role.getAuthorities().contains(deletedAuthority)) {
                role.getAuthorities().remove(deletedAuthority);
                roleRepository.save(role);
            }
        }

        authorityRepository.delete(deletedAuthority);
    }

}

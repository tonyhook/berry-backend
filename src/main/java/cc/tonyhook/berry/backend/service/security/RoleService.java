package cc.tonyhook.berry.backend.service.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.security.PermissionRepository;
import cc.tonyhook.berry.backend.dao.security.RoleRepository;
import cc.tonyhook.berry.backend.dao.security.UserRepository;
import cc.tonyhook.berry.backend.entity.security.Role;
import cc.tonyhook.berry.backend.entity.security.User;
import jakarta.transaction.Transactional;

@Service
public class RoleService {

    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasAuthority('SECURITY_MANAGEMENT')")
    public PagedModel<Role> getRoleList(Pageable pageable) {
        Integer totalElements = Long.valueOf(roleRepository.count()).intValue();
        if (totalElements <= pageable.getPageSize() * pageable.getPageNumber()) {
            pageable = PageRequest.of(
                    (totalElements - 1) / pageable.getPageSize(),
                    pageable.getPageSize(),
                    pageable.getSort());
        }

        PagedModel<Role> rolePage = new PagedModel<>(roleRepository.findAll(pageable));

        return rolePage;
    }

    @PreAuthorize("hasAuthority('SECURITY_MANAGEMENT')")
    public Role getRole(Integer id) {
        Role role = roleRepository.findById(id).orElse(null);

        return role;
    }

    @PreAuthorize("hasAuthority('SECURITY_MANAGEMENT')")
    public Role addRole(Role newRole) {
        Role updatedRole = roleRepository.save(newRole);

        return updatedRole;
    }

    @PreAuthorize("hasAuthority('SECURITY_MANAGEMENT')")
    public void updateRole(Integer id, Role newRole) {
        roleRepository.save(newRole);
    }

    @Transactional
    @PreAuthorize("hasAuthority('SECURITY_MANAGEMENT')")
    public void removeRole(Integer id) {
        Role deletedRole = roleRepository.findById(id).orElse(null);

        deletedRole.getAuthorities().clear();
        roleRepository.save(deletedRole);

        List<User> userList = userRepository.findAll();
        for (User user : userList) {
            if (user.getRoles().contains(deletedRole)) {
                user.getRoles().remove(deletedRole);
                userRepository.save(user);
            }
        }

        permissionRepository.deleteByRoleId(id);
        roleRepository.delete(deletedRole);
    }

}

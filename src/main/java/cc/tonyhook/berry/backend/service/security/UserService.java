package cc.tonyhook.berry.backend.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.security.UserRepository;
import cc.tonyhook.berry.backend.entity.security.User;
import jakarta.transaction.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasAuthority('SECURITY_MANAGEMENT')")
    public Page<User> getUserList(Pageable pageable) {
        Integer totalElements = Long.valueOf(userRepository.count()).intValue();
        if (totalElements <= pageable.getPageSize() * pageable.getPageNumber()) {
            pageable = PageRequest.of(
                    (totalElements - 1) / pageable.getPageSize(),
                    pageable.getPageSize(),
                    pageable.getSort());
        }

        Page<User> userPage = userRepository.findAll(pageable);

        return userPage;
    }

    @PreAuthorize("hasAuthority('SECURITY_MANAGEMENT')")
    public User getUser(Integer id) {
        User user = userRepository.findById(id).orElse(null);

        return user;
    }

    @PreAuthorize("hasAuthority('SECURITY_MANAGEMENT')")
    public User addUser(User newUser) {
        User updatedUser = userRepository.save(newUser);

        return updatedUser;
    }

    @PreAuthorize("hasAuthority('SECURITY_MANAGEMENT')")
    public void updateUser(Integer id, User newUser) {
        userRepository.save(newUser);
    }

    @Transactional
    @PreAuthorize("hasAuthority('SECURITY_MANAGEMENT')")
    public void removeUser(Integer id) {
        User deletedUser = userRepository.findById(id).orElse(null);

        deletedUser.getRoles().clear();
        userRepository.save(deletedUser);

        userRepository.delete(deletedUser);
    }

}

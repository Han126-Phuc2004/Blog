package service;
import entity.Role;
import org.springframework.stereotype.Service;
import repository.RoleRepository;

import java.util.List;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role save(Role role) {
        return roleRepository.save(role);
    }

    public Role findById(Integer id) {
        return roleRepository.findById(id).orElse(null);
    }

    public void deleteById(Integer id) {
        roleRepository.deleteById(id);
    }

    public Role findByName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }
}
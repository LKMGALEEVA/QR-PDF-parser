package aseproject.qrpdfparser.service;

import aseproject.qrpdfparser.config.AppUserDetails;
import aseproject.qrpdfparser.model.User;
import aseproject.qrpdfparser.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AppUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByName(username);
        return user.map(AppUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(username + "not found"));
    }
}

package io.onboard.userservice.service;

import io.onboard.userservice.RandomUtil;
import io.onboard.userservice.domain.Authority;
import io.onboard.userservice.domain.User;
import io.onboard.userservice.repository.UserRepository;
import io.onboard.userservice.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);


    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserSearchRepository userSearchRepository;

    private final AuthorityRepository authorityRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserSearchRepository userSearchRepository, AuthorityRepository authorityRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userSearchRepository = userSearchRepository;
        this.authorityRepository = authorityRepository;
    }

    public Mono<User> registerUser(AdminUserDTO userDTO, String password) {
        return userRepository
                .findOneByLogin(userDTO.getLogin().toLowerCase())
                .flatMap(existingUser -> {
                    if(!existingUser.isActivated()) {
                        return userRepository.delete(existingUser);
                    } else {
                        return Mono.error(new UsernameAlreadyUsedException());
                    }
                }).then(userRepository.findOneByEmailIgnoreCase(userDTO.getEmail()))
                .flatMap(existingUser -> {
                    if (!existingUser.isActivated()) {
                        return userRepository.delete(existingUser);
                    } else {
                        return Mono.error(new EmailAlreadyUsedException());
                    }
                })
                .publishOn(Schedulers.boundedElastic())
                .then(
                        Mono.fromCallable(() -> {
                            User newUser = new User();
                            String encryptedPassword = passwordEncoder.encode(password);
                            newUser.setLogin(userDTO.getLogin().toLowerCase());
                            newUser.setPassword(encryptedPassword);
                            newUser.setFirstName(userDTO.getFirstName());
                            newUser.setLastName(userDTO.getFirstName());
                            if(userDTO.getEmail() != null) {
                                newUser.setEmail(userDTO.getEmail().toLowerCase());
                            }
                            newUser.setActivated(false);
                            newUser.setActivationKey(RandomUtil.generateActivationKey());
                            return newUser;

                        })
                ).flatMap(newUser -> {
                    Set<Authority> authorities = new HashSet<>();
                    return authorityRepository
                            .findById(AuthoritiesConstants.USER)
                            .map(authorities::add)
                            .thenReturn(newUser)
                            .doOnNext(user -> user.setAuthorities(authorities))
                            .flatMap(this::saveUser)
                            .flatMap(user -> userSearchRepository.save(user).thenReturn(user))
                            .doOnNext(user -> log.debug("Created Information for User: {}", user));
                });
    }

    private Mono<User> saveUser(User user) {
        return SecurityUtils
                .getCurrentUserLogin()
                .switchIfEmpty(Mono.just(Constants.SYSTEM))
    }
}

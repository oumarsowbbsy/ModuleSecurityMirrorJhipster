package io.onboard.userservice.repository;

import io.onboard.userservice.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findOneByActivationKey(String activation);
    Flux<User> findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant dateTime);
    Mono<User> findOneByResetKey(String resetKey);
    Mono<User> findOneByEmailIgnoreCase(String email);
    Mono<User> findOneByLogin(String login);

    Flux<User> findAllByIdNotNull(Pageable pageable);
    Flux<User> findAllByIdNotNullAndActivatedIsTrue(Pageable pageable);
    Mono<Long> count();
}

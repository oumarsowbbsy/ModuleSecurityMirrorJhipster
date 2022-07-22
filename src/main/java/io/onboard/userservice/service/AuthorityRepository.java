package io.onboard.userservice.service;


import io.onboard.userservice.domain.Authority;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface AuthorityRepository extends ReactiveMongoRepository<Authority, String> {
}

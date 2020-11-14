package com.voroby.elasticclient.repository;

import com.voroby.elasticclient.domain.eom.UserEom;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ElasticUserRepository extends CrudRepository<UserEom, String> {

    Optional<UserEom> findByEmail(String email);

}

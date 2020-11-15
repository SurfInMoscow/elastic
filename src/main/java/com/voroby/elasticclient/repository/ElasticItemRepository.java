package com.voroby.elasticclient.repository;

import com.voroby.elasticclient.domain.eom.ItemEom;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ElasticItemRepository extends CrudRepository<ItemEom, String> {

    Optional<ItemEom> findByName(String name);
}
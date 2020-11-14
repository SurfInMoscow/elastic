package com.voroby.elasticclient.dataelastic;

import com.voroby.elasticclient.ElasticClientApplication;
import com.voroby.elasticclient.domain.eom.ItemEom;
import com.voroby.elasticclient.domain.eom.UserEom;
import com.voroby.elasticclient.repository.ElasticUserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRepositoryTest {
    @Autowired
    protected ElasticUserRepository elasticUserRepository;

    @Test
    public void save() {
        UserEom userEom = new UserEom("user@ya.ru", "password");
        ItemEom item = new ItemEom("Item1", "test item", userEom);
        ItemEom item1 = new ItemEom("Item2", "test item", userEom);
        userEom.getItems().addAll(Set.of(item, item1));

        UserEom userEom1 = elasticUserRepository.save(userEom);

        assertEquals(userEom, userEom1);

        userEom.getItems().remove(item1);

        userEom1 = elasticUserRepository.save(userEom);

        assertEquals(userEom, userEom1);
    }

    @Test
    public void get() {
        UserEom userEom = new UserEom("user@ya.ru", "password");
        ItemEom item = new ItemEom("Item1", "test item", userEom);
        userEom.getItems().addAll(Set.of(item));
        elasticUserRepository.save(userEom);

        Optional<UserEom> found = elasticUserRepository.findById(userEom.getId());
        UserEom getFound = found.orElseGet(UserEom::new);
        //Setting owner to items in collection because it's transfer state doesn't include it
        getFound.getItems().forEach(i -> i.setOwner(getFound));

        assertEquals(userEom, getFound);
    }

    @Test
    public void delete() {
        UserEom userEom = new UserEom("user@ya.ru", "password");
        ItemEom item = new ItemEom("Item1", "test item", userEom);
        userEom.getItems().addAll(Set.of(item));
        elasticUserRepository.save(userEom);

        elasticUserRepository.delete(userEom);

        Optional<UserEom> found = elasticUserRepository.findById(userEom.getId());

        assertThrows(NullPointerException.class, () -> found.orElseThrow(NullPointerException::new));
    }

    @Test
    public void getByEmail() {
        UserEom userEom = new UserEom("super@mail.ru", "password");
        ItemEom item = new ItemEom("TestItem", "test item", userEom);
        userEom.getItems().addAll(Set.of(item));
        elasticUserRepository.save(userEom);

        Optional<UserEom> byEmail = elasticUserRepository.findByEmail(userEom.getEmail());

        assertEquals(userEom, byEmail.orElseGet(UserEom::new));
    }

    @Test
    public void saveAll() {
        List<UserEom> userEoms = IntStream.range(0, 100)
                .mapToObj(i -> new UserEom(i + "user@ya.ru", "password"))
                .collect(Collectors.toList());

        Iterable<UserEom> savedUserEoms = elasticUserRepository.saveAll(userEoms);

        assertEquals(userEoms, savedUserEoms);
    }

    @Test
    @Order(Integer.MAX_VALUE)
    public void getAll() {
        Iterable<UserEom> userEoms = elasticUserRepository.findAll();

        List<UserEom> userEoms1 = new ArrayList<>();

        for (UserEom userEom : userEoms) {
            userEoms1.add(userEom);
        }

        assertTrue(userEoms1.size() > 1);
    }

    @AfterAll
    public static void cleanUp() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(ElasticClientApplication.class);
        applicationContext.refresh();
        ElasticUserRepository elasticUserRepository = applicationContext.getBean(ElasticUserRepository.class);
        elasticUserRepository.deleteAll();
    }
}
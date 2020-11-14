package com.voroby.elasticclient.dataelastic;

import com.voroby.elasticclient.ElasticClientApplication;
import com.voroby.elasticclient.domain.eom.ItemEom;
import com.voroby.elasticclient.domain.eom.UserEom;
import com.voroby.elasticclient.repository.ElasticItemRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ItemRepositoryTest {
    @Autowired
    private ElasticItemRepository elasticItemRepository;

    @Test
    public void save() {
        UserEom userEom = new UserEom("user@ya.ru", "password");
        ItemEom itemEom = new ItemEom("Item1", "test item", userEom);

        ItemEom itemEom1 = elasticItemRepository.save(itemEom);

        assertEquals(itemEom, itemEom1);
    }

    @Test
    public void get() {
        UserEom userEom = new UserEom("user@ya.ru", "password");
        ItemEom itemEom = new ItemEom("Item1", "test item", userEom);
        elasticItemRepository.save(itemEom);

        Optional<ItemEom> found = elasticItemRepository.findById(itemEom.getId());

        //Equals don't include Owner
        assertEquals(itemEom, found.orElseGet(ItemEom::new));
    }

    @Test
    public void delete() {
        UserEom userEom = new UserEom("user@ya.ru", "password");
        ItemEom itemEom = new ItemEom("Item1", "test item", userEom);
        elasticItemRepository.save(itemEom);

        elasticItemRepository.deleteById(itemEom.getId());

        Optional<ItemEom> byId = elasticItemRepository.findById(itemEom.getId());
        assertThrows(NullPointerException.class, () -> byId.orElseThrow(NullPointerException::new));
    }

    @Test
    public void getByName() {
        UserEom userEom = new UserEom("user@ya.ru", "password");
        ItemEom itemEom = new ItemEom("secretNameItem", "test item", userEom);
        elasticItemRepository.save(itemEom);

        Optional<ItemEom> found = elasticItemRepository.findByName(itemEom.getName());
        assertEquals(itemEom, found.orElseGet(ItemEom::new));
    }

    @Test
    public void saveAll() {
        List<ItemEom> itemEoms = IntStream.range(0, 100)
                .mapToObj(i -> new ItemEom("Item" + i, "test item", null))
                .collect(Collectors.toList());

        Iterable<ItemEom> itemEoms1 = elasticItemRepository.saveAll(itemEoms);
        assertIterableEquals(itemEoms, itemEoms1);
    }

    @Test
    @Order(Integer.MAX_VALUE)
    public void getAll() {
        Iterable<ItemEom> found = elasticItemRepository.findAll();
        List<ItemEom> itemEoms = new ArrayList<>();

        for (ItemEom itemEom: found) {
            itemEoms.add(itemEom);
        }

        assertTrue(itemEoms.size() > 50);
    }

    @AfterAll
    public static void cleanUp() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(ElasticClientApplication.class);
        applicationContext.refresh();
        ElasticItemRepository elasticItemRepository = applicationContext.getBean(ElasticItemRepository.class);
        elasticItemRepository.deleteAll();
    }
}

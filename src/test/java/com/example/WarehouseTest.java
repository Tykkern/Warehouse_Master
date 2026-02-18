package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseTest {

    private Warehouse warehouse;

    @BeforeEach
    void setUp() throws Exception {
        // Рефлексия, чтобы создать экземпляр несмотря на private конструктор
        var constructor = Warehouse.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        warehouse = (Warehouse) constructor.newInstance();
        try {
            Files.deleteIfExists(Paths.get("warehouse.json"));
        } catch (IOException ignored) {}
        System.out.println("Перед тестом: продуктов в складе = " + warehouse.getAllProducts().size());
    }

    @Test
    void testAddAndGetProducts() {
        AbstractProduct p1 = new FoodProduct(1, "Apple", 1.0, 10, LocalDate.now().plusDays(1));
        warehouse.addProduct(p1);

        assertEquals(1, warehouse.getAllProducts().size(), "После добавления одного продукта должно быть 1");
    }

    @Test
    void testRemoveProduct() {
        AbstractProduct p1 = new FoodProduct(1, "Apple", 1.0, 10, LocalDate.now().plusDays(1));
        warehouse.addProduct(p1);
        warehouse.removeProduct(1);
        assertTrue(warehouse.getAllProducts().isEmpty());
    }

    @Test
    void testSearchByName() {
        AbstractProduct p1 = new FoodProduct(1, "Apple", 1.0, 10, LocalDate.now().plusDays(1));
        AbstractProduct p2 = new ElectronicsProduct(2, "Phone", 100.0, 5, 12);
        warehouse.addProduct(p1);
        warehouse.addProduct(p2);

        assertEquals(1, warehouse.searchByName("App").size());
        assertEquals("Apple", warehouse.searchByName("App").get(0).getName());
    }

    @Test
    void testTotalValue() {
        AbstractProduct p1 = new FoodProduct(1, "Apple", 1.0, 10, LocalDate.now().plusDays(1));
        AbstractProduct p2 = new ElectronicsProduct(2, "Phone", 100.0, 5, 12);
        warehouse.addProduct(p1);
        warehouse.addProduct(p2);
        assertEquals(510.0, warehouse.getTotalValue(), 0.001);
    }

    @Test
    void testExpiredProducts() {
        AbstractProduct p1 = new FoodProduct(1, "Old Apple", 1.0, 10, LocalDate.now().minusDays(1));
        warehouse.addProduct(p1);
        assertEquals(1, warehouse.getExpiredFoodProducts().size());
    }

}
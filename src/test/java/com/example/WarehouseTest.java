package com.example;

import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseTest {

    private Warehouse warehouse;
    private static final String TEST_FILE_PATH = "test_warehouse.json";

    @BeforeEach
    void setUp() {
        warehouse = Warehouse.getInstance();

        // Очистка склада перед каждым тестом
        List<AbstractProduct> all = new ArrayList<>(warehouse.getAllProducts());
        for (AbstractProduct p : all) {
            warehouse.removeProduct(p.getId());
        }
        
    }

    @AfterEach
    void tearDown() {
    }
    
    //                UNIT-ТЕСТЫ
    @Test
    @DisplayName("Добавление продукта -> он появляется в списке")
    void shouldAddProduct() {
        FoodProduct milk = new FoodProduct(1001, "Молоко 3.2%", 89.90, 10, LocalDate.now().plusDays(12));
        warehouse.addProduct(milk);

        List<AbstractProduct> all = warehouse.getAllProducts();
        assertEquals(1, all.size());
        assertEquals(1001, all.get(0).getId());
        assertEquals("Молоко 3.2%", all.get(0).getName());
    }

    @Test
    @DisplayName("Удаление существующего продукта по id")
    void shouldRemoveExistingProduct() {
        ElectronicsProduct phone = new ElectronicsProduct(2001, "Poco X6", 18990, 1, 12);
        warehouse.addProduct(phone);

        warehouse.removeProduct(2001);

        assertTrue(warehouse.getAllProducts().isEmpty());
        assertTrue(warehouse.searchByName("Poco").isEmpty());
    }

    @Test
    @DisplayName("Удаление несуществующего продукта -> список не меняется")
    void removeNonExistingShouldNotFail() {
        int sizeBefore = warehouse.getAllProducts().size();

        warehouse.removeProduct(999999);

        assertEquals(sizeBefore, warehouse.getAllProducts().size());
    }

    @Test
    @DisplayName("getTotalValue считает правильную сумму")
    void shouldCalculateCorrectTotalValue() {
        warehouse.addProduct(new FoodProduct(3001, "Хлеб", 45.50, 4, LocalDate.now().plusDays(2)));
        warehouse.addProduct(new ElectronicsProduct(3002, "Зарядка 65W", 1490, 2, 6));

        double total = warehouse.getTotalValue();

        // 45.50 × 4 + 1490 × 2 = 182 + 2980 = 3162
        assertEquals(3162.0, total, 0.01);
    }

    @Test
    @DisplayName("searchByName находит продукты нечувствительно к регистру")
    void searchByNameCaseInsensitive() {
        warehouse.addProduct(new FoodProduct(4001, "Бананы", 129.90, 5, LocalDate.now().plusDays(5)));
        warehouse.addProduct(new FoodProduct(4002, "банановый йогурт", 65.0, 3, LocalDate.now().plusDays(7)));

        List<AbstractProduct> found = warehouse.searchByName("БАНАН");

        assertEquals(2, found.size());
    }

    @Test
    @DisplayName("searchByName возвращает пустой список при отсутствии совпадений")
    void searchByNameNotFound() {
        List<AbstractProduct> found = warehouse.searchByName("Космический корабль");
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("getProductTypeCounts правильно считает типы продуктов")
    void shouldCountProductTypes() {
        warehouse.addProduct(new FoodProduct(5001, "Сметана", 120, 2, LocalDate.now().plusDays(10)));
        warehouse.addProduct(new FoodProduct(5002, "Творог", 180, 1, LocalDate.now().plusDays(8)));
        warehouse.addProduct(new ElectronicsProduct(5003, "Наушники", 2490, 1, 24));

        var counts = warehouse.getProductTypeCounts();

        assertEquals(2, counts.get("FoodProduct"));
        assertEquals(1, counts.get("ElectronicsProduct"));
    }

    @Test
    @DisplayName("Добавление продукта с нулевой ценой и нулевым количеством")
    void addZeroPriceAndZeroQuantity() {
        FoodProduct zero = new FoodProduct(9001, "Пустышка", 0.0, 0, LocalDate.now().plusDays(10));
        warehouse.addProduct(zero);

        assertEquals(1, warehouse.getAllProducts().size());
        assertEquals(0.0, warehouse.getTotalValue(), 0.001);
    }

    @Test
    @DisplayName("Много продуктов — getTotalValue считает корректно")
    void manyProductsTotalValue() {
        double expected = 0;
        for (int i = 1; i <= 50; i++) {
            double price = 100.0 + i;
            int qty = i % 5 + 1;
            warehouse.addProduct(new FoodProduct(10000 + i, "Товар " + i, price, qty, LocalDate.now().plusDays(30)));
            expected += price * qty;
        }

        assertEquals(expected, warehouse.getTotalValue(), 0.1);
    }

    @Test
    @DisplayName("getExpiredFoodProducts находит просроченные продукты")
    void shouldFindExpiredFoodProducts() {
        // Текущая дата в системе — 26 февраля 2026
        FoodProduct fresh  = new FoodProduct(6001, "Молоко свежее",  90, 5, LocalDate.of(2026, 3, 10));
        FoodProduct expired = new FoodProduct(6002, "Йогурт просроченный", 55, 3, LocalDate.of(2026, 2, 20));

        warehouse.addProduct(fresh);
        warehouse.addProduct(expired);

        List<FoodProduct> expiredList = warehouse.getExpiredFoodProducts();

        assertEquals(1, expiredList.size());
        assertEquals(6002, expiredList.get(0).getId());
    }

    // ───────────────────────────────────────────────
    //           ФУНКЦИОНАЛЬНЫЕ ТЕСТЫ
    // ───────────────────────────────────────────────

    @Test
    @DisplayName("Сохранение -> очистка -> загрузка -> данные восстановлены")
    void saveLoadRoundtrip() {
        FoodProduct p1 = new FoodProduct(7001, "Консервы тушёнка", 210, 6, LocalDate.now().plusMonths(18));
        ElectronicsProduct p2 = new ElectronicsProduct(7002, "Powerbank 20000", 2490, 2, 12);

        warehouse.addProduct(p1);
        warehouse.addProduct(p2);

        JsonProductStorageStrategy strategy = new JsonProductStorageStrategy();

        try {
            // Сохраняем в тестовый файл
            strategy.save(warehouse.getAllProducts(), TEST_FILE_PATH);
        } catch (Exception e) {
            fail("Не удалось сохранить данные в файл: " + e.getMessage());
        }

        // Очищаем склад
        List<AbstractProduct> current = new ArrayList<>(warehouse.getAllProducts());
        for (AbstractProduct p : current) {
            warehouse.removeProduct(p.getId());
        }
        assertTrue(warehouse.getAllProducts().isEmpty(), "Склад должен быть пустым после очистки");

        try {
            // Загружаем обратно
            List<AbstractProduct> loaded = strategy.load(TEST_FILE_PATH);

            // Добавляем загруженные продукты обратно на склад
            loaded.forEach(warehouse::addProduct);

            List<AbstractProduct> afterLoad = warehouse.getAllProducts();
            assertEquals(2, afterLoad.size(), "После загрузки должно быть 2 продукта");

            // Проверяем наличие ключевых продуктов
            boolean hasTushenka = afterLoad.stream().anyMatch(p -> p.getName().contains("тушёнка"));
            boolean hasPowerbank = afterLoad.stream().anyMatch(p -> p.getName().contains("Powerbank"));
            assertTrue(hasTushenka, "Должен сохраниться продукт 'тушёнка'");
            assertTrue(hasPowerbank, "Должен сохраниться продукт 'Powerbank'");

        } catch (Exception e) {
            fail("Не удалось загрузить данные из файла: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Полный цикл: добавление -> поиск -> удаление -> проверка пустоты")
    void fullLifecycleTest() {
        AbstractProduct item = new ElectronicsProduct(8001, "Тестовый товар", 999, 1, 0);
        warehouse.addProduct(item);

        assertFalse(warehouse.searchByName("Тестовый").isEmpty());

        warehouse.removeProduct(8001);

        assertTrue(warehouse.getAllProducts().isEmpty());
        assertTrue(warehouse.searchByName("Тестовый").isEmpty());
    }
}
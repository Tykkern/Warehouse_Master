package com.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Warehouse {
    private static Warehouse instance;
    private final List<AbstractProduct> products = new ArrayList<>();
    private final ProductStorageStrategy storageStrategy = new JsonProductStorageStrategy();
    private final String filePath = "warehouse.json";
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private Warehouse() {
        startExpirationChecker();
    }

    public static Warehouse getInstance() {
        if (instance == null) {
            instance = new Warehouse();
        }
        return instance;
    }

    public void addProduct(AbstractProduct product) {
        products.add(product);
        //дебаг вывод
       // System.out.println("Добавлен продукт. Текущий размер списка: " + products.size()
         //       + " | isTestMode = " + isTestMode);
        saveData();
    }

    public void removeProduct(int id) {
        products.removeIf(p -> p.getId() == id);
        saveData();
    }

    public List<AbstractProduct> getAllProducts() {
        return new ArrayList<>(products);
    }

    // Streams: Поиск по имени
    public List<AbstractProduct> searchByName(String name) {
        return products.stream()
                .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    // Streams: Аналитика - общая стоимость товаров
    public double getTotalValue() {
        return products.stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();
    }

    // Streams: Аналитика - количество по типам
    public Map<String, Long> getProductTypeCounts() {
        return products.stream()
                .collect(Collectors.groupingBy(p -> p.getClass().getSimpleName(), Collectors.counting()));
    }

    // Streams: Фильтрация просроченных продуктов
    public List<FoodProduct> getExpiredFoodProducts() {
        LocalDate now = LocalDate.now();
        return products.stream()
                .filter(p -> p instanceof FoodProduct)
                .map(p -> (FoodProduct) p)
                .filter(fp -> fp.getExpirationDate().isBefore(now))
                .collect(Collectors.toList());
    }

    public void loadData() {
        try {
            List<AbstractProduct> loaded = storageStrategy.load(filePath);
            products.clear();
            products.addAll(loaded);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки данных: " + e.getMessage());
            e.printStackTrace();  //дебаг
        }
    }

    //для теста private boolean isTestMode = false;

    // public void setTestMode(boolean testMode) {
    //    this.isTestMode = testMode;
    //}

    private void saveData() {
        //if (isTestMode) {
        //    return; // не сохраняем в тестах
        //}
        try {
            storageStrategy.save(products, filePath);
        } catch (Exception e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    // Multithreading: Фоновый поток для проверки срока годности
    private void startExpirationChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            List<FoodProduct> expired = getExpiredFoodProducts();
            if (!expired.isEmpty()) {
                System.out.println("Notification: Expired products found: " + expired.size());
                expired.forEach(p -> System.out.println("Expired: " + p));
            } else {
                System.out.println("No expired products.");
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
    }


}
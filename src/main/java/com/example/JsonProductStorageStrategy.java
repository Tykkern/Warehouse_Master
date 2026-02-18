package com.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JsonProductStorageStrategy implements ProductStorageStrategy {
    private final Gson gson;

    /*дебаг если не работает через абстрактный класс
    public JsonProductStorageStrategy() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(FoodProduct.class, new ProductTypeAdapter())
                .registerTypeAdapter(ElectronicsProduct.class, new ProductTypeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
    }*/
    public JsonProductStorageStrategy() {
        RuntimeTypeAdapterFactory<AbstractProduct> productFactory = RuntimeTypeAdapterFactory
                .of(AbstractProduct.class, "type")
                .registerSubtype(FoodProduct.class, "FoodProduct")
                .registerSubtype(ElectronicsProduct.class, "ElectronicsProduct");

        this.gson = new GsonBuilder()
                .registerTypeAdapterFactory(productFactory)
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
    }

    @Override
    public void save(List<AbstractProduct> products, String filePath) throws Exception {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(products, writer);
        }
    }

    @Override
    public List<AbstractProduct> load(String filePath) throws Exception {
        try (Reader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<ArrayList<AbstractProduct>>() {}.getType();
            return gson.fromJson(reader, listType);
        }
    }
}
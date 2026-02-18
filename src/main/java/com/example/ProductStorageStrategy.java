package com.example;

import java.util.List;

public interface ProductStorageStrategy {
    void save(List<AbstractProduct> products, String filePath) throws Exception;
    List<AbstractProduct> load(String filePath) throws Exception;
}
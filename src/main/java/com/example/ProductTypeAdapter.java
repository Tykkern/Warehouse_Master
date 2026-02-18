//Старый файл регистрации

/*package com.example;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;

public class ProductTypeAdapter implements JsonSerializer<AbstractProduct>, JsonDeserializer<AbstractProduct> {

    @Override
    public JsonElement serialize(AbstractProduct src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", src.getClass().getSimpleName());
        jsonObject.addProperty("id", src.getId());
        jsonObject.addProperty("name", src.getName());
        jsonObject.addProperty("price", src.getPrice());
        jsonObject.addProperty("quantity", src.getQuantity());

        if (src instanceof FoodProduct) {
            jsonObject.addProperty("expirationDate", ((FoodProduct) src).getExpirationDate().toString());
        } else if (src instanceof ElectronicsProduct) {
            jsonObject.addProperty("warrantyMonths", ((ElectronicsProduct) src).getWarrantyMonths());
        }

        return jsonObject;
    }

    @Override
    public AbstractProduct deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        JsonElement typeElement = jsonObject.get("type");
        if (typeElement == null) {
            throw new JsonParseException("Отсутствует обязательное поле 'type' в объекте продукта");
        }
        String type = typeElement.getAsString();

        JsonElement idElement = jsonObject.get("id");
        if (idElement == null) throw new JsonParseException("Отсутствует 'id'");
        int id = idElement.getAsInt();

        JsonElement nameElement = jsonObject.get("name");
        if (nameElement == null) throw new JsonParseException("Отсутствует 'name'");
        String name = nameElement.getAsString();

        JsonElement priceElement = jsonObject.get("price");
        if (priceElement == null) throw new JsonParseException("Отсутствует 'price'");
        double price = priceElement.getAsDouble();

        JsonElement quantityElement = jsonObject.get("quantity");
        if (quantityElement == null) throw new JsonParseException("Отсутствует 'quantity'");
        int quantity = quantityElement.getAsInt();

        if ("FoodProduct".equals(type)) {
            JsonElement expElement = jsonObject.get("expirationDate");
            if (expElement == null) throw new JsonParseException("Отсутствует 'expirationDate' для FoodProduct");
            LocalDate expirationDate = LocalDate.parse(expElement.getAsString());
            return new FoodProduct(id, name, price, quantity, expirationDate);
        } else if ("ElectronicsProduct".equals(type)) {
            JsonElement warrantyElement = jsonObject.get("warrantyMonths");
            if (warrantyElement == null)
                throw new JsonParseException("Отсутствует 'warrantyMonths' для ElectronicsProduct");
            int warrantyMonths = warrantyElement.getAsInt();
            return new ElectronicsProduct(id, name, price, quantity, warrantyMonths);
        }

        throw new JsonParseException("Неизвестный тип продукта: " + type);
    }
}*/
package com.example;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Adapts values whose runtime type may differ from their declared type. This
 * is primarily useful to serialize (but not deserialize) subclasses of a
 * declared type. Serialization requires the field be declared as the base type
 * in the containing class. Deserialization does not require a type adapter for
 * the declared type, only for the base type.
 *
 * <p>The following example shows serializing a tree where nodes can be either
 * a {@code String} or a {@code Integer}. The {@code Tree} type is abstract
 * and has two subclasses: {@code StringNode} and {@code IntegerNode}.
 *
 * <pre>{@code
 *   abstract class Tree {}
 *   class StringNode extends Tree { String value; }
 *   class IntegerNode extends Tree { int value; }
 * }</pre>
 *
 * <p>Serializing a tree with both string and integer nodes:
 *
 * <pre>{@code
 *   Tree tree = new StringNode();
 *   tree.value = "foo";
 *   gson.toJson(tree); // {"type":"StringNode","value":"foo"}
 * }</pre>
 *
 * <p>Deserializing a tree:
 *
 * <pre>{@code
 *   gson.fromJson(json, Tree.class); // returns StringNode or IntegerNode
 * }</pre>
 *
 * <p>The type field name defaults to "type" but can be overridden:
 *
 * <pre>{@code
 *   RuntimeTypeAdapterFactory<Tree> treeAdapterFactory
 *       = RuntimeTypeAdapterFactory.of(Tree.class)
 *           .registerSubtype(StringNode.class)
 *           .registerSubtype(IntegerNode.class)
 *           .withTypeFieldName("nodeType");
 * }</pre>
 */
public final class RuntimeTypeAdapterFactory<T> implements TypeAdapterFactory {
    private final Class<?> baseType;
    private final String typeFieldName;
    private final Map<String, Class<?>> labelToSubtype = new HashMap<>();
    private final Map<Class<?>, String> subtypeToLabel = new HashMap<>();

    private RuntimeTypeAdapterFactory(Class<?> baseType, String typeFieldName) {
        if (typeFieldName == null || baseType == null) {
            throw new NullPointerException();
        }
        this.baseType = baseType;
        this.typeFieldName = typeFieldName;
    }

    /**
     * Creates a new runtime type adapter factory for {@code baseType} using {@code
     * typeFieldName} as the type field name. Subtypes registered using {@link
     * #registerSubtype} will be serialized with a type field value equal to the
     * subtype's simple class name.
     */
    public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType, String typeFieldName) {
        return new RuntimeTypeAdapterFactory<>(baseType, typeFieldName);
    }

    /**
     * Creates a new runtime type adapter factory for {@code baseType} using {@code
     * "type"} as the type field name. Subtypes registered using {@link
     * #registerSubtype} will be serialized with a type field value equal to the
     * subtype's simple class name.
     */
    public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType) {
        return new RuntimeTypeAdapterFactory<>(baseType, "type");
    }

    /**
     * Registers {@code type} identified by {@code label}. Labels are case
     * sensitive.
     *
     * @throws IllegalArgumentException if either type or label have already been
     *     registered
     */
    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type, String label) {
        if (type == null || label == null) {
            throw new NullPointerException();
        }
        if (labelToSubtype.containsKey(label) || subtypeToLabel.containsKey(type)) {
            throw new IllegalArgumentException("types and labels must be unique");
        }
        labelToSubtype.put(label, type);
        subtypeToLabel.put(type, label);
        return this;
    }

    /**
     * Registers {@code type} identified by its {@link Class#getSimpleName()
     * simple class name}. Labels are case sensitive.
     *
     * @throws IllegalArgumentException if either type or its simple class name
     *     have already been registered
     */
    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type) {
        return registerSubtype(type, type.getSimpleName());
    }

    @Override
    public <R> com.google.gson.TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
        if (!baseType.isAssignableFrom(type.getRawType())) {
            return null;
        }

        final Map<String, com.google.gson.TypeAdapter<?>> labelToDelegate
                = new LinkedHashMap<>();
        final Map<Class<?>, com.google.gson.TypeAdapter<?>> subtypeToDelegate
                = new LinkedHashMap<>();

        for (Map.Entry<String, Class<?>> entry : labelToSubtype.entrySet()) {
            com.google.gson.TypeAdapter<?> delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.getValue()));
            labelToDelegate.put(entry.getKey(), delegate);
            subtypeToDelegate.put(entry.getValue(), delegate);
        }

        return new com.google.gson.TypeAdapter<R>() {
            @Override
            public R read(JsonReader in) throws IOException {
                JsonElement jsonElement = gson.fromJson(in, JsonElement.class);
                JsonElement labelJsonElement = jsonElement.getAsJsonObject().get(typeFieldName);
                if (labelJsonElement == null) {
                    throw new JsonParseException("cannot deserialize " + baseType
                            + " because it does not define a field named " + typeFieldName);
                }
                String label = labelJsonElement.getAsString();
                @SuppressWarnings("unchecked") // registration requires that subtype extends T
                com.google.gson.TypeAdapter<R> delegate = (com.google.gson.TypeAdapter<R>) labelToDelegate.get(label);
                if (delegate == null) {
                    throw new JsonParseException("cannot deserialize " + baseType + " subtype named "
                            + label + "; did not register subtype name: " + label);
                }
                return delegate.fromJsonTree(jsonElement);
            }

            @Override
            public void write(JsonWriter out, R value) throws IOException {
                Class<?> srcType = value.getClass();
                String label = subtypeToLabel.get(srcType);
                @SuppressWarnings("unchecked") // registration requires that subtype extends T
                com.google.gson.TypeAdapter<R> delegate = (com.google.gson.TypeAdapter<R>) subtypeToDelegate.get(srcType);
                if (delegate == null) {
                    throw new JsonParseException("cannot serialize " + srcType.getName()
                            + "; did not register subtype name: " + label);
                }
                JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();
                if (jsonObject.has(typeFieldName)) {
                    throw new JsonIOException("serialized object has a field named " + typeFieldName
                            + "; please rename it or use a different type field name");
                }
                JsonObject clone = new JsonObject();
                clone.add(typeFieldName, new JsonPrimitive(label));
                for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
                    clone.add(e.getKey(), e.getValue());
                }
                gson.toJson(clone, out);
            }
        }.nullSafe();
    }
}
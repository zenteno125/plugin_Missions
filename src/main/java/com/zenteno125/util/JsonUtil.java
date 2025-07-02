package com.zenteno125.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class JsonUtil {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static Gson gson() { return GSON; }
    private JsonUtil() {}

    /**
     * Serializa una lista de ItemStack a una cadena Base64.
     * @param items Lista de ItemStack a serializar
     * @return String con representación Base64 de los ítems
     */
    public static String itemStackListToBase64(List<ItemStack> items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Escribir tamaño de la lista
            dataOutput.writeInt(items.size());

            // Escribir cada ítem, saltando los nulls
            for (ItemStack item : items) {
                if (item != null) {
                    dataOutput.writeObject(item);
                } else {
                    dataOutput.writeObject(null);
                }
            }

            // Cerrar streams
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Deserializa una cadena Base64 a una lista de ItemStack.
     * @param base64 String en formato Base64 con los ítems serializados
     * @return Lista de ItemStack deserializada
     */
    public static List<ItemStack> itemStackListFromBase64(String base64) {
        try {
            if (base64 == null || base64.isEmpty()) return new ArrayList<>();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            List<ItemStack> items = new ArrayList<>();

            // Leer tamaño de la lista
            int size = dataInput.readInt();

            // Leer cada ítem
            for (int i = 0; i < size; i++) {
                items.add((ItemStack) dataInput.readObject());
            }

            // Cerrar streams
            dataInput.close();
            return items;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}


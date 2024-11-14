package com.backend.crmInmobiliario.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonPrinter {

    public static String toString(Object t){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateAdapter.class, new LocalDateAdapter());
        Gson gson = gsonBuilder.setPrettyPrinting().create();

        return gson.toJson(t).trim().replace("\n", "").replace("\t","");
    }
}

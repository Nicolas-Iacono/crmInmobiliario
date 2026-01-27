//package com.backend.crmInmobiliario.loader;
//
//import com.backend.crmInmobiliario.service.impl.IA.CodigoCivilManager;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class CodigoCivilLoader implements CommandLineRunner {
//
//    private final CodigoCivilManager manager;
//
//    public CodigoCivilLoader(CodigoCivilManager manager) {
//        this.manager = manager;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        // ⚠️ Ruta al PDF del Código Civil y Comercial Argentino
//        String rutaPdf = "C:\\Users\\nico_\\OneDrive\\Escritorio\\Codigo_Civil_y_Comercial_de_la_Nacion.pdf";
//
//
//        System.out.println("🧠 Iniciando carga del Código Civil desde: " + rutaPdf);
//
//        manager.procesarYSubir(rutaPdf);
//
//        System.out.println("✅ Carga del Código Civil completada con éxito");
//    }
//}

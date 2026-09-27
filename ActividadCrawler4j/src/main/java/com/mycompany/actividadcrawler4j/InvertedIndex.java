
package com.mycompany.actividadcrawler4j;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class InvertedIndex {

    private static final InvertedIndex INSTANCIA =
            new InvertedIndex();

    private final Map<String, Set<Integer>> indice =
            new TreeMap<>();

    private final Map<Integer, Documento> documentos =
            new LinkedHashMap<>();

    private final Set<String> urlsRegistradas =
            new HashSet<>();

    private static final Set<String> STOP_WORDS =
            new HashSet<>(Arrays.asList(
                "a", "an", "the", "and", "or", "but",
                "for", "to", "of", "in", "on", "at",
                "by", "with", "from", "is", "are",
                "was", "were", "be", "been", "being",
                "it", "its", "this", "that", "these",
                "those", "as", "not", "can", "will",
                "you", "your", "we", "our", "they",
                "their", "he", "she", "his", "her",
                "have", "has", "had", "do", "does",
                "did", "into", "about", "all", "any",
                "more", "than", "then", "there",
                "here", "which", "who", "what",
                "when", "where", "how", "also",
                "up", "out", "if", "so", "no", "yes"
            ));

    public static InvertedIndex getInstancia() {
        return INSTANCIA;
    }

    public synchronized int agregarDocumento(
            String url, String texto) {

        if (urlsRegistradas.contains(url)) {
            return -1;
        }

        urlsRegistradas.add(url);

        int id = documentos.size() + 1;

        Map<String, Integer> frecuencias =
                new HashMap<>();

        String[] palabras = texto
                .toLowerCase(Locale.ROOT)
                .split("[^\\p{L}\\p{N}]+");
        
            for (String palabra : palabras) {

                boolean numeroCorto =
                        palabra.matches("\\d{1,2}");

                boolean identificadorHexadecimal =
                        palabra.matches(
                                "(?=.*[a-f])(?=.*\\d)[0-9a-f]{16,}"
                        );

                if (palabra.length() < 2
                        || STOP_WORDS.contains(palabra)
                        || numeroCorto
                        || identificadorHexadecimal) {

                    continue;
                }

                frecuencias.merge(palabra, 1, Integer::sum);
            }

        Documento documento = new Documento();

        documento.id = id;
        documento.url = url;
        documento.frecuencias = frecuencias;

        documentos.put(id, documento);

        for (String palabra : frecuencias.keySet()) {

            indice.computeIfAbsent(
                    palabra, k -> new TreeSet<>()
            ).add(id);
        }

        return id;
    }

    public synchronized int totalDocumentos() {
        return documentos.size();
    }

    public synchronized int totalTerminos() {
        return indice.size();
    }

    public synchronized void guardarEn(Path carpeta)
            throws IOException {

        Files.createDirectories(carpeta);

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        Path archivoIndice =
                carpeta.resolve("indice_invertido.json");

        Path archivoDocumentos =
                carpeta.resolve("documentos.json");

        try (Writer writer = Files.newBufferedWriter(
                archivoIndice, StandardCharsets.UTF_8)) {

            gson.toJson(indice, writer);
        }

        try (Writer writer = Files.newBufferedWriter(
                archivoDocumentos, StandardCharsets.UTF_8)) {

            gson.toJson(documentos, writer);
        }

        System.out.println("Archivos JSON guardados:");
        System.out.println(archivoIndice.toAbsolutePath());
        System.out.println(archivoDocumentos.toAbsolutePath());
    }

    public static class Documento {

        public int id;
        public String url;

        public Map<String, Integer> frecuencias =
                new HashMap<>();
    }
}

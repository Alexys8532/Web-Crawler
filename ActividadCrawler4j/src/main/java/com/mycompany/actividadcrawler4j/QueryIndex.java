package com.mycompany.actividadcrawler4j;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class QueryIndex {

    private static class Documento {
        int id;
        String url;
        Map<String, Integer> frecuencias;
    }

    private static class Resultado {
        Documento documento;
        int coincidencias;
        int frecuenciaTotal;

        Resultado(Documento documento) {
            this.documento = documento;
        }
    }

    public static void main(String[] args) throws Exception {

        Gson gson = new Gson();

        Path carpeta = Path.of("resultados");

        Path archivoIndice =
                carpeta.resolve("inverted_index.json");

        if (!Files.exists(archivoIndice)) {
            archivoIndice =
                    carpeta.resolve("indice_invertido.json");
        }

        Path archivoDocumentos =
                carpeta.resolve("documentos.json");

        if (!Files.exists(archivoIndice)
                || !Files.exists(archivoDocumentos)) {

            System.out.println(
                    "Error: No se encontraron los archivos JSON."
            );

            return;
        }

        Map<String, List<Integer>> indice;

        Map<String, Documento> documentos;

        try (Reader reader = Files.newBufferedReader(
                archivoIndice, StandardCharsets.UTF_8)) {

            indice = gson.fromJson(
                    reader,
                    new TypeToken<Map<String, List<Integer>>>() {}.getType()
            );
        }

        try (Reader reader = Files.newBufferedReader(
                archivoDocumentos, StandardCharsets.UTF_8)) {

            documentos = gson.fromJson(
                    reader,
                    new TypeToken<Map<String, Documento>>() {}.getType()
            );
        }

        Scanner scanner = new Scanner(System.in);

        System.out.println("=================================");
        System.out.println("       INVERTED INDEX SEARCH");
        System.out.println("=================================");

        System.out.println(
                "Documentos disponibles: " + documentos.size()
        );

        System.out.println(
                "Terminos disponibles: " + indice.size()
        );

        while (true) {

            System.out.print("\nEnter search query (exit to quit): ");

            String consulta = scanner.nextLine()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            if (consulta.equals("exit")) {
                break;
            }

            List<String> terminos = Arrays.stream(
                    consulta.split("[^\\p{L}\\p{N}]+")
            )
                    .filter(p -> !p.isEmpty())
                    .filter(indice::containsKey)
                    .distinct()
                    .toList();

            if (terminos.isEmpty()) {

                System.out.println(
                        "No se encontraron terminos en el indice."
                );

                continue;
            }

            Map<Integer, Resultado> resultados =
                    new HashMap<>();

            for (String termino : terminos) {

                List<Integer> ids = indice.get(termino);

                for (Integer id : ids) {

                    Documento doc =
                            documentos.get(String.valueOf(id));

                    if (doc == null) {
                        continue;
                    }

                    Resultado resultado =
                            resultados.computeIfAbsent(
                                    id,
                                    k -> new Resultado(doc)
                            );

                    resultado.coincidencias++;

                    resultado.frecuenciaTotal +=
                            doc.frecuencias.getOrDefault(
                                    termino, 0
                            );
                }
            }

            List<Resultado> ordenados =
                    new ArrayList<>(resultados.values());

            ordenados.sort(
                    Comparator
                            .comparingInt(
                                    (Resultado r) -> r.coincidencias
                            )
                            .reversed()
                            .thenComparing(
                                    Comparator.comparingInt(
                                            (Resultado r) -> r.frecuenciaTotal
                                    ).reversed()
                            )
            );

            System.out.println("\nResults found: "
                    + ordenados.size());

            System.out.println("------------------------------");

            for (Resultado resultado : ordenados) {

                System.out.println(
                        "Document ID: "
                        + resultado.documento.id
                );

                System.out.println(
                        "URL: "
                        + resultado.documento.url
                );

                System.out.println(
                        "Matching terms: "
                        + resultado.coincidencias
                );

                System.out.println(
                        "Term frequency: "
                        + resultado.frecuenciaTotal
                );

                System.out.println("------------------------------");
            }
        }

        System.out.println("Search finished.");
    }
}
package com.iablonski;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Test {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("java -jar {название проекта}.jar тестовый-файл.txt");
            return;
        }

        String inputFilePath = args[0];
        long startTime = System.currentTimeMillis();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             PrintWriter writer = new PrintWriter("output-lng.txt")) {
            // Хранит строки в Set (каждый представляет из себя группу с совпадениями)
            List<Set<String>> groupsOfStrings = new ArrayList<>();
            // Хранит каждое значение колонки с номером группы (Map -> k=значение, v=номер группы)
            List<Map<String, Integer>> columnValuesAndGroupNumbers = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                // Разбираем строку на столбцы
                String[] columnValues = extractColumns(line);
                // Ищем номер группы для строки
                Integer groupNumber = findGroupNumber(columnValues, columnValuesAndGroupNumbers, groupsOfStrings);

                if (groupNumber == null) {
                    // Создаем новую группу, если строка не принадлежит ни к одной из существующих групп
                    if (containsNonEmptyValues(columnValues)) {
                        groupsOfStrings.add(new HashSet<>(List.of(line)));
                        updateColumnValuesAndGroupNumbers(columnValues, groupsOfStrings.size() - 1, columnValuesAndGroupNumbers);
                    }
                } else {
                    groupsOfStrings.get(groupNumber).add(line);
                    // Добавляем строку в существующую группу и обновляем информацию в Map
                    updateColumnValuesAndGroupNumbers(columnValues, groupNumber, columnValuesAndGroupNumbers);
                }
            }

            sortAndWriteToFile(writer, groupsOfStrings);

        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        double executionTimeInSeconds = executionTime / 1000.0;

        System.out.println("Execution Time: " + executionTimeInSeconds + " seconds");
    }

    private static Integer findGroupNumber(
            String[] columnValues,
            List<Map<String, Integer>> columnValuesAndGroupNumbers,
            List<Set<String>> groupsOfStrings) {

        Integer groupNumber = null;
        int size = Math.min(columnValuesAndGroupNumbers.size(), columnValues.length);

        for (int i = 0; i < size; i++) {
            if (!columnValues[i].isEmpty()) {
                Integer groupNumber2 = columnValuesAndGroupNumbers.get(i).get(columnValues[i]);

                if (groupNumber2 != null) {
                    if (groupNumber == null) {
                        groupNumber = groupNumber2;
                    } else if (!groupNumber.equals(groupNumber2)) {
                        mergeGroups(groupsOfStrings, columnValuesAndGroupNumbers, groupNumber, groupNumber2);
                    }
                }
            }
        }
        return groupNumber;
    }

    private static void mergeGroups(List<Set<String>> groupsOfStrings,
                                    List<Map<String, Integer>> columnValuesAndGroupNumbers,
                                    int groupNumber,
                                    int groupNumber2) {

        Set<String> group = groupsOfStrings.get(groupNumber);
        Set<String> group1 = groupsOfStrings.get(groupNumber2);

        group.addAll(group1);

        group1.stream()
                .map(Test::extractColumns)
                .forEach(columns -> updateColumnValuesAndGroupNumbers(columns, groupNumber, columnValuesAndGroupNumbers));

        group1.clear();
        groupsOfStrings.set(groupNumber2, new HashSet<>());
    }

    private static void updateColumnValuesAndGroupNumbers(String[] columnValues,
                                                          int groupNumber,
                                                          List<Map<String, Integer>> columnValuesAndGroupNumbers) {
        for (int i = 0; i < columnValues.length; i++) {
            Map<String, Integer> map;
            // Если существует колонка (Map)
            if (i < columnValuesAndGroupNumbers.size()) {
                map = columnValuesAndGroupNumbers.get(i);
                // Если нет, создаем новую колонку (Map)
            } else {
                map = new HashMap<>();
                if (!columnValues[i].isEmpty()) columnValuesAndGroupNumbers.add(map);
                else {
                    map.put("-", groupNumber);
                    columnValuesAndGroupNumbers.add(map);
                    continue;
                }
            }
            // Добавляем значения в нужную колонку
            if (!columnValues[i].isEmpty()) map.put(columnValues[i], groupNumber);
            else map.put("-", groupNumber);
        }
    }

    private static void sortAndWriteToFile(PrintWriter writer, List<Set<String>> groupsOfStrings) {
        long nonEmptyGroupsCount = groupsOfStrings.stream()
                .filter(group -> group.size() > 1)
                .count();
        writer.println("Number of groups with more than one element: " + nonEmptyGroupsCount);
        groupsOfStrings.sort(Comparator.comparingInt(set -> -set.size()));
        int i = 0;
        for (Set<String> group : groupsOfStrings) {
            i++;
            if (group.isEmpty()) break;
            writer.println("\nGroup " + i);
            group.forEach(writer::println);
        }
    }

    private static String[] extractColumns(String line) {
        if(line.isEmpty()) return new String[0];
        StringBuilder result = new StringBuilder(line.length());

        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ';' && !inQuotes) {
                result.append("\"\";");
                continue;
            }
            result.append(c);
        }

        if (result.charAt(0) == ';') {
            result.insert(0, "\"\"");
        }
        if (result.charAt(result.length() - 1) == ';') {
            result.append("\"\"");
        }

        return result.toString().replaceAll("\"", "").split(";");
    }

    private static boolean containsNonEmptyValues(String[] array) {
        return Arrays.stream(array).anyMatch(s -> !s.isEmpty());
    }
}
import java.io.*;
import java.lang.UnsupportedOperationException;
import java.util.ArrayList;

public class problem_3 {
    public static void main(String[] args) throws IOException {
        long startTime = System.nanoTime();
        ArrayList<Integer> numbers = readListFromJsonFile("inputs/random_integers.json");
        numbers = quickSort(numbers);
        long stopTime = System.nanoTime();
        System.out.println((stopTime - startTime)/1000000.0 + " ms");
    }

    private static ArrayList<Integer> readListFromJsonFile(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = reader.readLine();
        line = line.replaceAll("\\[", "");
        line = line.replaceAll("\\]", "");
        String[] stringList = line.split(", ");
        ArrayList<Integer> result = new ArrayList<>();
        for (String s : stringList) {
            try {
                result.add(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return result;
    }

    private static ArrayList<Integer> quickSort(ArrayList<Integer> list) {
        if (list.size() <= 1) {
            return list;
        }
        int pivot = list.get(list.size() / 2);
        ArrayList<Integer> less = new ArrayList<>();
        ArrayList<Integer> equal = new ArrayList<>();
        ArrayList<Integer> greater = new ArrayList<>();
        for (int i : list) {
            if (i < pivot) {
                less.add(i);
            } else if (i > pivot) {
                greater.add(i);
            } else {
                equal.add(i);
            }
        }
        ArrayList<Integer> result = new ArrayList<>();
        result.addAll(quickSort(less));
        result.addAll(equal);
        result.addAll(quickSort(greater));
        return result;
    }

    private static ArrayList<Integer> parallelSort(ArrayList<Integer> list) {
        throw new UnsupportedOperationException();
    }
}
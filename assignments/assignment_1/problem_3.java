import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import java.util.Comparator;

public class problem_3 {

    public static void main(String[] args) throws IOException {
        for (int i = 3; i <= 7; i++) {
            System.out.println("Instance size 10^" + i + "\n");
            for (int j = 1; j <= 5; j++) {
                ArrayList<Long> numbers = readListFromJsonFile("assignments/assignment_1/inputs/random_integers_"+ i + "_"+ j + ".json");
                getTimeQuicksort(numbers);
                getTimeParallelSort(numbers);
            }
            System.out.println();
        }
    }

    private static void getTimeParallelSort(ArrayList<Long> numbers) {
        long startTime = System.nanoTime();
        numbers = parallelSort(numbers);
        long stopTime = System.nanoTime();
        System.out.println("Parallel quicksort: "+ (stopTime - startTime)/1000000.0 + " ms");
    }

    private static void getTimeQuicksort(ArrayList<Long> numbers) {
        long startTime = System.nanoTime();
        numbers.sort(new LongComparator());
        long stopTime = System.nanoTime();
        System.out.print("Quicksort: "+ (stopTime - startTime)/1000000.0 + " ms,  ");
    }

    private static ArrayList<Long> readListFromJsonFile(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = reader.readLine();
        line = line.replaceAll("\\[", "");
        line = line.replaceAll("\\]", "");
        String[] stringList = line.split(", ");
        ArrayList<Long> result = new ArrayList<>();
        for (String s : stringList) {
            try {
                result.add(Long.parseLong(s));
            } catch (NumberFormatException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        reader.close();
        return result;
    }

    private static ArrayList<Long> quickSort(ArrayList<Long> list) {
        if (list.size() <= 1) {
            return list;
        }
        long pivot = list.get(list.size() / 2);
        ArrayList<Long> less = new ArrayList<>();
        ArrayList<Long> equal = new ArrayList<>();
        ArrayList<Long> greater = new ArrayList<>();
        for (long i : list) {
            if (i < pivot) {
                less.add(i);
            } else if (i > pivot) {
                greater.add(i);
            } else {
                equal.add(i);
            }
        }
        ArrayList<Long> result = new ArrayList<>();
        result.addAll(quickSort(less));
        result.addAll(equal);
        result.addAll(quickSort(greater));
        return result;
    }

    private static ArrayList<Long> parallelSort(ArrayList<Long> list) {
        ForkJoinPool pool = new ForkJoinPool();
        ArrayList<Long> result = pool.invoke(new QuicksortTask(list));
        pool.shutdown();
        return result;
    }
}

class QuicksortTask extends RecursiveTask<ArrayList<Long>> {
    private final ArrayList<Long> list;

    public QuicksortTask(ArrayList<Long> list) {
        this.list = list;
    }

    @Override
    protected ArrayList<Long> compute() {
        if (list.size() <= 1) {
            return list;
        }

        long pivot = list.get(list.size() / 2);
        ArrayList<Long> less = new ArrayList<>();
        ArrayList<Long> equal = new ArrayList<>();
        ArrayList<Long> greater = new ArrayList<>();

        for (long i : list) {
            if (i < pivot) {
                less.add(i);
            } else if (i > pivot) {
                greater.add(i);
            } else {
                equal.add(i);
            }
        }

        QuicksortTask leftTask = new QuicksortTask(less);
        QuicksortTask rightTask = new QuicksortTask(greater);

        leftTask.fork();
        ArrayList<Long> rightResult = rightTask.compute();
        ArrayList<Long> leftResult = leftTask.join();

        ArrayList<Long> result = new ArrayList<>(leftResult.size() + equal.size() + rightResult.size());
        result.addAll(leftResult);
        result.addAll(equal);
        result.addAll(rightResult);

        return result;
    }
}

class LongComparator implements Comparator<Long> {
    @Override
    public int compare(Long a, Long b) {
        return a.compareTo(b);
    }
}

package main;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class exclusaomutua {

    private static final int NUM_PROCESSES = 5;
    private static final int QUORUM = NUM_PROCESSES / 2 + 1;
    private static final String FILENAME = "critical_region.txt";

    private static int[] clocks = new int[NUM_PROCESSES];
    private static boolean[] waiting = new boolean[NUM_PROCESSES];
    private static List<Integer> requestQueue = new ArrayList<>();

    public static void main(String[] args) {
        for (int i = 0; i < NUM_PROCESSES; i++) {
            clocks[i] = 0;
            waiting[i] = false;
        }

        
        for (int i = 0; i < 10; i++) {
            int processId = (int) (Math.random() * NUM_PROCESSES);
            requestCriticalSection(processId);
        }
    }

    private static synchronized void requestCriticalSection(int processId) {
        clocks[processId]++;
        requestQueue.add(processId);

       
        Collections.sort(requestQueue, (p1, p2) -> {
            if (clocks[p1] < clocks[p2]) {
                return -1;
            } else if (clocks[p1] > clocks[p2]) {
                return 1;
            } else {
                return Integer.compare(p1, p2);
            }
        });

        if (requestQueue.indexOf(processId) < QUORUM && !waiting[processId]) {
            waiting[processId] = true;

            try (FileWriter writer = new FileWriter(FILENAME, true)) {
                writer.write("Process " + processId + " entered critical region at " + System.currentTimeMillis() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

            
            try {
                Thread.sleep((int) (Math.random() * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            waiting[processId] = false;

            try (FileWriter writer = new FileWriter(FILENAME, true)) {
                writer.write("Process " + processId + " exited critical region at " + System.currentTimeMillis() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

            requestQueue.remove(Integer.valueOf(processId));

            
            for (int i = 0; i < NUM_PROCESSES; i++) {
                if (requestQueue.indexOf(i) < QUORUM && waiting[i]) {
                    synchronized (exclusaomutua.class) {
                        exclusaomutua.class.notify();
                    }
                }
            }
        } else {
            while (requestQueue.indexOf(processId) >= QUORUM || waiting[processId]) {
                try {
                    synchronized (exclusaomutua.class) {
                        exclusaomutua.class.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            requestCriticalSection(processId);
        }
    }
}

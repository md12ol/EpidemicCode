import static java.lang.Thread.sleep;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Michael Dubé // md12ol@brocku.ca // 25 Feb, 2019
 *
 * This class ...
 */
public class RunMany {

    private static final String OUTPUT_FOLDER = "./Output/";
    public static final int NUM_COMMANDS = 9;

    private static final int NUM_CORES = 9;

    /**
     * @param args vaccine strategy, vaccine delay, fitness function
     */
    public static void main(String[] args) {
        BufferedReader reader;
        LinkedList<LinkedList<Double>> PS = new LinkedList<>();

        String outPath;

        LinkedList<Double> densities = new LinkedList<>();
        for (int i = 0; i < 0 + NUM_COMMANDS; i++) {
            densities.add(Double.parseDouble(args[i]));
        }

        int PSnum = 1;
        int running = 0;
        GET[] workers = new GET[NUM_CORES];
        ArrayList<Integer> lazy = new ArrayList<>(NUM_CORES);
        // Set all to null and add to lazy
        for (int i = 0; i < NUM_CORES; i++) {
            workers[i] = null;
            lazy.add(i);
        }

        for (int prof = 1; prof < NUM_CORES+ 1; prof++) {
            outPath = OUTPUT_FOLDER + "P" + prof + "/PS" + (PSnum) + "/";
            GET w = new GET(prof, densities, outPath);
            workers[lazy.remove(0)] = w;
//            w.start();
            running++;
            PSnum++;
        }

        // End workers
//        for (GET w : workers) {
//            if (w != null) {
//                try {
////                    w.join();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    e.getMessage();
//                }
//            }
//        }
        System.out.println("ALL DONE w " + Thread.activeCount() + " threads");
    }
}



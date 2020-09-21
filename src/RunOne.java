import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Michael Dubé // md12ol@brocku.ca // 06 Feb, 2019
 *
 * This class ...
 */
public class RunOne {

    private static final String OUTPUT_FOLDER = "Output/";

    private static final int NUM_COMMANDS = 9;          // Number of commands

    public static final int NUM_PROFILES = 9;

    public static final int NUM_CORES = 1;

    /**
     * @param args vaccine strategy, vaccine delay, fitness function, PSnum, PS
     */
    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        new DirTools(8, 9);

//        System.out.println(freeMemory + "/" + maxMemory + "\t[" + allocatedMemory + "]");

        int PSnum = Integer.parseInt(args[0]);
        String outPath;
        LinkedList<Double> densities = new LinkedList<>();
        for (int i = 1; i < 1 + NUM_COMMANDS; i++) {
            densities.add(Double.parseDouble(args[i]));
        }

        int running = 0;
        GET[] workers = new GET[NUM_CORES];
        ArrayList<Integer> lazy = new ArrayList<>(NUM_CORES);

        // Set all to null and add to lazy
        for (int i = 0; i < NUM_CORES; i++) {
            workers[i] = null;
            lazy.add(i);
        }

        for (int prof = 1; prof < NUM_CORES + 1; prof++) {
            outPath = OUTPUT_FOLDER + "PS" + (PSnum) + "/P" + (prof) + "/";
            GET w = new GET(prof, densities, outPath);
            workers[lazy.remove(0)] = w;
            w.start();
            running++;
        }

//        System.out.println("ALL STARTED w " + Thread.activeCount() + " threads");

        for (GET w : workers) {
            try {
                w.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

//        System.out.println("ALL DONE w " + Thread.activeCount() + " threads");
    }

} // RunOne
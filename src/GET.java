import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

class GET extends Thread{

    // Constant variables across runs/profiles
    private static final boolean VERBOSE = true;              // Print running output
    private static final double ALPHA = 0.5;                  // Probability of Epidemic Spread
    private static final int PROFILE_LENGTH = 16;             // Profile Length
    private static final int NUM_EPIDEMICS = 50;              // Number of Sampled Epidemics
    private static final int SHORT_EPIDEMIC = 3;              // Meaningful Epidemic Length
    private static final int SHORT_RETRIES = 5;               // Retry Short Epidemics
    private static final int FINAL_TEST_LEN = 50;             // Final Test Length
    private static final int RUNS = 30;                       // Number of Runs
    private static final int MATING_EVENTS = 40000;          // Number of Mating Events
    private static final int REPORTING_INTERVAL = 400;       // Reporting Interval
    private static final int NUM_COMMANDS = 9;                // Number of commands
    private static final int MIN_DEG_SWAP = 2;                // Minimum degree for swap
    private static final int POPSIZE = 1000;                  // Number of chromosomes
    private static final int VERTS = 128;                     // Number of nodes
    private static final int GENE_LENGTH = 256;               // Gene length
    private static final int NODES_CUBED = 2097152;           // Nodes cubed
    private static final int MAX_MUTATIONS = 3;               // Maximum number of mutations
    private static final int TOURN_SIZE = 7;                  // Tournament Size
    private static final long SEED = 91207819;                // Random Number Seed

    // Non-constant variables
    private String outPath;                                   // Output Location
    private String proFilePath;                               // Profile file profile
    private ArrayList<Double> cmdDensity;                              // Command Densities
    private double[] fitness;                                 // Fitness array
    private ArrayList<ArrayList<Integer>> pop;                // Population of command strings
    private int[] sortIndex;                                  // Sorting index
    private int[] profile;                                    // Epidemic profile
    private Random rand;                                      // Random numbers

    /**
     * Runs one iteration of the Graph Evolution Tool with the provided profile number, output
     * folder and command densities.  Uses the profile matching fitnessPM.
     *
     * @param profileNum Epidemic profile number (1-9)
     * @param outputPath Output location
     * @param densities  Command densities
     */
    GET(int profileNum, LinkedList<Double> densities, String outputPath) {

        profile = new int[VERTS];
        fitness = new double[POPSIZE];
        sortIndex = new int[POPSIZE];
        pop = new ArrayList<>(POPSIZE);
        cmdDensity = new ArrayList<>(densities);
        outPath = outputPath;

        if (VERBOSE) {
            System.out.println("CORE");
            System.out.println("\tProfile Number: " + profileNum);
            System.out.println("\tOutput Location: " + outPath);
            System.out.print("\tCommand Densities : ");
            for (int i = 0; i < NUM_COMMANDS; i++) {
                System.out.print(densities.get(i) + " ");
            }
            System.out.println();
        }

        // Grab information from command line arguments

        proFilePath = "./Profiles/Profile" + profileNum + ".dat"; // Profile name

        initalg();
    } // constructor

    public void run() {
        for (int run = 0; run < RUNS; run++) {
            StringBuilder runOutput = new StringBuilder();

            initpop();
            runOutput.append(report());

            for (int mev = 0; mev < MATING_EVENTS; mev++) {
                matingEvent();
                if ((mev + 1) % REPORTING_INTERVAL == 0) {
                    if (VERBOSE) {
                        System.out.print(run + " " + (mev + 1) / REPORTING_INTERVAL + " ");
                    }
                    runOutput.append(report());
                }
            }
            printToFile(outPath + "run" + run + ".dat", runOutput.toString(), false);
            reportBest(run);
        }
    }

    // Facilitates one mating event
    private void matingEvent() {

        int cp1, cp2; // Crossover Points
        int tmp; // For swapping

        // Perform tournament selection
        tselect(); // places first 7 peeps as the tourn (best at end)
        cp1 = abs(rand.nextInt() % GENE_LENGTH);
        cp2 = abs(rand.nextInt() % GENE_LENGTH);
        if (cp1 > cp2) {
            tmp = cp1;
            cp1 = cp2;
            cp2 = tmp;
        }

        // Crossover
        ArrayList<Integer> parent1 = pop.get(sortIndex[TOURN_SIZE - 2]);
        ArrayList<Integer> parent2 = pop.get(sortIndex[TOURN_SIZE - 1]);

        ArrayList<Integer> child1 = new ArrayList<>(parent1.subList(0, cp1));
        ArrayList<Integer> child2 = new ArrayList<>(parent2.subList(0, cp1));

        child1.addAll(parent2.subList(cp1, cp2));
        child2.addAll(parent1.subList(cp1, cp2));

        child1.addAll(parent1.subList(cp2, GENE_LENGTH));
        child2.addAll(parent2.subList(cp2, GENE_LENGTH));

        // Skeptical tourn selection for ED fitness
        //        try {
        //            fitness[sortIndex[TOURN_SIZE - 1]]
        //                = fitness(pop.get(sortIndex[TOURN_SIZE - 1]), true);
        //            fitness[sortIndex[TOURN_SIZE - 2]]
        //                = fitness(pop.get(sortIndex[TOURN_SIZE - 2]), true);
        //        } catch (NoSuchMethodException e) {
        //            System.out.println(e.getMessage());
        //        }

        // Mutation
        int r; // Location of mutation
        int muts = abs(rand.nextInt() % (MAX_MUTATIONS + 1)); // Number of Mutations
        for (int i = 0; i < muts; i++) {
            r = abs(rand.nextInt() % GENE_LENGTH);
            child1.set(r, validLoci());
        }
        muts = abs(rand.nextInt() % (MAX_MUTATIONS + 1));
        for (int i = 0; i < muts; i++) {
            r = abs(rand.nextInt() % GENE_LENGTH);
            child2.set(r, validLoci());
        }

        // Update Fitness

        fitness[sortIndex[0]] = fitness(child1);
        fitness[sortIndex[1]] = fitness(child2);

        pop.set(sortIndex[0], child1);
        pop.set(sortIndex[1], child2);
    } // matingEvent

    // Reports data from run
    private String report() {
        Dataset data = new Dataset();
        data.add(fitness);
        String newData;
        newData = data.getReport();
        if (VERBOSE) {
            System.out.print(newData);
        }
        return newData;
    }

    // Initialize Population
    private void initpop() {
        pop = new ArrayList<>(POPSIZE);
        for (int p = 0; p < POPSIZE; p++) {
            pop.add(new ArrayList<>(GENE_LENGTH));
        }

        for (int i = 0; i < POPSIZE; i++) {
            for (int j = 0; j < GENE_LENGTH; j++) {
                pop.get(i).add(validLoci());
            }
            fitness[i] = fitness(pop.get(i));

            sortIndex[i] = i; // Initialize order
        }
    }

    // This generates a valid loci for the expression routine
    private int validLoci() {
        int cmd;
        double dart;

        dart = rand.nextDouble() - cmdDensity.get(0); // Throw the dart
        cmd = 0;
        while (dart > 0 && (cmd < NUM_COMMANDS - 1)) {
            dart -= cmdDensity.get(++cmd);
        }
        cmd += NUM_COMMANDS * abs(rand.nextLong() % NODES_CUBED);
        return (cmd);
    }

    // Initialize the algorithm by seeding the random number generator and reading profile
    private void initalg() {
        rand = new Random(SEED);
        try {
            Scanner in = new Scanner(new FileReader(proFilePath));
            for (int i = 0; i < VERTS; i++) {
                profile[i] = 0; // Clear profile
            }
            profile[0] = 1; // Patient zero
            for (int i = 1; i <= PROFILE_LENGTH; i++) {
                profile[i] = in.nextInt();
            }

            // Print profile
            if (VERBOSE) {
                System.out.print("\tProfile: ");

                for (int i = 0; i < PROFILE_LENGTH; i++) {
                    System.out.print(profile[i] + " ");
                }
                System.out.println();

            }
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: File '" + proFilePath + "' Not Found");
            e.printStackTrace();
        }

    }

    // Prints toWrite to file at path.  Overwriting, if path file already exists.
    private void printToFile(String path, String toWrite, Boolean append) {
        FileWriter write;
        try {
            write = new FileWriter(path, append);
            PrintWriter print = new PrintWriter(write);
            print.write(toWrite);
            print.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double fitness(ArrayList<Integer> cmd) {
        return fitnessPM(cmd);
    }

    private double fitnessPM(ArrayList<Integer> cmd) {
        Graph G; // Graph
        LinkedList<Double> trials; // Stores squared error for each trial
        trials = new LinkedList<>();
        double delta; // Difference between profile and trial
        double accu; // Accumulator
        double error;

        G = new Graph();
        express(G, cmd);
        for (int e = 0; e < NUM_EPIDEMICS; e++) {
            // Epidemic Profile
            double[] rsltProfile = G.SIRProfile(ALPHA);
            error = 0.0; // Zero squared error
            if (G.getLen() < PROFILE_LENGTH + 1) {
                G.setLen(PROFILE_LENGTH + 1);
            }
            for (int i = 0; i < G.getLen(); i++) {
                delta = rsltProfile[i] - profile[i];
                error += delta * delta;
            }
            trials.add(sqrt(error / G.getLen()));
        }

        Collections.sort(trials); // Ascending order
        accu = 0.0;
        for (int i = 0; i < NUM_EPIDEMICS; i++) {
            accu += trials.get(i) / (i + 1);
        }
        return accu;
    } // fitnessPM

    private void express(Graph G, ArrayList<Integer> cmd) {
        int a, b, c; // Decoded values
        int cdv; // Command value
        int block; // Integer carving block

        G.RNGnm(VERTS, 2);

        for (int i : cmd) {
            block = i;
            cdv = block % NUM_COMMANDS;
            block /= NUM_COMMANDS;
            a = block % VERTS;
            b = (block / VERTS) % VERTS;
            c = (block / VERTS / VERTS) % VERTS;
            switch (cdv) {
                case 0: // Toggle
                    G.toggle(a, b);
                    break;
                case 1: // Hop
                    G.hop(a, b, c);
                    break;
                case 2: // Add
                    G.add(a, b);
                    break;
                case 3: // Delete
                    G.del(a, b);
                    break;
                case 4: // Swap
                    a = block % (VERTS * 10);
                    b = (block / (VERTS * 10)) % (VERTS * 10);
                    G.swap(a, b, MIN_DEG_SWAP);
                    break;
                case 5: // Local Toggle
                    G.ltog(a, b, c);
                    break;
                case 6: // Local Add
                    G.ladd(a, b, c);
                    break;
                case 7: // Local Delete
                    G.ldel(a, b, c);
                    break;
                case 8: // Null
                    break;
            }
        }
        G.setCreated();
    }

    // Randomly selects num entries, places them at front of sortIndex and then sorts first num
    // Entries of arr in decreasing order (highest first)
    private void tselect() {
        int r; // Random value
        Number tmp; // For swapping values

        // Select tournament members
        for (int i = 0; i < TOURN_SIZE; i++) {
            r = abs(rand.nextInt() % POPSIZE);
            tmp = sortIndex[i];
            sortIndex[i] = sortIndex[r];
            sortIndex[r] = (int) tmp;
        }

        boolean notSorted; // Keeps track of whether or not there is potential for more sorting

        do {
            notSorted = false;
            // Ensure decreasing fitnessPM (lower better)
            for (int i = 0; i < TOURN_SIZE - 1; i++) {
                if (fitness[sortIndex[i]] < fitness[sortIndex[i + 1]]) {
                    tmp = sortIndex[i];
                    sortIndex[i] = sortIndex[i + 1];
                    sortIndex[i + 1] = (int) tmp;
                    notSorted = true;
                }
            }
        } while (notSorted);

    } // tselect

    private void reportBest(int run) {
        int b; // Index of best fitnessPM found
        StringBuilder output = new StringBuilder(); // What this method outputs
        Graph G = new Graph(VERTS); // To hold final graph from run

        b = 0;
        for (int i = 1; i < POPSIZE; i++) {
            if (fitness[i] < fitness[b]) {
                b = i;
            }
        }
        output.append(fitness[b]).append(" -fitnessPM").append("\n");

        // Place best graph and fitness in best.lint

        output.append(pop.get(b).get(0));
        for (int i = 1; i < GENE_LENGTH; i++) {
            output.append(" ").append(pop.get(b).get(i));
        }
        output.append("\n");

        express(G, pop.get(b)); // Create graph
        printToFile(outPath + "Graph" + run + ".dat", G.write(), false);
        printToFile(outPath + "best.lint", output.toString(), true);
    }
}


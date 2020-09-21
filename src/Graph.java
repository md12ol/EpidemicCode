import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.random;

import java.util.ArrayList;
import java.util.LinkedList;

@SuppressWarnings({"SameParameterValue", "UnusedMethod"})
public class Graph {

    private int E; // Number edges
    private int V; // Number vertices
    private ArrayList<LinkedList<Integer>> nbr; // Neighbour lists
    private ArrayList<Integer> clr; // Colours
    private boolean created;
    private boolean epidemiced;

    // TODO: Add functionality to catch if epidemic not run yet
    private int maxI; // Maximum infected at one time
    private int len; // Epidemic length
    private int totI; // Total people infected

    Graph() {
        init(128);
    } // Default Constructor

    // Create with max number of vertices
    private void init(int max) {
        V = 0;
        E = 0;
        nbr = new ArrayList<>(max);
        for (int i = 0; i < max; i++) {
            nbr.add(new LinkedList<>());
        }
        clr = new ArrayList<>(max);
        created = false;
        epidemiced = false;
    }

    Graph(int max) {
        init(max);
    } // Constructor

    // Ring of 128 nodes, where each vertex has 2m neighbours (m before and m after)
    void RNGnm(int n, int m) {
        init(n); // Check for storage

        if (m > n) {
            m %= n; // Fail safe
        }

        V = n;
        E = 2 * m * n;
        for (int i = 0; i < n; i++) {
            for (int j = 1; j <= m; j++) {
                nbr.get(i).add((i + j) % n);
                nbr.get(i).add((i - j + n) % n);
            }
        }
    }

    // Hop edge
    void hop(int v, int n1, int n2) {
        int nb1; // Neighbour of v for consideration
        int nb2; // Neighbour of nb1 for consideration
        int d1; // Degree of v
        int d2; // Degree of nb1

        if (V == 0) { // Empty graph
            return;
        }
        v = normalize(v, V);
        d1 = degree(v); // Get degree of neighbour
        if (d1 < 1) { // No neighbours
            return;
        }
        n1 = normalize(n1, d1);
        nb1 = nbrMod(v, n1); // Get neighbour
        d2 = degree(nb1); // Get degree of neighbour
        if (d2 < 2) { // No place to hop
            return;
        }
        n2 = normalize(n2, d2);
        nb2 = nbrMod(nb1, n2); // Get neighbour of neighbour
        if (edgeP(v, nb2)) { // Triangle, no hop possible
            return;
        }
        if (v == nb2) { // Trying to add a loop
            return;
        }

        // TODO: Remove duplicate code above

        // Process hop
        del(v, nb1);
        add(v, nb2);
    }

    // Local Toggle
    void ltog(int v, int n1, int n2) {

        int nb1;
        int nb2;
        int d1;
        int d2;

        if (V == 0) {
            return; // Empty graph
        }
        v = normalize(v, V);
        d1 = degree(v);
        if (d1 < 1) {
            return; // No neighbours
        }
        n1 = normalize(n1, d1);
        nb1 = nbrMod(v, n1); // Get neighbour
        d2 = degree(nb1);
        if (d2 < 2) {
            return; // No place to toggle
        }
        n2 = normalize(n2, d2);
        nb2 = nbrMod(nb1, n2); // Get neighbour of neighbour
        if (v == nb2) {
            return; // Trying to add a loop
        }
        toggle(v, nb2); // Apply toggle
    }

    // Toggle edge
    void toggle(int a, int b) {
        if (V == 0) {
            return;
        }
        a = normalize(a, V);
        b = normalize(b, V);
        if (a == b) {
            return;
        }
        if (nbr.get(a).contains(b)) { // Edge exists
            nbr.get(a).removeFirstOccurrence(b);
            nbr.get(b).removeFirstOccurrence(a);
            E--;
        } else { // Edge doesn't exist
            nbr.get(a).add(b);
            nbr.get(b).add(a);
            E++;
        }
    }

    // Returns the (n % degree)th neighbour of v
    private int nbrMod(int v, int n) {
        return (nbr.get(v).get(n % degree(v)));
    }

    // Takes in orig and returns orig if 0 <= orig < max.  Otherwise, it returns
    // ((orig % max) + max) % max, this forces correct vertex numbers
    private int normalize(int orig, int max) {
        if ((orig < 0) || (orig >= V)) {
            orig = ((orig % max) + max) % max;
        }
        return orig;
    }

    private int degree(int v) {
        return nbr.get(v).size();
    }

    // Local Add
    void ladd(int v, int n1, int n2) {

        int nb1;
        int nb2;
        int d1;
        int d2;

        if (V == 0) {
            return; // Empty graph
        }
        v = normalize(v, V);
        d1 = degree(v);
        if (d1 < 1) {
            return; // No neighbours
        }
        n1 = normalize(n1, d1);
        nb1 = nbrMod(v, n1); // Get neighbour
        d2 = degree(nb1);
        if (d2 < 2) {
            return; // No place to toggle
        }
        n2 = normalize(n2, d2);
        nb2 = nbrMod(nb1, n2); // Get neighbour of neighbour
        if (v == nb2) {
            return; // Trying to add a loop
        }
        add(v, nb2); // Apply add
    }

    // Add edge
    void add(int a, int b) {
        a = normalize(a, V);
        b = normalize(b, V);
        if (a == b) {
            return;
        }
        if (!nbr.get(a).contains(b)) {
            toggle(a, b);
        }
    }

    // Local Del
    void ldel(int v, int n1, int n2) {
        int nb1;
        int nb2;
        int d1;
        int d2;

        if (V == 0) {
            return; // Empty graph
        }
        v = normalize(v, V);
        d1 = degree(v);
        if (d1 < 1) {
            return; // No neighbours
        }
        n1 = normalize(n1, d1);
        nb1 = nbrMod(v, n1); // Get neighbour
        d2 = degree(nb1);
        if (d2 < 2) {
            return; // No place to toggle
        }
        n2 = normalize(n2, d2);
        nb2 = nbrMod(nb1, n2); // Get neighbour of neighbour
        if (v == nb2) {
            return; // Trying to add a loop
        }
        del(v, nb2); // Apply del
    }

    // Del edge
    void del(int a, int b) {
        a = normalize(a, V);
        b = normalize(b, V);
        if (a == b) { // Same vertex
            return;
        }
        if (nbr.get(a).contains(b)) {
            toggle(a, b);
        }
    }

    // Swap
    void swap(int a, int b, int k) {

        int v1;
        int v2;
        int n1;
        int n2;

        // Check degree bound
        v1 = a % V;
        if (nbr.get(v1).size() < k) {
            return;
        }
        v2 = b % V;
        if (nbr.get(v2).size() < k) {
            return;
        }

        if (edgeP(v1, v2)) {
            return; // Already edge from v1 to v2
        }
        n1 = (a / V) % degree(v1); // First neighbour's index
        n1 = nbr.get(v1).get(n1); // First neighbour
        n2 = (b / V) % degree(v2); // Second neighbour's index
        n2 = nbr.get(v2).get(n2); // Second neighbour

        if (edgeP(v1, n2) || edgeP(v2, n1) || edgeP(n1, n2)) { // Edges already exist
            return;
        }

        // Process swap
        toggle(v1, n1);
        toggle(v2, n2);
        toggle(v1, n2);
        toggle(v2, n1);
    }

    // Is a to b an edge
    private boolean edgeP(int a, int b) {
        if ((a < 0) || (b < 0) || (a >= V) || (b >= V)) { // Not a vertex
            return false;
        }
        return nbr.get(a).contains(b);
    }

    int SIRLength(double alpha, boolean sirs, int removedLength) {

        if (!created) {
            System.out.println("ERROR: SIRLength not created");
        }

        maxI = 0;
        totI = 0;
        len = 0;

        int NI; // Number of infected individuals
        ArrayList<Integer> nin; // Number of Infected Neighbors

        if (V == 0) {
            System.out.println("ERROR: SIRLength on Graph with 0 Nodes");
        }

        setClr(0); // Susceptible
        clr.set(0, 1); // Infect node with index 0
        NI = 1;
        len = 0;
        int neighbour;

        while (NI > 0) { // Still infected
            nin = new ArrayList<>(V);
            for (int i = 0; i < V; i++) { // Clear nin
                nin.add(0);
            }
            for (int i = 0; i < V; i++) { // Fill nin
                if (clr.get(i) == 1) { // Infected individual found (node i)
                    for (int j = 0; j < nbr.get(i).size(); j++) { // For each neighbour
                        neighbour = nbr.get(i).get(j);
                        nin.set(neighbour, nin.get(neighbour) + 1); // Increase nin
                    }
                }
            }
            // Check for transmission
            for (int i = 0; i < V; i++) {
                if ((clr.get(i) == 0) && (nin.get(i) > 0)) {
                    clr.set(i, infected(nin.get(i), alpha) ? 4 : 0);
                }
            }
            if (NI > maxI) {
                maxI = NI;
            }
            totI += NI;
            NI = 0;

            // TODO: correct below
            if (sirs) {
                final int cap = removedLength + 3; // SIR and Newly Infected (0-3)
                for (int i = 0; i < V; i++) {
                    int colour = clr.get(i);
                    if (colour == 1) {
                        clr.set(i, 2); // Infected, move to removed
                    } else if (colour == 2) {
                        clr.set(i, 4); // Removed, move to next day of resistance
                    } else if (colour == 3) {
                        clr.set(i, 1); // Newly infected, move to infected
                        NI++;
                    } else if (colour == cap) {
                        clr.set(i, 0); // End of resistance, move to susceptible
                    } else if (colour != 0) {
                        clr.set(i, colour + 1);
                    }
                }
            } else {
                for (int i = 0; i < V; i++) {
                    switch (clr.get(i)) {
                        case 0: // Susceptible, do nothing
                            break;
                        case 1: // Infected, move to removed
                            clr.set(i, 2);
                            break;
                        case 2: // Removed, do nothing
                            break;
                        case 3: // Newly infected, move to infected
                            clr.set(i, 1);
                            NI++;
                            break;
                    }
                }
            }
            len++;
        }
        epidemiced = true;
        return len;
    } // SIRLength

    // Returns true if node would become infected given strength alpha
    private boolean infected(int n, double alpha) {
        double beta;
        beta = 1 - exp(n * log(1 - alpha));
        return (random() < beta);
    }

    // Set all colours to c
    private void setClr(int c) {
        clr = new ArrayList<>(V);
        for (int i = 0; i < V; i++) {
            clr.add(c);
        }
    }

    double[] SIRProfile(double alpha) {

        if (!created) {
            System.out.println("ERROR: SIRProfile not created");
        }

        double[] prof = new double[V];

        maxI = 0;
        totI = 0;
        len = 0;

        int NI; // Number of infected individuals
        ArrayList<Integer> nin; // Number of Infected Neighbors

        if (V == 0) {
            System.out.println("ERROR: SIRProfile on Graph with 0 Nodes");
        }

        for (int i = 0; i < V; i++) {
            prof[i] = 0;
        }

        setClr(0); // Susceptible
        clr.set(0, 1); // Infect node with index 0
        NI = 1;
        len = 0;
        prof[len] = 1.0;
        int neighbour;

        while (NI > 0) { // Still infected
            nin = new ArrayList<>(V);
            for (int i = 0; i < V; i++) {
                nin.add(0);
            }
            for (int i = 0; i < V; i++) {
                if (clr.get(i) == 1) { // Infected individual found (node i)
                    for (int j = 0; j < nbr.get(i).size(); j++) { // For each neighbour
                        neighbour = nbr.get(i).get(j);
                        nin.set(neighbour, nin.get(neighbour) + 1); // Increase nin
                    }
                }
            }
            // Check for transmission
            for (int i = 0; i < V; i++) {
                if ((clr.get(i) == 0) && (nin.get(i) > 0)) {
                    clr.set(i, infected(nin.get(i), alpha) ? 4 : 0);
                }
            }
            if (NI > maxI) {
                maxI = NI;
            }
            totI += NI;
            NI = 0;

            for (int i = 0; i < V; i++) {
                switch (clr.get(i)) {
                    case 0: // Susceptible, do nothing
                        break;
                    case 1: // Infected day 1, move to day 2
                        clr.set(i, 2);
                        break;
                    case 2: // Infected day 2, move to removed
                        clr.set(i, 3);
                        break;
                    case 3: // Removed, do nothing
                        break;
                    case 4: // Newly infected, move to infected
                        clr.set(i, 1);
                        NI++;
                        prof[len + 1] += 1.0;
                        break;
                }
            }
            len++;
        }
        epidemiced = true;
        return prof;
    }

    String write() {
        StringBuilder output = new StringBuilder();
        output.append(V).append("\t").append(E).append("\n");
        for (LinkedList<Integer> l : nbr) {
            for (int n : l) {
                output.append(n).append("\t");
            }
            output.append("\n");
        }
        return output.toString();
    }

    public int getMaxI() {
        if (!created || !epidemiced) {
            System.out.println("ERROR: getMaxI");
        }
        return maxI;
    }

    int getLen() {
        if (!created || !epidemiced) {
            System.out.println("ERROR: getLen");
        }
        return len;
    }

    void setLen(int l) {
        if (!created || !epidemiced) {
            System.out.println("ERROR: setLen");
        }
        len = l;
    }

    public int getTotI() {
        if (!created || !epidemiced) {
            System.out.println("ERROR: getTotI");
        }
        return totI;
    }

    void setCreated() {
        created = true;
        epidemiced = false;
    }

}

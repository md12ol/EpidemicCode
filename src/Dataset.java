import static java.lang.Math.sqrt;

class Dataset {

    private double max;     // Max value in Dataset
    private double min;     // Minimum value in Dataset
    private int count;      // Number of values added to the Dataset
    private double mu;      // Average of sum
    private double sd;      // Standard deviation
    private double CI95;    // 95% Confidence Interval
    private boolean empty;  // Whether set is empty or not
    private boolean computed; // Keeps track if statistical analysis has occurred yet
    private double sum;     // Sum of all values in Dataset
    private double sums;    // Sum of all values squared in Dataset

    Dataset() {
        // TODO: Seriously.....just turn add into a constructor...
        sum = 0.0;
        sums = 0.0;
        max = 0.0;
        min = 0.0;
        count = 0;
        mu = 0.0;
        sd = 0.0;
        CI95 = 0.0;
        empty = true;
        computed = false;
    }

    // Adds the values within vals to the Dataset
    void add(double[] vals) {
        if (empty) { // Clear
            sum = 0.0;
            sums = 0.0;
            max = vals[0];
            min = vals[0];
            count = 0;
            mu = 0.0;
            sd = 0.0;
            CI95 = 0.0;
            empty = false;
            computed = false;
        }
        for (double val : vals) {
            sum += val;
            sums += (val * val);
            if (val > max) {
                max = val;
            }
            if (val < min) {
                min = val;
            }
        }
        count += vals.length;
    }

    // Calculates the 95% Confidence Interval, Mu and Standard Deviation
    private void compute() {
        double n;

        if (!empty) {
            n = (double) count;
            mu = sum / n;
            if (count > 1) {
                sd = sums / n - mu * mu;
                if (sd > 0) {
                    sd = sqrt(sd);
                } else {
                    sd = 0.0;
                }
                CI95 = 1.96 * sd / sqrt(n - 1.0);
            } else {
                sd = 0.0;
                CI95 = 0.0;
            }
        }
        computed = true;
    }

    double getMu() {
        if (!computed) {
            compute();
            // TODO: Exceptions for these bools
        }
        return mu;
    }

    double getSD() {
        if (!computed) {
            compute();
        }
        return sd;
    }

    double getCI() {
        if (!computed) {
            compute();
        }
        return CI95;
    }

    double getMin() {
        if (!computed) {
            compute();
        }
        return min;
    }

    double getMax() {
        if (!computed) {
            compute();
        }
        return max;
    }

    String getReport() {
        if (!computed) {
            compute();
        }
        return mu + " " + CI95 + " " + sd + " " + min + "\n";

    }

}

import static java.lang.Thread.sleep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Main {

  private static final String OUTPUT_FOLDER = "./Output/";

  private static final int NUM_PROFILES = 9;          // Number of profiles
  private static final int NUM_COMMANDS = 9;          // Number of commands

  private static final Map<String, Integer> MODES;    // Possible program modes
  private static int mode;                                    // Mode to run
  private static int numParamSettings;

  static {
    MODES = new HashMap<>();
    MODES.put("Folder Making", -1);
    MODES.put("Profile Matching", 0);                 // Typical Profile Matching
    MODES.put("Epidemic Duration", 1);                // Typical Epidemic Duration
    MODES.put("Epidemic Duration w SIRS", 2);         // Epidemic Duration w SIRS
  }

  /**
   * Facilitates the desired test.
   *
   * @param args 0: bool Folder Making Mode, 1-9: double Densities (THADS[L(TAD)]N), 10: int Time in
   *             Removed State (SIRS), 11: String Output path from ./Output/, 12: int Profile
   *             Number
   */
  public static void main(String[] args) {

    switch (Integer.parseInt(args[0])) { // Mode
      case -1:
        mode = MODES.get("Folder Making");
        break;
      case 0:
        mode = MODES.get("Profile Matching");
        break;
      case 1:
        mode = MODES.get("Epidemic Duration");
        break;
      case 2:
        mode = MODES.get("Epidemic Duration w SIRS");
        break;
    }

    int numCores = Integer.parseInt(args[1]);
    int removedLength = Integer.parseInt(args[2]);
    int profileNum = 0;
    numParamSettings = 0;
    LinkedList<LinkedList<Double>> PS = new LinkedList<>();


    BufferedReader reader;
    try {
      reader = new BufferedReader(new FileReader("./ParameterSettings.txt"));
      String line = reader.readLine();
      while (line != null) {
        LinkedList<Double> paramSetting = new LinkedList<>();
        for (String s : line.split(" ")) {
          paramSetting.add(Double.parseDouble(s));
        }
        line = reader.readLine();
        PS.add(new LinkedList<>(paramSetting));
        numParamSettings++;
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (mode < 0) {
      folderMode();
    } else {
      int index = 1;
      int running = 0;
      long tStart = System.nanoTime();
      while (index <= numParamSettings) {
        // Setup
        ArrayList<GET> workers = new ArrayList<>();
        for (int i = index - 1; i < index + numCores - 1; i++) {
          if (i < numParamSettings) {
            String outPath = OUTPUT_FOLDER + "PS" + (i + 1) + "/";
//            workers.add(new GET(mode, profileNum, PS.get(i), removedLength, outPath));
          }
        }
        // Start
        for (GET w : workers) {
//          w.start();
//          System.out.println("Process " + w.getId() + " started.");
          running++;
          try {
            sleep(1500); // 5 Seconds
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        // Wait till done
//        for (GET w : workers) {
//          try {
//            w.join();
//            System.out.println("Process " + w.getId() + " ended.");
//            running--;
//          } catch (InterruptedException e) {
//            e.printStackTrace();
//          }
//        }
        index += numCores;
      }
      long tEnd = System.nanoTime();
      long time = (tEnd - tStart) / (1000 * 1000 * 1000);
      System.out.println("Took " + time + " seconds with " + numCores + " cores.");
    }
  }

  private static void folderMode() {
    boolean outputFolderExists = false;
    boolean error = false;

    String workingDirPath = FileSystems.getDefault().getPath(".").toString();
    File workingDir = new File(workingDirPath);
    LinkedList<File> files = getFiles(workingDirPath);
    if (files != null) { // Files exist within folder
      for (File f : files) {
        if (f.getName().equals("Output")) {
          outputFolderExists = true;
        }
      }
    }
    try {
      if (outputFolderExists) {
        File outputFolder = new File("Output");
        if (outputFolder.isDirectory()) {
          if (!deleteDir(outputFolder)) {
            throw new FileSystemException("ERROR: Could not delete " + outputFolder.getName());
          }
        } else {
          if (outputFolder.delete()) {
            throw new FileSystemException("ERROR: Could not delete " + outputFolder.getName());
          }
        }

      }
    } catch (FileSystemException e) {
      System.out.println(e.getMessage());
      error = true; // Directory not deleted
    }

    try {
      if (!error) { // Directory deleted
        File output = new File(workingDir, "./Output");
        if (!output.mkdir()) {
          throw new FileSystemException("ERROR: Could not make " + output.getName());
        }
        makeFolders(output);
      }
    } catch (FileSystemException e) {
      System.out.println(e.getMessage());
    }
  }

  private static void makeFolders(File root) throws FileSystemException {
    LinkedList<String> prefixes = new LinkedList<>();
    LinkedList<String> postfixes = new LinkedList<>();
    LinkedList<Integer> counts = new LinkedList<>();
    //    switch (mode) {
    //      case -0: // Profile matching
    //        prefixes.add("PS");
    //        postfixes.add("");
    //        counts.add(numParamSettings);
    //        prefixes.add("P");
    //        postfixes.add("");
    //        counts.add(NUM_PROFILES);
    //        makeOutputFolders(root, prefixes, postfixes, counts);
    //        break;
    //      case -1: // Epidemic duration
    //        prefixes.add("PS");
    //        postfixes.add("");
    //        counts.add(numParamSettings);
    //        makeOutputFolders(root, prefixes, postfixes, counts);
    //        break;
    //      case -2: // Epidemic duration w SIRS
    //        prefixes.add("PS");
    //        postfixes.add("");
    //        counts.add(numParamSettings);
    //        makeOutputFolders(root, prefixes, postfixes, counts);
    //        break;
    //    }
    prefixes.add("PS");
    postfixes.add("");
    counts.add(numParamSettings);
    makeOutputFolders(root, prefixes, postfixes, counts);
  }

  private static void makeOutputFolders(File parent, LinkedList<String> pres,
      LinkedList<String> posts, LinkedList<Integer> count) throws FileSystemException {
    if (pres.size() != posts.size() || pres.size() != count.size()) {
      throw new IllegalArgumentException("ERROR: Arguements pres, posts and count do not have"
          + "equilivant size, as expected.");
    }
    File madeFile;

    if (count.size() == 1) { // Final layer
      for (int i = 1; i <= count.peekFirst(); i++) {
        madeFile = new File(parent, pres.peekFirst() + i + posts.peekFirst());
        if (!madeFile.mkdir()) {
          throw new FileSystemException("ERROR: Could not make" + madeFile.getName());
        }
      }
    } else { // Not final
      LinkedList<String> newPres = new LinkedList<>(pres);
      LinkedList<String> newPosts = new LinkedList<>(posts);
      LinkedList<Integer> newCount = new LinkedList<>(count);
      newPres.removeFirst();
      newPosts.removeFirst();
      newCount.removeFirst();
      for (int i = 1; i <= count.peekFirst(); i++) {
        madeFile = new File(parent, pres.peekFirst() + i + posts.peekFirst());
        if (!madeFile.mkdir()) {
          throw new FileSystemException("ERROR: Could not make" + madeFile.getName());
        }
        makeOutputFolders(madeFile, newPres, newPosts, newCount);
      }
    }
  }

  private static LinkedList<File> getFiles(String root) {
    File dir = new File(root);
    return getFiles(dir);
  }

  private static LinkedList<File> getFiles(File root) {
    LinkedList<File> files = new LinkedList<>();
    String[] fileNames = (String[]) root.list();
    if (fileNames == null) {
      return null;
    } else {
      for (String fName : fileNames) {
        if (fName != null) {
          files.add(new File(fName));
        }
      }
      return files;
    }
  }

  private static boolean deleteDir(String root) throws FileSystemException {
    File dir = new File(root);
    return deleteDir(dir);
  }

  private static boolean deleteDir(File root) throws FileSystemException {
    File[] contents = root.listFiles();
    for (File f : contents) {
      if (f.isDirectory()) {
        deleteDir(f);
      } else {
        if (!f.delete()) {
          throw new FileSystemException("ERROR: Could not delete " + f.getPath());
        }
      }
    }
    return root.delete();
  }
}

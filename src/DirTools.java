import java.io.File;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystems;
import java.util.LinkedList;

public class DirTools {

    /**
     * @param args mode, numPSs, numProfs
     */
//    public static void main(String[] args) {
//        folderMode(Integer.parseInt(args[0]));
//    }

    DirTools(int numPSs, int numProfs){
        folderMode(numPSs, numProfs);
    }

    void folderMode(int numPSs, int numProfs) {
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
                        throw new FileSystemException(
                            "ERROR: Could not delete " + outputFolder.getName());
                    }
                } else {
                    if (outputFolder.delete()) {
                        throw new FileSystemException(
                            "ERROR: Could not delete " + outputFolder.getName());
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
                if (numPSs>0){
                    makeFolders(output, numPSs, numProfs);
                }
            }
        } catch (FileSystemException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void makeFolders(File root, int numPSs, int numProfs)
        throws FileSystemException {
        LinkedList<String> prefixes = new LinkedList<>();
        LinkedList<String> postfixes = new LinkedList<>();
        LinkedList<Integer> counts = new LinkedList<>();
        prefixes.add("PS");
        postfixes.add("");
        counts.add(numPSs);
        prefixes.add("P");
        postfixes.add("");
        counts.add(numProfs);
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

    public static void main(String[] args){
        new DirTools(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }

} // DirTools

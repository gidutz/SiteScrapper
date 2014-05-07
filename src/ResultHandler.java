import java.io.*;
import java.util.*;

/**
 * Created by gidutz on 5/1/14.
 */
public class ResultHandler implements Runnable {
    private String timestamp;
    private String path;
    private String inputFile;

    /*list of stores that were added since the last run*/
    private LinkedList<Store> addedSinceLastRun;

    /*list of stores that were removed since the last run*/
    private LinkedList<Store> removedSinceLastRun;

    /*The list resulted from the last run*/
    private LinkedList<Store> oldList;

    /* The list resulted from the current run*/
    private LinkedList<Store> newList;


    /**
     * @param oldList
     * @param newList
     * @param path
     * @param timeStamp
     */
    public ResultHandler(LinkedList oldList, LinkedList newList, String path, String timeStamp) {
        this.inputFile = inputFile;
        this.addedSinceLastRun = new LinkedList<Store>();
        this.removedSinceLastRun = new LinkedList<Store>();
        this.oldList = oldList;
        this.newList = newList;
        this.path = path;
        this.timestamp = timeStamp;

    }

    @Override
    public void run() {

        compareLists();
        logResultsToFile();


    }

    private void logResultsToFile() {
        PrintWriter out = null;
        System.out.println(path + "/" + timestamp + "-RESULTS.txt");
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(path  + timestamp + "-RESULTS.txt", true)));
            out.println("result of run from " + timestamp);

            out.println("new restaurants:");
            for (Store store : addedSinceLastRun) {
                out.println(store);
            }

            out.println("removed restaurants:");
            for (Store store : removedSinceLastRun) {
                out.println(store);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }

    /*
    *   compares the loaded list with the current list
    *   Should run in O(n)
     */
    private void compareLists() {
        Store oldStore = null;
        while (!oldList.isEmpty()) {

            //if the new list is shorter then all the rest were removed
            if (newList.peek() == null) {
                for (Store store : newList) {
                    removedSinceLastRun.add((store));
                }
                continue;
            }

            oldStore = oldList.peek();
            if (oldStore.equals(newList.peek())) {
                newList.poll();
                oldList.poll();
            } else if (oldStore.compareTo(newList.peek()) > 0) {
                addedSinceLastRun.add(newList.poll());
            } else {
                removedSinceLastRun.add(oldStore);
                oldList.poll();
            }

        }
        if (!newList.isEmpty()) {
            for (Store store : newList) {
                addedSinceLastRun.add((store));
            }
        }
    }


}

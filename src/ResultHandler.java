import java.util.*;

/**
 * Created by gidutz on 5/1/14.
 */
public class ResultHandler implements Runnable {
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
     *
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
    }

    @Override
    public void run() {

        compareLists();
        logResultsToFile();
        //TODO:
    /*
    * get the last file
    * get the current file
    * compare the files
    * make result file "Scrap from date... was compared to scrap from date... x new restaurants were added, y were removed
    * here is the list of restaurants added, here is the list of restaurants removed.
    *
    * */

    }

    private void logResultsToFile() {

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

            oldStore = oldList.poll();
            if (oldStore.equals(newList.peek())) {
                newList.poll();
            } else if (oldStore.compareTo(newList.peek()) > 0) {
                addedSinceLastRun.add(newList.poll());
            } else {
                removedSinceLastRun.add(oldStore);
            }

        }
        if (!newList.isEmpty()) {
            for (Store store : newList) {
                addedSinceLastRun.add((store));
            }
        }
    }



}

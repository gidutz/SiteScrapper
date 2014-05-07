/**
 * Created by gidutz on 4/29/14.
 */

import org.json.simple.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;


public class SiteScapper {
    /**
     * path to the file containing the urls to scrap
     */
    static LinkedList<String> urlsToScrap;
    static LinkedList<Store> storeList;

    /**
     * settings
     */
    static final String SETTINGS_FILE = "config.ini";
    static final String URL_TAG = "urls";
    static final String LAST_RUN_TAG = "last_run";
    static final String OUTPUT_PATH_TAG = "output_path";

    static final String JSON = "-JSON.txt";
    static final String TABLE = "-TABLE.txt";
    static final String COMPARE = "-COMPARE.txt";

    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    static String urlsFile;
    static String lastRun;
    static String outPath;
    static String timeStamp;


    public static void main(String args[]) {


        System.out.println("Runnig...");
        Calendar cal = Calendar.getInstance();
        timeStamp = dateFormat.format(cal.getTime());
        System.out.println("runtime name = " + timeStamp);

        loadProperties();

        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);

        ExecutorService executor = Executors.newCachedThreadPool();
        CompletionService<ConcurrentSkipListSet<Store>> completionService = new ExecutorCompletionService<ConcurrentSkipListSet<Store>>(executor);
        try {
            Future<ConcurrentSkipListSet<Store>> future = null;
            loadUrls();

            //start scrapping sites
            for (String url : urlsToScrap) {
                JsonFetcher scrapper = new JsonFetcher(url);
                completionService.submit(scrapper);
            }

            if (storeList == null) {
                storeList = new LinkedList<Store>();
            }

            //gets a store list from the next completed task
            for (int i = 0; i < urlsToScrap.size(); i++) {
                future = completionService.take();
                ConcurrentSkipListSet<Store> retrievedStoreList = future.get();
                for (Store store : retrievedStoreList) {
                    storeList.add(store);
                }

            }
            executor.shutdown();
            Collections.sort(storeList);


            logToFile();

            System.out.println("Total of " + storeList.size() + " stores");


            setProperties();


            ResultHandler resultHandler = new ResultHandler(lastRunList(), storeList, outPath, timeStamp);
            Thread handleResults = new Thread(resultHandler);
            handleResults.join();
            handleResults.start();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            executor.shutdown();
            System.out.println("Run completed successfully!");
        }


    }

    /**
     * make sure u have 2 files, on as table and other as JSON array
     */
    private static void logToFile() {

        File jsonOutFile = new File(outPath + timeStamp + JSON);
        File tableOutFile = new File(outPath + timeStamp + TABLE);

        System.out.println("trying to log to files: " + jsonOutFile.getAbsolutePath());
        try {
            if (!jsonOutFile.exists()) {
                jsonOutFile.createNewFile();

            }
            if (tableOutFile.exists()) {
                tableOutFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        * prints the JSON file
         */
        List jsonList = new LinkedList();

        for (int i = 0; i < storeList.size(); i++) {
            Map<String, String> storeMap = new LinkedHashMap();

            Store store = storeList.get(i);
            storeMap.put("title", store.getTitle());
            storeMap.put("phone", store.getPhone());
            storeMap.put("lat", "" + store.getLat());
            storeMap.put("long", "" + store.getLng());
            storeMap.put("city", store.getCity());
            storeMap.put("type", "" + store.getType());
            jsonList.add(storeMap);
        }

        String jsonString = JSONValue.toJSONString(jsonList);
        PrintWriter jsonOutputStream = null;
        try {
            jsonOutputStream = new PrintWriter(new FileOutputStream(jsonOutFile));
            jsonOutputStream.println(jsonString);
            jsonOutputStream.flush();
            System.out.println("printed JSON array to result file " + jsonOutFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            System.err.println("Could not create JSON file! " + e.getStackTrace());
        } finally {

            jsonOutputStream.close();
        }

        /*
        * prints the table file
         */
        PrintWriter tableOutputStream = null;
        try {
            for (int i = 0; i < storeList.size(); i++) {
                tableOutputStream = new PrintWriter(new FileOutputStream(tableOutFile, true));

                tableOutputStream.println(storeList.get(i));
                tableOutputStream.flush();

            }

            System.out.println("printed TABLE  to result file " + tableOutFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            System.err.println("Could not create TABLE file! " + e.getStackTrace());
        } finally {

            tableOutputStream.close();
        }
    }

    /**
     * loads the urls to be scrapped to an array list
     *
     * @throws FileNotFoundException
     */
    private static void loadUrls() throws FileNotFoundException {

        if (urlsToScrap == null)
            urlsToScrap = new LinkedList<String>();

        BufferedReader reader = new BufferedReader((new FileReader(new File(urlsFile))));
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                //TODO: validate string format
                urlsToScrap.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadProperties() {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(SETTINGS_FILE);

            // load a properties file
            prop.load(input);

            // get the property values
            urlsFile = prop.getProperty(URL_TAG);
            lastRun = prop.getProperty(LAST_RUN_TAG);
            outPath = prop.getProperty(OUTPUT_PATH_TAG);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void setProperties() {
        Properties prop = new Properties();
        OutputStream output = null;

        try {

            output = new FileOutputStream(SETTINGS_FILE);

            // set the properties value
            prop.setProperty(LAST_RUN_TAG, timeStamp);
            prop.setProperty(URL_TAG, urlsFile);
            prop.setProperty(OUTPUT_PATH_TAG, outPath);

            // save properties to project root folder
            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private static LinkedList<Store> lastRunList() {
        LinkedList<Store> returnList = new LinkedList<Store>();
        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(outPath + "/" + lastRun + JSON))));
            while (reader.ready()) {
                sb.append(reader.readLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Object obj = JSONValue.parse(sb.toString());
        JSONArray array = (JSONArray) obj;
        for (int i = 0; i < array.size(); i++) {
            returnList.add(JsonFetcher.storeFromJSON(array.get(i).toString()));
        }
        return returnList;
    }
}


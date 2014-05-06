import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import org.json.simple.parser.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

/**
 * This Class represents a site scrapper that loads pages, searches for a specific pattern of JSON object and downloads it
 * <p/>
 * Created by gidutz on 5/1/14.
 */
public class JsonFetcher implements Callable<ConcurrentSkipListSet<Store>> {
    private final WebClient webClient;

    /*
    starts with brackets, possible several spaces, quotes, word, quotes, : ,quotes, word, quotes space and brackets
    */
    public static final String JSON_PATTERN = "((\\{{1}\\s*?\"?[^\\{\\}]*?\"?:\\s*?\"?[^\\{\\}]*?\"?,?\\s*?\\}{1})\\s*?,?\\s*?)";
    /*
     reppeating the pattern of JSON object
     */
    public static final String JSON_ARRAY_PATTERN = "(\\[\\s*?" + JSON_PATTERN + "+?\\])";
    /*
     *the url to scrap
     */
    private  String url;

    /**
     * constructs a new scrapper on a given url
     * @param url
     */
    public JsonFetcher(String url) {
        this.webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        this.url = url;
    }

    /**
     * retrives a JSON array from the page and then matches each object of the array
     * prints the objects one by one
     *
     * @return
     */
    @Override
    public ConcurrentSkipListSet<Store> call() {
        final HtmlPage page;
        ConcurrentSkipListSet<Store> storeReturnList = new ConcurrentSkipListSet
                <Store>();
        try {

            //fetching the page
            System.out.println("Loading " + url);
            page = webClient.getPage(url);
            StringBuilder pageXml = new StringBuilder();
            pageXml.append(page.asXml());

           //matches the JSON array
            int matches = 0;
            Matcher matchArray = Pattern.compile(JSON_ARRAY_PATTERN).matcher(pageXml);
            if (matchArray.find()) { //match array pattern
                Matcher matchSingleStore = Pattern.compile(JSON_PATTERN).matcher(matchArray.group());

                //construct a Store for every JSON object
                while (matchSingleStore.find()) {
                    matches++;
                    storeReturnList.add(storeFromJSON(matchSingleStore.group(2)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Thread has finished!");
            webClient.closeAllWindows();
            return storeReturnList;

        }
    }

    /**
     * Constructs a Store object from the JSON string
     * @param jsonString
     * @return
     */
    protected static Store storeFromJSON(String jsonString) {
        Store store = null;
        JSONParser parser = new JSONParser();
        ContainerFactory containerFactory = new ContainerFactory() {
            public List creatArrayContainer() {
                return new LinkedList();
            }

            public Map createObjectContainer() {
                return new LinkedHashMap();
            }

        };

        try {
            Map<String, String> json = (Map<String, String>) parser.parse(jsonString, containerFactory);

            store = new Store.StoreBuilder().city(json.get("city"))
                    .phone(json.get("phone"))
                    .lat(json.get("lat"))
                    .lng(json.get("long"))
                    .type(json.get("type"))
                    .title(json.get("title")).build();
        } catch (ParseException pe) {
            System.out.println(pe);
        }
        return store;


    }
}

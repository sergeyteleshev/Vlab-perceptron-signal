package vlab.server_java;

import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import com.wolfram.alpha.WASubpod;
import org.json.JSONObject;

import java.util.ArrayList;


/*
 * A simple example program demonstrating the WolframAlpha.jar library. The program
 * performs a query given on the command line and prints out the resulting pod titles
 * and plaintext content.
 * 
 * You will need to insert your appid into the code. To compile or run this program
 * you will need the following dependent libraries on your classpath (including 
 * WolframAlpha.jar, of course):
 * 
 *     commons-codec-1.3.jar
 *     httpclient-4.0.1.jar
 *     httpcore-4.0.1.jar
 *     commons-logging.jar
 *     
 * These libraries are widely available on the Internet. You can probably use other version
 * numbers than these, although these are the versions I used.
 * 
 * To launch from the command line, do the following (these classpath specifications assume
 * that the WolframAlpha.jar file and the four other dependent jars listed above are in the same
 * directory as AlphaAPISample.class):
 * 
 *     Windows:
 *     
 *       java -classpath .;* AlphaAPISample "sin x"
 *     
 *     Linux, Mac OSX:
 *     
 *       java -classpath .:* AlphaAPISample "sin x"
 */

public class WolframAPI {

    // PUT YOUR APPID HERE:
    private static String appid = "4EY46U-KWKU9KEGKE";

    public static JSONObject getResults(String input)
    {
        WAEngine engine = new WAEngine();
        JSONObject result = new JSONObject();

        // These properties will be set in all the WAQuery objects created from this WAEngine.
        engine.setAppID(appid);
        engine.addFormat("plaintext");

        // Create the query.
        WAQuery query = engine.createQuery();

        // Set properties of the query.
        query.setInput(input);

        try {
            // This sends the URL to the Wolfram|Alpha server, gets the XML result
            // and parses it into an object hierarchy held by the WAQueryResult object.
            WAQueryResult queryResult = engine.performQuery(query);

            if (queryResult.isError()) {
                System.out.println("Query error");
                System.out.println("  error code: " + queryResult.getErrorCode());
                System.out.println("  error message: " + queryResult.getErrorMessage());
            } else if (!queryResult.isSuccess()) {
                System.out.println("Query was not understood; no results available.");
            } else {
                // Got a result.
                System.out.println("Successful query. Pods follow:\n");
                for (WAPod pod : queryResult.getPods()) {
                    if (!pod.isError()) {
                        String title = pod.getTitle();
                        ArrayList<Object> subpods = new ArrayList<>();
                        System.out.println(title);
                        System.out.println("------------");
                        for (WASubpod subpod : pod.getSubpods()) {
                            for (Object element : subpod.getContents()) {
                                if (element instanceof WAPlainText) {
                                    System.out.println(((WAPlainText) element).getText());
                                    System.out.println("");
                                    subpods.add(subpod);
                                }
                            }
                        }
                        System.out.println("");
                        result.append(title, subpods);
                    }
                }

                // We ignored many other types of Wolfram|Alpha output, such as warnings, assumptions, etc.
                // These can be obtained by methods of WAQueryResult or objects deeper in the hierarchy.
            }
        } catch (WAException e) {
            e.printStackTrace();
        }

        return result;
    }
}

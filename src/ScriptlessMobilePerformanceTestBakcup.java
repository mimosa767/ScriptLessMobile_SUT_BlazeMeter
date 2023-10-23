import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ScriptlessMobilePerformanceTestBakcup {

    private static String securityToken = "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4YmI4YmZmZS1kMzBjLTQ2MjctYmMxMS0zNTYyMmY1ZDkyMGYifQ.eyJpYXQiOjE2NzUyMjkzNTIsImp0aSI6IjQ2NWE3N2QxLTIwZDktNDdmYy04MTZlLWRiM2MwY2QzNmI2YyIsImlzcyI6Imh0dHBzOi8vYXV0aC5wZXJmZWN0b21vYmlsZS5jb20vYXV0aC9yZWFsbXMvZGVtby1wZXJmZWN0b21vYmlsZS1jb20iLCJhdWQiOiJodHRwczovL2F1dGgucGVyZmVjdG9tb2JpbGUuY29tL2F1dGgvcmVhbG1zL2RlbW8tcGVyZmVjdG9tb2JpbGUtY29tIiwic3ViIjoiZDQ0Y2Q5ZjctYmY3Mi00M2YwLWFjYjgtNzcyNDdkMjFhMDMyIiwidHlwIjoiT2ZmbGluZSIsImF6cCI6Im9mZmxpbmUtdG9rZW4tZ2VuZXJhdG9yIiwibm9uY2UiOiJlYzY3YzcwMy1lOTE4LTQ2ZTUtODc2ZS05MmYyYTRmNzA3YWUiLCJzZXNzaW9uX3N0YXRlIjoiOWE4MzhlNGMtYTY5OC00YjYxLTliZmEtNmEzZjUwMjYwYTE4Iiwic2NvcGUiOiJvcGVuaWQgb2ZmbGluZV9hY2Nlc3MifQ.KLGLiHd0tDQj4i0sxQ15gKEUzZhdn00E9TtZLhcd2F0";
    private static String cloudUrl = "demo.perfectomobile.com";

    public static String blazeJobStatus = "false";
    public static int blazeJobId = 0;

    String testNumber = "";
    String APIkey = "YzE0ZjQ5YWUwMTZiZGJjMzgzOTQyM2U3OjEyODFmN2U3YTQyNGZiZmQ4MDJhNzQxZmJjMDZhNGM4YWY3ODNhMTI5ZjBhMmUyMWFiMDdkMjVjYjgyNzM1NzUzMmIzZDFkOQ==";

    // Some variables that need to live outside code blocks
    int jobID = 0;
    int index = 0;

    @BeforeClass
    public void beforeClass(){
        StringBuffer response = new StringBuffer();
        JSONObject myResponse = null;

        if (blazeJobStatus.equalsIgnoreCase("false")) {
            String jobStatus = "";
            // Start the BlazeMeter job and get the job ID
            try {
                response = APIPostRequest("https://a.blazemeter.com/api/v4/tests/" + testNumber + "/start?delayedStart=false");

                // Read JSON response and print
                myResponse = new JSONObject(response.toString());
                System.out.println("result after Reading JSON Response");
                // Get the jobID of the job we created
                JSONObject result = myResponse.getJSONObject("result");
                jobID = result.getInt("id");
                System.out.println("jobID: " + jobID);
                blazeJobStatus = "true";
                blazeJobId = jobID;
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Now check the status until it gives "DATA_RECEIVED" which
            // means the BlazeMeter job is now running

            do {
                //ReportUtils.logStepStart("Checking Job Status...");
                try {
                    //URL url = new URL("https://a.blazemeter.com/api/v4/masters/" + blazeJobId + "/status?level=DEBUG");
                    response = APIPostRequest("https://a.blazemeter.com/api/v4/masters/" + blazeJobId + "/status?level=DEBUG");

                    myResponse = new JSONObject(response.toString());
                    JSONObject result = myResponse.getJSONObject("result");
                    jobStatus = (String) result.get("status");
                    System.out.println("jobStatus:" + jobStatus);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Sleep for a couple of second before trying again
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (Exception e) {
                    System.out.println("Oops! Something went wrong!");
                }

            } while (!jobStatus.equals("DATA_RECEIVED"));
        }
    }

    @Test
    @Parameters({"scriptKey", "DUT", "testName", "jobName", "jobNumber", "appPath"})
    public void mainTest(String scriptKey, String DUT, String testName, String jobName, String jobNumber, String appPath) throws InterruptedException, ParserConfigurationException, IOException, SAXException {
        int index = 0;
        String jobStatus = "";
        System.out.println("I am here");
        StringBuffer response = new StringBuffer();
        JSONObject myResponse = null;
        String[] epochTime = new String[20];
        String[][] metricDescription = new String[20][20];
        String[][] metricValue = new String[20][20];
        String dManufacturer = null, dModel = null, osVersion = null;

        do {

        } while (jobStatus.equals("DATA_RECEIVED"));

        response = APIPostRequest("https://" + cloudUrl + "/services/executions?operation=execute&scriptKey=" + scriptKey + "&securityToken=" +
                securityToken + "&param.DUT=" + DUT + "&param.TestName=" + testName + "&param.JobName=" + jobName + "&param.JobNumber=" +Integer.parseInt(jobNumber) + "&param.AppPath=" + appPath);
        System.out.println(response);
        myResponse = new JSONObject(response.toString());
        System.out.println(myResponse.get("executionId"));
        // Read JSON response and print
        response = APIPostRequest("https://" + cloudUrl + "/services/executions/" + myResponse.get("executionId") + "?operation=status&securityToken=" + securityToken);
        myResponse = new JSONObject(response.toString());
        System.out.println(myResponse);
        System.out.println(myResponse.get("status"));

        while (!myResponse.get("status").toString().equalsIgnoreCase("Completed")) {
            System.out.println("Waiting for Script completion. Script Status = " + myResponse.get("status"));
            Thread.sleep(5000);
            response = APIPostRequest("https://" + cloudUrl + "/services/executions/" + myResponse.get("executionId") + "?operation=status&securityToken=" + securityToken);
            myResponse = new JSONObject(response.toString());
        }

        response = APIPostRequest("https://" + cloudUrl + "/services/reports/" + myResponse.get("reportKey") + "?operation=download&securityToken=" + securityToken + "&responseFormat=json");

       //response = APIPostRequest("https://demo.perfectomobile.com/services/reports/" + "PRIVATE:230425/DBankScriptLess_23-04-25_14_22_30_1589.xml" + "?operation=download&securityToken=eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4YmI4YmZmZS1kMzBjLTQ2MjctYmMxMS0zNTYyMmY1ZDkyMGYifQ.eyJpYXQiOjE2NzUyMjkzNTIsImp0aSI6IjQ2NWE3N2QxLTIwZDktNDdmYy04MTZlLWRiM2MwY2QzNmI2YyIsImlzcyI6Imh0dHBzOi8vYXV0aC5wZXJmZWN0b21vYmlsZS5jb20vYXV0aC9yZWFsbXMvZGVtby1wZXJmZWN0b21vYmlsZS1jb20iLCJhdWQiOiJodHRwczovL2F1dGgucGVyZmVjdG9tb2JpbGUuY29tL2F1dGgvcmVhbG1zL2RlbW8tcGVyZmVjdG9tb2JpbGUtY29tIiwic3ViIjoiZDQ0Y2Q5ZjctYmY3Mi00M2YwLWFjYjgtNzcyNDdkMjFhMDMyIiwidHlwIjoiT2ZmbGluZSIsImF6cCI6Im9mZmxpbmUtdG9rZW4tZ2VuZXJhdG9yIiwibm9uY2UiOiJlYzY3YzcwMy1lOTE4LTQ2ZTUtODc2ZS05MmYyYTRmNzA3YWUiLCJzZXNzaW9uX3N0YXRlIjoiOWE4MzhlNGMtYTY5OC00YjYxLTliZmEtNmEzZjUwMjYwYTE4Iiwic2NvcGUiOiJvcGVuaWQgb2ZmbGluZV9hY2Nlc3MifQ.KLGLiHd0tDQj4i0sxQ15gKEUzZhdn00E9TtZLhcd2F0&responseFormat=json");

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(response.toString())));

        JSONObject jsonResponse = XML.toJSONObject(String.valueOf(response));
        JSONObject executions = (JSONObject) jsonResponse.get("execution");

        JSONObject input = ((JSONObject) (((JSONObject) executions.get("input")).getJSONObject("handsets")).get("handset")).getJSONObject("properties");

        JSONArray handsetDetails = input.getJSONArray("property");

        for(int k=0; k < handsetDetails.length(); k++ ){
            JSONObject x = (JSONObject) handsetDetails.get(k);
            if(x.get("name").toString().contains("manufacturer")) {
                dManufacturer = (String) x.get("value");
            }
            if(x.get("name").toString().contains("model")) {
                dModel = (String) x.get("value");
            }
            if(x.get("name").toString().contains("osVersion")) {
                osVersion = x.get("value").toString();
            }
        }


        JSONObject flow = (JSONObject) executions.get("flow");

        JSONArray timersteps = (JSONArray) flow.get("step");

        System.out.println(timersteps.length());

        for(int i =0; i < timersteps.length(); i++){
            JSONObject timer = (JSONObject) ((JSONObject) timersteps.get(i)).get("info");
            String transactionName = (String) timer.get("name");
            JSONObject transacTimes = (((JSONObject) timer.get("times")).getJSONObject("flowTimes")).getJSONObject("end");
            long transactionePochTime = (long) transacTimes.get("millis");
            System.out.println(transactionName + transactionePochTime);
            epochTime[i] = String.valueOf(transactionePochTime);
        }

        NodeList errNodes = doc.getElementsByTagName("timers");
        if (errNodes.getLength() > 0) {
            Element err = (Element) errNodes.item(0);
            System.out.println(errNodes.item(0));
            System.out.println(err.getAttributes());

            System.out.println(err.getElementsByTagName("timer")
                    .item(0)
                    .getTextContent());

            NodeList x = err.getElementsByTagName("timer");
            for (int i = 0; i < x.getLength(); i++) {

                System.out.println(((Element) x.item(i)).getAttribute("id"));

                NodeList x1 = ((Element) x.item(i)).getElementsByTagName("time");

                if(!((Element) x.item(i)).getAttribute("id").toString().equalsIgnoreCase("auto")) {
                    System.out.println(dManufacturer + "_" + dModel + "_OS_" + osVersion);
                    metricDescription[index][1] = dManufacturer + "_" + dModel + "_OS_" + osVersion + "/" + ((Element) x.item(i)).getAttribute("id");
                    for (int j = 0; j < x1.getLength(); j++) {
                        System.out.println(((Element) x1.item(j)).getAttribute("label") + "  =  " + ((Element) x1.item(j)).getTextContent());
                        if(((Element) x1.item(j)).getAttribute("label").equalsIgnoreCase("ux")){
                            double uxTime = Double.parseDouble(((Element) x1.item(j)).getTextContent()) * 1000;
                            metricValue[index][j] = String.valueOf(uxTime);
                        }
                    }
                    index++;
                }
            }
        }

        System.out.println(epochTime);
        System.out.println(metricValue);
        System.out.println(metricDescription);


//        NodeList flow = doc.getElementsByTagName("flow");
//        System.out.println(flow.getLength());
//        Element step = (Element) flow.item(3);

    }

    private static StringBuffer APIPostRequest(String URL) {
        StringBuffer response = new StringBuffer();
        JSONObject myResponse = null;
        try {
            URL url = new URL(URL);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json");
            http.setRequestProperty("Authorization", "Basic " );
            http.setRequestProperty("Content-Length", "0");

            System.out.println(http.getResponseCode() + " " + http.getResponseMessage());

            // We need to extract the job number from the returned JSON string
            BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            http.disconnect();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return response;
        }
    }

}
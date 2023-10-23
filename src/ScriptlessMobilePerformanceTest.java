import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ScriptlessMobilePerformanceTest {

    private static String securityToken = "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4YmI4YmZmZS1kMzBjLTQ2MjctYmMxMS0zNTYyMmY1ZDkyMGYifQ.eyJpYXQiOjE2NTc1NjMyODAsImp0aSI6IjZiZTBkMTY0LWEzODMtNGUwZC1iYmJjLWM0YjJiZjBmNTg0YyIsImlzcyI6Imh0dHBzOi8vYXV0aC5wZXJmZWN0b21vYmlsZS5jb20vYXV0aC9yZWFsbXMvZGVtby1wZXJmZWN0b21vYmlsZS1jb20iLCJhdWQiOiJodHRwczovL2F1dGgucGVyZmVjdG9tb2JpbGUuY29tL2F1dGgvcmVhbG1zL2RlbW8tcGVyZmVjdG9tb2JpbGUtY29tIiwic3ViIjoiNjEwM2FhZjktOTdkNC00YjgwLThmZTYtZDNhYmRlNTJhM2JiIiwidHlwIjoiT2ZmbGluZSIsImF6cCI6Im9mZmxpbmUtdG9rZW4tZ2VuZXJhdG9yIiwibm9uY2UiOiIwNGM3YzA2Yi02MTVhLTRhZGUtOGYwZi1jMTUzZDdmYWRiYjYiLCJzZXNzaW9uX3N0YXRlIjoiNjM3ZGQyNTEtNWI3ZS00NTlkLTk5MDgtZDY3ZWZhYmE5YjEzIiwic2NvcGUiOiJvcGVuaWQgb2ZmbGluZV9hY2Nlc3MifQ.guCb3NHpOboo1SGBE93gp2w0uknKl7D6jrCDUmw-YGQ";
    private static String cloudUrl = "demo.perfectomobile.com";

    public static String blazeJobStatus = "false";
    public static int blazeJobId = 67675779;

    String testNumber = "12903279";
    // Encode your Blazemeter API key
    static String APIkey = "ZDYxNDdkYWEwZTI3ODcwOGNkMjcyMjA4OmJjYzg0MzI4NjU0YjBkZWVhMWI5ODY2YzYwOTUxMDE0N2ZiYzZjNDcxYWI5ZjUwMmVlNDlmNWFlMmYzODhiMzAyZDJiNjMxNg==";

    // Some variables that need to live outside code blocks
    int jobID = 0;
    int index = 0;

    @Test
    public void InitiateBlazeTest(){
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
                blazeJobStatus = "true";
                blazeJobId = jobID;
                System.out.println("jobID: " + blazeJobId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Now check the status until it gives "DATA_RECEIVED" which
            // means the BlazeMeter job is now running

            do {
                //ReportUtils.logStepStart("Checking Job Status...");
                try {
                    //URL url = new URL("https://a.blazemeter.com/api/v4/masters/" + blazeJobId + "/status?level=DEBUG");
                    response = APIGetRequest("https://a.blazemeter.com/api/v4/masters/" + blazeJobId + "/status?level=DEBUG");

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
    @Parameters({"scriptKey", "DUT", "testName", "jobName", "jobNumber", "appPath","networkProfile", "backgroundAppList"})
    public void DbankClientTest(String scriptKey, String DUT, String testName, String jobName, String jobNumber, String appPath, String networkProfile, String backgroundAppList ) throws InterruptedException, ParserConfigurationException, IOException, SAXException {
        int index = 0;
        int totalTransaction = 0;
        String jobStatus = "";
        int currentIndex = 0;
        System.out.println("I am here");
        StringBuffer response = new StringBuffer();
        JSONObject myResponse = null;
        String[] epochTime = new String[20];
        String[][] metricDescription = new String[20][20];
        String[][] metricValue = new String[20][20];
        String dManufacturer = null, dModel = null, osVersion = null;

        do {

            response = APIPostRequest("https://" + cloudUrl + "/services/executions?operation=execute&scriptKey="
                    + scriptKey + "&securityToken="
                    + securityToken + "&param.DUT=" + DUT
                    + "&param.TestName=" + testName
                    + "&param.JobName=" + System.getProperty("reportium-job-name")
                    + "&param.JobNumber=" + Integer.parseInt(System.getProperty("reportium-job-number"))
                    + "&param.AppPath=" + appPath
                    + "&param.BackGroundAppNames=" + backgroundAppList
                    + "&param.NetworkProfile=" + networkProfile);


            System.out.println(response);
            myResponse = new JSONObject(response.toString());
            System.out.println(myResponse.get("executionId"));
            // Read JSON response and print
            response = APIPostRequest("https://" + cloudUrl + "/services/executions/"
                    + myResponse.get("executionId")
                    + "?operation=status&securityToken=" + securityToken);
            myResponse = new JSONObject(response.toString());
            System.out.println(myResponse);
            System.out.println(myResponse.get("status"));

            while (!myResponse.get("status").toString().equalsIgnoreCase("Completed")) {
                System.out.println("Waiting for Script completion. Script Status = " + myResponse.get("status"));
                Thread.sleep(5000);
                response = APIPostRequest("https://" + cloudUrl + "/services/executions/"
                        + myResponse.get("executionId")
                        + "?operation=status&securityToken=" + securityToken);
                myResponse = new JSONObject(response.toString());
            }

            response = APIPostRequest("https://" + cloudUrl + "/services/reports/"
                    + myResponse.get("reportKey")
                    + "?operation=download&securityToken=" + securityToken + "&responseFormat=json");

          // response = APIPostRequest("https://demo.perfectomobile.com/services/reports/" + "PRIVATE:230430/DBankScriptLess_23-04-30_06_48_18_4161.xml" + "?operation=download&securityToken=eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4YmI4YmZmZS1kMzBjLTQ2MjctYmMxMS0zNTYyMmY1ZDkyMGYifQ.eyJpYXQiOjE2NzUyMjkzNTIsImp0aSI6IjQ2NWE3N2QxLTIwZDktNDdmYy04MTZlLWRiM2MwY2QzNmI2YyIsImlzcyI6Imh0dHBzOi8vYXV0aC5wZXJmZWN0b21vYmlsZS5jb20vYXV0aC9yZWFsbXMvZGVtby1wZXJmZWN0b21vYmlsZS1jb20iLCJhdWQiOiJodHRwczovL2F1dGgucGVyZmVjdG9tb2JpbGUuY29tL2F1dGgvcmVhbG1zL2RlbW8tcGVyZmVjdG9tb2JpbGUtY29tIiwic3ViIjoiZDQ0Y2Q5ZjctYmY3Mi00M2YwLWFjYjgtNzcyNDdkMjFhMDMyIiwidHlwIjoiT2ZmbGluZSIsImF6cCI6Im9mZmxpbmUtdG9rZW4tZ2VuZXJhdG9yIiwibm9uY2UiOiJlYzY3YzcwMy1lOTE4LTQ2ZTUtODc2ZS05MmYyYTRmNzA3YWUiLCJzZXNzaW9uX3N0YXRlIjoiOWE4MzhlNGMtYTY5OC00YjYxLTliZmEtNmEzZjUwMjYwYTE4Iiwic2NvcGUiOiJvcGVuaWQgb2ZmbGluZV9hY2Nlc3MifQ.KLGLiHd0tDQj4i0sxQ15gKEUzZhdn00E9TtZLhcd2F0&responseFormat=json");

//            if(index == 0)
//                response = APIPostRequest("https://demo.perfectomobile.com/services/reports/" + "PRIVATE:230430/DBankScriptLess_23-04-30_07_23_50_4183.xml" + "?operation=download&securityToken=eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4YmI4YmZmZS1kMzBjLTQ2MjctYmMxMS0zNTYyMmY1ZDkyMGYifQ.eyJpYXQiOjE2NzUyMjkzNTIsImp0aSI6IjQ2NWE3N2QxLTIwZDktNDdmYy04MTZlLWRiM2MwY2QzNmI2YyIsImlzcyI6Imh0dHBzOi8vYXV0aC5wZXJmZWN0b21vYmlsZS5jb20vYXV0aC9yZWFsbXMvZGVtby1wZXJmZWN0b21vYmlsZS1jb20iLCJhdWQiOiJodHRwczovL2F1dGgucGVyZmVjdG9tb2JpbGUuY29tL2F1dGgvcmVhbG1zL2RlbW8tcGVyZmVjdG9tb2JpbGUtY29tIiwic3ViIjoiZDQ0Y2Q5ZjctYmY3Mi00M2YwLWFjYjgtNzcyNDdkMjFhMDMyIiwidHlwIjoiT2ZmbGluZSIsImF6cCI6Im9mZmxpbmUtdG9rZW4tZ2VuZXJhdG9yIiwibm9uY2UiOiJlYzY3YzcwMy1lOTE4LTQ2ZTUtODc2ZS05MmYyYTRmNzA3YWUiLCJzZXNzaW9uX3N0YXRlIjoiOWE4MzhlNGMtYTY5OC00YjYxLTliZmEtNmEzZjUwMjYwYTE4Iiwic2NvcGUiOiJvcGVuaWQgb2ZmbGluZV9hY2Nlc3MifQ.KLGLiHd0tDQj4i0sxQ15gKEUzZhdn00E9TtZLhcd2F0&responseFormat=json");
//            if(index == 1)
//                response = APIPostRequest("https://demo.perfectomobile.com/services/reports/" + "PRIVATE:230430/DBankScriptLess_23-04-30_06_45_37_4158.xml" + "?operation=download&securityToken=eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4YmI4YmZmZS1kMzBjLTQ2MjctYmMxMS0zNTYyMmY1ZDkyMGYifQ.eyJpYXQiOjE2NzUyMjkzNTIsImp0aSI6IjQ2NWE3N2QxLTIwZDktNDdmYy04MTZlLWRiM2MwY2QzNmI2YyIsImlzcyI6Imh0dHBzOi8vYXV0aC5wZXJmZWN0b21vYmlsZS5jb20vYXV0aC9yZWFsbXMvZGVtby1wZXJmZWN0b21vYmlsZS1jb20iLCJhdWQiOiJodHRwczovL2F1dGgucGVyZmVjdG9tb2JpbGUuY29tL2F1dGgvcmVhbG1zL2RlbW8tcGVyZmVjdG9tb2JpbGUtY29tIiwic3ViIjoiZDQ0Y2Q5ZjctYmY3Mi00M2YwLWFjYjgtNzcyNDdkMjFhMDMyIiwidHlwIjoiT2ZmbGluZSIsImF6cCI6Im9mZmxpbmUtdG9rZW4tZ2VuZXJhdG9yIiwibm9uY2UiOiJlYzY3YzcwMy1lOTE4LTQ2ZTUtODc2ZS05MmYyYTRmNzA3YWUiLCJzZXNzaW9uX3N0YXRlIjoiOWE4MzhlNGMtYTY5OC00YjYxLTliZmEtNmEzZjUwMjYwYTE4Iiwic2NvcGUiOiJvcGVuaWQgb2ZmbGluZV9hY2Nlc3MifQ.KLGLiHd0tDQj4i0sxQ15gKEUzZhdn00E9TtZLhcd2F0&responseFormat=json");
//            else
//                response = APIPostRequest("https://demo.perfectomobile.com/services/reports/" + "PRIVATE:230430/DBankScriptLess_23-04-30_06_48_18_4161.xml" + "?operation=download&securityToken=eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI4YmI4YmZmZS1kMzBjLTQ2MjctYmMxMS0zNTYyMmY1ZDkyMGYifQ.eyJpYXQiOjE2NzUyMjkzNTIsImp0aSI6IjQ2NWE3N2QxLTIwZDktNDdmYy04MTZlLWRiM2MwY2QzNmI2YyIsImlzcyI6Imh0dHBzOi8vYXV0aC5wZXJmZWN0b21vYmlsZS5jb20vYXV0aC9yZWFsbXMvZGVtby1wZXJmZWN0b21vYmlsZS1jb20iLCJhdWQiOiJodHRwczovL2F1dGgucGVyZmVjdG9tb2JpbGUuY29tL2F1dGgvcmVhbG1zL2RlbW8tcGVyZmVjdG9tb2JpbGUtY29tIiwic3ViIjoiZDQ0Y2Q5ZjctYmY3Mi00M2YwLWFjYjgtNzcyNDdkMjFhMDMyIiwidHlwIjoiT2ZmbGluZSIsImF6cCI6Im9mZmxpbmUtdG9rZW4tZ2VuZXJhdG9yIiwibm9uY2UiOiJlYzY3YzcwMy1lOTE4LTQ2ZTUtODc2ZS05MmYyYTRmNzA3YWUiLCJzZXNzaW9uX3N0YXRlIjoiOWE4MzhlNGMtYTY5OC00YjYxLTliZmEtNmEzZjUwMjYwYTE4Iiwic2NvcGUiOiJvcGVuaWQgb2ZmbGluZV9hY2Nlc3MifQ.KLGLiHd0tDQj4i0sxQ15gKEUzZhdn00E9TtZLhcd2F0&responseFormat=json");

            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(response.toString())));

            JSONObject jsonResponse = XML.toJSONObject(String.valueOf(response));
            JSONObject executions = (JSONObject) jsonResponse.get("execution");

            JSONObject startTime = ((JSONObject) (((JSONObject) executions.get("info")).getJSONObject("times")).get("flowTimes")).getJSONObject("start");

            long startTimeePochTime = (long) startTime.get("millis") / 1000;
            System.out.println("ScriptStart_EpochTime" + startTimeePochTime);
            epochTime[index] = String.valueOf(startTimeePochTime);

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

//            for(int i =0; i < timersteps.length(); i++){
//                JSONObject timer = (JSONObject) ((JSONObject) timersteps.get(i)).get("info");
//                String transactionName = (String) timer.get("name");
//                JSONObject transacTimes = (((JSONObject) timer.get("times")).getJSONObject("flowTimes")).getJSONObject("end");
//                long transactionePochTime = (long) transacTimes.get("millis") / 1000;
//                System.out.println(transactionName + transactionePochTime);
//                epochTime[index][i] = String.valueOf(transactionePochTime);
//            }

            NodeList errNodes = doc.getElementsByTagName("timers");
            if (errNodes.getLength() > 0) {
                Element err = (Element) errNodes.item(0);
                System.out.println(errNodes.item(0));
                System.out.println(err.getAttributes());

                System.out.println(err.getElementsByTagName("timer")
                        .item(0)
                        .getTextContent());

                NodeList x = err.getElementsByTagName("timer");
                totalTransaction = x.getLength() - 1;
                int count = 0;
                for (int i = 0; i < x.getLength(); i++) {

                    System.out.println(((Element) x.item(i)).getAttribute("id"));

                    NodeList x1 = ((Element) x.item(i)).getElementsByTagName("time");

                    if(!((Element) x.item(i)).getAttribute("id").toString().equalsIgnoreCase("auto")) {
                        System.out.println(dManufacturer + "_" + dModel + "_OS_" + osVersion);
                        metricDescription[index][count] = dManufacturer + "_" + dModel + "_OS_" + osVersion + "_" + networkProfile + "_" +"/" + ((Element) x.item(i)).getAttribute("id");
                        for (int j = 0; j < x1.getLength(); j++) {
                            System.out.println(((Element) x1.item(j)).getAttribute("label") + "  =  " + ((Element) x1.item(j)).getTextContent());
                            if(((Element) x1.item(j)).getAttribute("label").equalsIgnoreCase("ux")){
                                double uxTime = Double.parseDouble(((Element) x1.item(j)).getTextContent()) * 1000;
                                metricValue[index][count] = String.valueOf(uxTime);
                            }
                        }
                        count++;
                    }
                }
                index++;
            }

            System.out.println(epochTime);
            System.out.println(metricValue);
            System.out.println(metricDescription);

            File csvOutputFile = new File("./m5_" + dManufacturer + "_" + dModel + "_OS_" + osVersion + "_" + index + ".json");

            // Build the JSON string.  Loop through the metrics and add them to the string
            String JSONString = "{\"intervals\":[";

            //int ctr = 1;
            for (int ctr = currentIndex; ctr < index; ctr++) {
                for (int ctr2 = 0; ctr2 < totalTransaction; ctr2++){
                    JSONString = JSONString + "{\"_id\":{\"masterId\":"+ blazeJobId + ",\"metricPath\":\"";
                    JSONString = JSONString + metricDescription[ctr][ctr2] + "\",\"ts\":" + epochTime[ctr];
                    JSONString = JSONString + "},\"kpis\":[{\"value\":" + metricValue[ctr][ctr2];
                    JSONString = JSONString + ",\"ts\":" + epochTime[ctr] + "}],\"profileName\":\"Mobile_" + dManufacturer + "_" + dModel + "_OS_" + osVersion + "_" + networkProfile + "_" +"\"}";
                    // If this isn't the last line, we need to add a comma
                    if (ctr < index || ctr2 < index){
                        JSONString = JSONString + ",";
                    }
                }
                currentIndex = ctr+1;
            }
            //remove the unwatend comma at the end.
            JSONString = JSONString.substring(0, JSONString.length() - 1);
            JSONString = JSONString + "]}";

            System.out.println(JSONString);

            try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
                pw.println(JSONString);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // Now that we have the JSON string, push it to BlazeMeter
            try { URL url = new URL("https://a.blazemeter.com/api/v4/data/timeseries");
                System.out.println("Injection Start -------> \n" + JSONString);
                injectBlazeData("https://a.blazemeter.com/api/v4/data/timeseries", JSONString);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            // Check the BlazeMeter job status
            try {
                URL url = new URL(
                        "https://a.blazemeter.com/api/v4/masters/" + blazeJobId + "/status?level=DEBUG");
                response = APIGetRequest("https://a.blazemeter.com/api/v4/masters/" + blazeJobId + "/status?level=DEBUG");

                myResponse = new JSONObject(response.toString());
                JSONObject result = myResponse.getJSONObject("result");
                jobStatus = (String) result.get("status");
                System.out.println("jobStatus:" + jobStatus);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //        NodeList flow = doc.getElementsByTagName("flow");
        //        System.out.println(flow.getLength());
        //        Element step = (Element) flow.item(3);

    } while (jobStatus.equals("DATA_RECEIVED"));
    //} while (index != 3);
        // While the job is no longer actively running, we need to wait until it has actually ended
        // So we loop until the status is "ENDED"

        do {
            //ReportUtils.logStepStart("Checking for Job Ended...");
            try {
                URL url = new URL("https://a.blazemeter.com/api/v4/masters/" + blazeJobId + "/status?level=DEBUG");
                response = APIGetRequest("https://a.blazemeter.com/api/v4/masters/" + blazeJobId + "/status?level=DEBUG");

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

        } while (!jobStatus.equals("ENDED"));
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
            http.setRequestProperty("Authorization", "Basic " + APIkey);
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

    private static StringBuffer APIGetRequest(String URL) {
        StringBuffer response = new StringBuffer();
        JSONObject myResponse = null;
        try {
            URL url = new URL(URL);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("GET");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json");
            http.setRequestProperty("Authorization", "Basic " + APIkey);
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

    private static void injectBlazeData(String URL, String JSONString) {
        StringBuffer response = new StringBuffer();
        JSONObject myResponse = null;
        try { URL url = new URL("https://a.blazemeter.com/api/v4/data/timeseries");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST"); http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json");
            http.setRequestProperty("Authorization", "Basic " + APIkey);
            http.setRequestProperty("Content-Length", "0");
            http.setDoOutput(true);

            try(OutputStream os = http.getOutputStream()) {
                byte[] input = JSONString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            System.out.println(http.getResponseCode() + " " + http.getResponseMessage());

            http.disconnect();
        }
        catch (Exception e)
        { e.printStackTrace();
        }
    }
}
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import com.thoughtworks.gauge.Gauge;
import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;
import com.thoughtworks.gauge.datastore.DataStore;
import com.thoughtworks.gauge.datastore.DataStoreFactory;
import io.restassured.RestAssured;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.json.JSONException;
import org.testng.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

public abstract class BaseClass {

    public BaseClass() {
    }

    private Response response;
    private String accessTokenOfUser = "";
    public String AUTHORIZATION_HEADER_NAME = System.getenv("header_name_for_authorization");
    public String TOKEN_TYPE = "Bearer ";
    private RequestSpecification request = this.getRequestSpecification();
    CookieFilter cookieFilter = new CookieFilter();


    public void printApiEndpoint(String apiEndpoint) {
        System.out.println("API Endpoint is: \n" + apiEndpoint);
        Gauge.writeMessage("API Endpoint is: \n" + apiEndpoint);
    }

    public void printResponse() {

        String response_as_a_json_string = this.response.prettyPrint().toString();
        if (response_as_a_json_string.equals("")) {
            System.out.println("Response is empty for the given payload");
            Gauge.writeMessage("Response is empty for the given payload");
        } else if (response_as_a_json_string.equals("[]")) {
            System.out.println("Response is null for the given payload");
            Gauge.writeMessage("Response is null for the given payload");
        } else {
            Gauge.writeMessage("Response is: \n" + response_as_a_json_string);
        }

    }

    public String getQueryParams() {
        String queryParams = String.valueOf(getSavedValueForScenario("queryParams"));
        if (queryParams.equals("") || queryParams.equals("null")) {
            queryParams = "";
        }

        return queryParams;
    }

    public String getPathParams() {
        String pathParams = String.valueOf(getSavedValueForScenario("pathParams"));
        if (pathParams.equals("") || pathParams.equals("null")) {
            pathParams = "";
        }

        return pathParams;
    }

    public String getResponse() {

        String responseAsString = this.response.prettyPrint();
        saveValueForScenario("response", responseAsString);
        saveValueForSpecification("response", responseAsString);
        return responseAsString;
    }

    public RequestSpecification getRequestSpecification() {
        return RestAssured.given().contentType(ContentType.JSON);
    }

    public void apiToBeInvoked(String apiEndpointName) throws IOException {
        saveValueForScenario("API_NAME", apiEndpointName);
        this.printApiEndpoint(ApiEndpoints.getApiEndpointByName(apiEndpointName));
    }

    public void invokeConfiguredApi(String jsonPayload, List<Header> headerList) throws IOException {
        String apiName = getSavedValueForScenario("API_NAME");
        System.out.println("You are going to invoked a " + GetApiEndpointDataFromExel.getHttpMethod(apiName));
        accessTokenOfUser = "";

        try {

            accessTokenOfUser = setAccessToken(getSavedValueForScenario("token_number"));

        }catch (NullPointerException e){

            accessTokenOfUser = "";

        }

        if (GetApiEndpointDataFromExel.getHttpMethod(apiName).equals("GET")) {
            this.getAPIWithAuthMultipleHeaders(accessTokenOfUser, headerList);
        }

        if (GetApiEndpointDataFromExel.getHttpMethod(apiName).equals("POST")) {
            this.postAPIWithAuthMultipleHeaders(jsonPayload, accessTokenOfUser, headerList);
        }

        if (GetApiEndpointDataFromExel.getHttpMethod(apiName).equals("PUT")) {
            this.putAPIWithAuthMultipleHeaders(jsonPayload, accessTokenOfUser, headerList);
        }

        if (GetApiEndpointDataFromExel.getHttpMethod(apiName).equals("DELETE")) {
            this.deleteAPIWithAuthMultipleHeaders(jsonPayload, accessTokenOfUser, headerList);
        }
        if (GetApiEndpointDataFromExel.getHttpMethod(apiName).equals("PATCH")) {
            this.patchAPIWithAuthMultipleHeaders(jsonPayload, accessTokenOfUser, headerList);
        }
    }


    public void getAPIWithAuthMultipleHeaders(String accessToken, List<Header> headerList) throws IOException {

        String apiName = getSavedValueForScenario("API_NAME");
        String invokingEndpoint;
        if(System.getenv("ENVIRONMENT").contentEquals("QA")){

            String endpoint = ApiEndpoints.getApiEndpointByName(apiName).concat(this.getPathParams().concat(this.getQueryParams()));
            invokingEndpoint = endpoint.replaceAll("dev", "qa");


        }else {

            invokingEndpoint = ApiEndpoints.getApiEndpointByName(apiName).concat(getPathParams().concat(getQueryParams()));

        }


        System.out.println("Invoked API Endpoint: \n" + invokingEndpoint);
        Gauge.writeMessage("Invoked API Endpoint: \n" + invokingEndpoint);
        if (null != accessToken) {

            System.out.println("AccessToken Is: \n" + accessToken);

        } else {
            System.out.println("AccessToken Is Empty");
        }
        Headers headers = new Headers(headerList);

        if (headers.size() == 0) {
            if (accessToken.contentEquals("")) {
                this.response = (Response) RestAssured.given().when().urlEncodingEnabled(false).get(invokingEndpoint, new Object[0]);
            } else {
                this.response = (Response) RestAssured.given().urlEncodingEnabled(false).header(this.AUTHORIZATION_HEADER_NAME, accessToken).when().get(invokingEndpoint);
            }
        } else {
            if (accessToken.contentEquals("")) {
                this.response = (Response) RestAssured.given().urlEncodingEnabled(false).headers(headers).when().get(invokingEndpoint, new Object[0]);
            } else {
                this.response = (Response) RestAssured.given().urlEncodingEnabled(false).header(this.AUTHORIZATION_HEADER_NAME, accessToken).headers(headers).when().get(invokingEndpoint);

            }

        }
        this.getStatusCode();
        this.getResponse();
        this.printResponse();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void postAPIWithAuthMultipleHeaders(String jsonPayload, String accessToken, List<Header> headerList) throws IOException {

        String apiName = getSavedValueForScenario("API_NAME");
        String invokingEndpoint;
        if(System.getenv("ENVIRONMENT").contentEquals("QA")){

            String endpoint = ApiEndpoints.getApiEndpointByName(apiName).concat(this.getPathParams().concat(this.getQueryParams()));
            invokingEndpoint = endpoint.replaceAll("dev", "qa");

        }else {

            invokingEndpoint = ApiEndpoints.getApiEndpointByName(apiName).concat(this.getPathParams().concat(this.getQueryParams()));

        }


        System.out.println("Invoked API Endpoint: \n" + invokingEndpoint);
        if (null != accessToken) {

            System.out.println("AccessToken Is: \n" + accessToken);

        } else {
            System.out.println("AccessToken Is Empty");
        }
        Gauge.writeMessage("Invoked API Endpoint: \n" + invokingEndpoint);
        Headers headers = new Headers(headerList);
        if (headers.size() == 0) {
            if (accessToken.contentEquals("")) {
                this.response = (Response) RestAssured.given().contentType(ContentType.JSON).body(jsonPayload).when().post(invokingEndpoint);
            } else {
                this.response = (Response) RestAssured.given().urlEncodingEnabled(false).header(this.AUTHORIZATION_HEADER_NAME, accessToken).contentType(ContentType.JSON).body(jsonPayload).when().post(invokingEndpoint);
            }
        } else {
            if (accessToken.contentEquals("")) {
                this.response = (Response) RestAssured.given().headers(headers).body(jsonPayload).when().post(invokingEndpoint);
            } else {

                this.response = (Response) RestAssured.given().urlEncodingEnabled(false).header(this.AUTHORIZATION_HEADER_NAME, accessToken).headers(headers).body(jsonPayload).when().post(invokingEndpoint);
            }
        }
        this.getStatusCode();
        this.getResponse();
        this.printResponse();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void putAPIWithAuthMultipleHeaders(String jsonPayload, String accessToken, List<Header> headerList) throws IOException {
        String apiName = getSavedValueForScenario("API_NAME");

        String invokingEndpoint;
        if(System.getenv("ENVIRONMENT").contentEquals("QA")){

            String endpoint = ApiEndpoints.getApiEndpointByName(apiName).concat(this.getPathParams().concat(this.getQueryParams()));
            invokingEndpoint = endpoint.replaceAll("dev", "qa");

        }else {

            invokingEndpoint = ApiEndpoints.getApiEndpointByName(apiName).concat(this.getPathParams().concat(this.getQueryParams()));

        }

        System.out.println("Invoked API Endpoint: \n" + invokingEndpoint);
        if (null != accessToken) {

            System.out.println("AccessToken Is: \n" + accessToken);

        } else {
            System.out.println("AccessToken Is Empty");
        }
        Gauge.writeMessage("Invoked API Endpoint: \n" + invokingEndpoint);
        Headers headers = new Headers(headerList);
        if (headers.size() == 0) {
            if (accessToken.contentEquals("")) {
                this.response = (Response) RestAssured.given().contentType(ContentType.JSON).body(jsonPayload).when().put(invokingEndpoint);
            } else {
                this.response = (Response) RestAssured.given().urlEncodingEnabled(false).header(this.AUTHORIZATION_HEADER_NAME, accessToken).contentType(ContentType.JSON).body(jsonPayload).when().put(invokingEndpoint);
            }
        } else {
            if (accessToken.contentEquals("")) {
                this.response = (Response) RestAssured.given().headers(headers).body(jsonPayload).when().put(invokingEndpoint);
            } else {

                this.response = (Response) RestAssured.given().urlEncodingEnabled(false).header(this.AUTHORIZATION_HEADER_NAME, accessToken).headers(headers).body(jsonPayload).when().put(invokingEndpoint);
            }
        }
        this.getStatusCode();
        this.getResponse();
        this.printResponse();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void deleteAPIWithAuthMultipleHeaders(String jsonPayload, String accessToken, List<Header> headerList) throws IOException {
        String apiName = getSavedValueForScenario("API_NAME");

        String invokingEndpoint;
        if(System.getenv("ENVIRONMENT").contentEquals("QA")){

            String endpoint = ApiEndpoints.getApiEndpointByName(apiName).concat(this.getPathParams().concat(this.getQueryParams()));
            invokingEndpoint = endpoint.replaceAll("dev", "qa");

        }else {

            invokingEndpoint = ApiEndpoints.getApiEndpointByName(apiName).concat(this.getPathParams().concat(this.getQueryParams()));

        }

        System.out.println("Invoked API Endpoint: \n" + invokingEndpoint);
        if (null != accessToken) {

            System.out.println("AccessToken Is: \n" + accessToken);

        } else {
            System.out.println("AccessToken Is Empty");
        }
        Gauge.writeMessage("Invoked API Endpoint: \n" + invokingEndpoint);
        Headers headers = new Headers(headerList);
        if (headers.size() == 0) {
            if (accessToken.contentEquals("")) {
                this.response = (Response) RestAssured.given().contentType(ContentType.JSON).body(jsonPayload).when().delete(invokingEndpoint);
            } else {

                this.response = (Response) RestAssured.given().urlEncodingEnabled(false).header(this.AUTHORIZATION_HEADER_NAME, accessToken).contentType(ContentType.JSON).body(jsonPayload).when().delete(invokingEndpoint);
            }
        } else {
            if (accessToken.contentEquals("")) {
                this.response = (Response) RestAssured.given().headers(headers).body(jsonPayload).when().delete(invokingEndpoint);
            } else {

                this.response = (Response) RestAssured.given().urlEncodingEnabled(false).header(this.AUTHORIZATION_HEADER_NAME, accessToken).headers(headers).body(jsonPayload).when().delete(invokingEndpoint);
            }
        }
        this.getStatusCode();
        this.getResponse();
        this.printResponse();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void patchAPIWithAuthMultipleHeaders(String jsonPayload, String accessToken, List<Header> headerList) throws IOException {
        String apiName = getSavedValueForScenario("API_NAME");

        String invokingEndpoint;
        if(System.getenv("ENVIRONMENT").contentEquals("QA")){

            String endpoint = ApiEndpoints.getApiEndpointByName(apiName).concat(this.getPathParams().concat(this.getQueryParams()));
            invokingEndpoint = endpoint.replaceAll("dev", "qa");

        }else {

            invokingEndpoint = ApiEndpoints.getApiEndpointByName(apiName).concat(this.getPathParams().concat(this.getQueryParams()));

        }

        System.out.println("Invoked API Endpoint: \n" + invokingEndpoint);
        if (null != accessToken) {

            System.out.println("AccessToken Is: \n" + accessToken);

        } else {
            System.out.println("AccessToken Is Empty");
        }
        Gauge.writeMessage("Invoked API Endpoint: \n" + invokingEndpoint);
        Headers headers = new Headers(headerList);
        if (headers.size() == 0) {
            if (accessToken.contentEquals("")) {
                this.response = (Response) RestAssured.given().contentType(ContentType.JSON).body(jsonPayload).when().patch(invokingEndpoint);
            } else {

                this.response = (Response) RestAssured.given().urlEncodingEnabled(false).header(this.AUTHORIZATION_HEADER_NAME, accessToken).contentType(ContentType.JSON).body(jsonPayload).when().patch(invokingEndpoint);
            }
        } else {
            if (accessToken.contentEquals("")) {
                this.response = (Response) RestAssured.given().headers(headers).body(jsonPayload).when().patch(invokingEndpoint);
            } else {

                this.response = (Response) RestAssured.given().urlEncodingEnabled(false).header(this.AUTHORIZATION_HEADER_NAME, accessToken).headers(headers).body(jsonPayload).when().patch(invokingEndpoint);
            }
        }
        this.getStatusCode();
        this.getResponse();
        this.printResponse();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    public void getStatusCode() {
        int statusCode = this.response.statusCode();
        saveValueForScenario("statusCode", String.valueOf(statusCode));
    }


    public void verifyResponseStatusCode(String statusCode) {
        Assert.assertEquals(getSavedValueForScenario("statusCode"), statusCode, "The expected status code for the request is not match with the actual status code\n");
    }

    public void jsonPathAssertion(String jsonPath, String expectedResult) {
        Object responseString = Configuration.defaultConfiguration().jsonProvider().parse(getSavedValueForScenario("response"));
        if (responseString.toString().equals("")) {
            System.out.println("No any JSON Paths found. Because the response is empty for the given payload");
            Gauge.writeMessage("No any JSON Paths found. Because the response is empty for the given payload");
        }

        if (responseString.toString().equals("[]")) {
            System.out.println("No any JSON Paths found. Because the response is null for the given payload");
            Gauge.writeMessage("No any JSON Paths found. Because the response is null for the given payload");
        }

        String expectedResultForEmpty;
        if (jsonPath.isEmpty()){
            Assert.assertTrue(expectedResult.equals(responseString.toString()), "Found mismatches in Expected and Actual results");
        } else if (expectedResult.toLowerCase().equals("[]")) {
            expectedResultForEmpty = "[]";
            Assert.assertEquals(JsonPath.read(responseString, jsonPath, new Predicate[0]).toString(), expectedResultForEmpty, "Found mismatches in Expected and Actual results");
        } else if (expectedResult.toLowerCase().equals("null")) {
            expectedResultForEmpty = null;
            Assert.assertEquals(JsonPath.read(responseString, jsonPath, new Predicate[0]), expectedResultForEmpty, "Found mismatches in Expected and Actual results");
        } else if (expectedResult.toLowerCase().equals("true")) {
            Assert.assertEquals(JsonPath.read(responseString, jsonPath, new Predicate[0]), Boolean.TRUE, "Found mismatches in Expected and Actual results");
        } else if (expectedResult.toLowerCase().equals("false")) {
            Assert.assertEquals(JsonPath.read(responseString, jsonPath, new Predicate[0]), Boolean.FALSE, "Found mismatches in Expected and Actual results");
        } else if (expectedResult.trim().equals("")) {
            expectedResultForEmpty = "";
            Assert.assertEquals(JsonPath.read(responseString, jsonPath, new Predicate[0]), expectedResultForEmpty, "Found mismatches in Expected and Actual results");
        } else if (expectedResult.matches("\\d+")) {
            Assert.assertEquals(JsonPath.read(responseString, jsonPath, new Predicate[0]).toString(), expectedResult, "Found mismatches in Expected and Actual results");
        } else if (expectedResult.matches("[-]?\\d*\\.?\\d*")) {
            Assert.assertEquals(JsonPath.read(responseString, jsonPath, new Predicate[0]), Double.valueOf(expectedResult), "Found mismatches in Expected and Actual results");
        } else if (!responseString.toString().contains("[]")) {
            Assert.assertEquals(JsonPath.read(responseString, jsonPath), expectedResult, "Found mismatches in Expected and Actual results");
        } else {
            Assert.assertEquals(StringUtils.strip(String.valueOf(JsonPath.read(responseString, jsonPath, new Predicate[0])), "\"[]"), expectedResult, "Found mismatches in Expected and Actual results");
        }


    }


    public void checkTheResponse(Table table) {

        List rows = table.getTableRows();
        List columnNames = table.getColumnNames();
        Iterator iterator = rows.iterator();
        String body = getSavedValueForScenario("response");
        while (iterator.hasNext()) {
            TableRow row = (TableRow) iterator.next();
            if (body.contains(row.getCell((String) columnNames.get(0)))) {

                System.out.println("pass");

            } else {

                System.out.println("fail");
            }


        }
    }

    public void isNotCheckTheResponse(Table table) {
        List rows = table.getTableRows();
        List columnNames = table.getColumnNames();
        Iterator iterator = rows.iterator();
        String body = getSavedValueForScenario("response");
        while (iterator.hasNext()) {
            TableRow row = (TableRow) iterator.next();
            if (body.contains(row.getCell((String) columnNames.get(0)))) {

                Assert.fail();

            } else {

                Gauge.writeMessage("Value %s not in the response", row.getCell((String) columnNames.get(0)));
            }


        }
    }

    public void saveResponseAttributeValue(String attributeName, String variableNameOfValueToBeStoredInDataStore) throws JSONException {
        io.restassured.path.json.JsonPath jsonPathEvaluator = response.jsonPath();
        System.out.println("Attribute value: " + jsonPathEvaluator.get(attributeName).toString());
        saveValueForSpecification(variableNameOfValueToBeStoredInDataStore, jsonPathEvaluator.get(attributeName).toString());
    }


    public static String getSavedValueForScenario(String variableNameOfValueStoredInDataStore) {
        DataStore scenarioStore = DataStoreFactory.getScenarioDataStore();
        return (String) scenarioStore.get(variableNameOfValueStoredInDataStore);
    }


    public void checkTheResponseTime(String time) {

        long responseTime = this.response.then().extract().timeIn(MILLISECONDS);
        assertThat(responseTime, lessThan(Long.valueOf(time)));


    }

    public void jsonValidator(String name) throws IOException {

        String FILE_PATH = System.getProperty("user.dir") + "/" + System.getenv("json_schema_path") + name.concat(".json");
        File schemaFile = new File(FILE_PATH);
        JsonNode schemaNode = JsonLoader.fromFile(schemaFile);
        this.response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchema(String.valueOf(schemaNode)));


    }

    public static io.restassured.path.json.JsonPath getSavedValueForScenarioAsJsonPathObject(String variableNameOfValueStoredInDataStore) {
        DataStore scenarioStore = DataStoreFactory.getScenarioDataStore();
        return (io.restassured.path.json.JsonPath) scenarioStore.get(variableNameOfValueStoredInDataStore);
    }

    public static String getSavedValueForSpecification(String variableNameOfValueStoredInDataStore) {
        DataStore specDataStore = DataStoreFactory.getSpecDataStore();
        return (String) specDataStore.get(variableNameOfValueStoredInDataStore);
    }

    public static void saveValueForScenario(String variableNameOfValueToBeStoredInDataStore, String valueToBeStoredInDataStore) {
        DataStore scenarioStore = DataStoreFactory.getScenarioDataStore();
        scenarioStore.put(variableNameOfValueToBeStoredInDataStore, valueToBeStoredInDataStore);
    }

    public static void saveValueForSpecification(String variableNameOfValueToBeStoredInDataStore, String valueToBeStoredInDataStore) {
        DataStore specDataStore = DataStoreFactory.getSpecDataStore();
        specDataStore.put(variableNameOfValueToBeStoredInDataStore, valueToBeStoredInDataStore);
    }

    public static String getValueFromDataStore(String variableNameOfValueStoredInDataStore) {
        // Fetching Value from the Data Store
        DataStore scenarioStore = DataStoreFactory.getScenarioDataStore();
        return (String) scenarioStore.get(variableNameOfValueStoredInDataStore);
    }

    public static void setValueToDataStore(String variableNameOfValueToBeStoredInDataStore, String valueToBeStoredInDataStore) {
        // Adding value to the Data Store
        DataStore scenarioStore = DataStoreFactory.getScenarioDataStore();
        scenarioStore.put(variableNameOfValueToBeStoredInDataStore, valueToBeStoredInDataStore);
    }

    public String setAccessToken(String number) {

        String FILE_PATH = System.getProperty("user.dir") + "/" + System.getenv("access_token_file_path");
        String token = null;

        FileInputStream ip = null;
        try {
            ip = new FileInputStream(FILE_PATH);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Workbook wb = null;
        try {

            wb = WorkbookFactory.create(ip);


        } catch (IOException e) {
            e.printStackTrace();
        }
        Sheet sheet = wb.getSheetAt(0);

        int i, j;
        int rowcount = 13, cellcount = 1;

        for (i = 0; i <= rowcount; i++) {
            Row rows = sheet.getRow(i);

            for (j = 0; j < cellcount; j++) {
                Cell cell = rows.getCell(j);
                String cellval = cell.getStringCellValue();
                if (cellval.contentEquals(number)) {
                    cell = rows.getCell(j + 1);
                    cellval = cell.getStringCellValue();
                    token = cellval;
                }

            }

        }
        try {
            ip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return token;
    }

}

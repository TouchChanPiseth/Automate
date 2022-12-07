import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetAuthentication extends BaseClass{



    public  void getAccessToken (String msisdn, String otp){


        JSONObject requestParams = new JSONObject();
        requestParams.put("msisdn", msisdn);
        requestParams.put("otp", otp);
        requestParams.put("sessionId", getSessionUsingMsisdn(msisdn));

        List<Header> headerlist = new ArrayList<Header>();
        headerlist.add(new Header("x-source", "GPWeb"));
        headerlist.add(new Header("x-device", "PC"));
        headerlist.add(new Header("x-browser", "Chrome"));
        Headers headers = new Headers(headerlist);


        Response response = (Response) RestAssured.given().contentType(ContentType.JSON).headers(headers).body(requestParams.toString()).when().post(System.getenv("signin_verify"));
        JsonPath jsonPathEvaluator = response.jsonPath();
        String accessToken = jsonPathEvaluator.get("idToken");
        setValueToDataStore("token", accessToken);
        System.out.println(accessToken);
    }

    public String getSessionUsingMsisdn(String msisdn){

        JSONObject requestParams = new JSONObject();
        requestParams.put("msisdn", msisdn);

        List<Header> headerlist = new ArrayList<Header>();
        headerlist.add(new Header("x-source", "GPWeb"));
        headerlist.add(new Header("x-device", "PC"));
        headerlist.add(new Header("x-browser", "Chrome"));
        Headers headers = new Headers(headerlist);


        Response response = (Response) RestAssured.given().contentType(ContentType.JSON).headers(headers).body(requestParams.toString()).when().post(System.getenv("sign_in_endpoint"));
        JsonPath jsonPathEvaluator = response.jsonPath();
        String session_id = jsonPathEvaluator.get("sessionId");

        return session_id;
    }



}

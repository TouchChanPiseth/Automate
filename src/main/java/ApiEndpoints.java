import java.io.IOException;

public abstract class ApiEndpoints {

    public ApiEndpoints() {
    }

    public static String getApiEndpointByName(String apiEndpointName) throws IOException {
        return GetApiEndpointDataFromExel.getAPIEndpoint(apiEndpointName);
    }

}

import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableRow;

import java.io.IOException;

public class GetAccessToken extends BaseClass {

    GetAuthentication getAuthentication = new GetAuthentication();
    AccessTokenList accessTokenList = new AccessTokenList();

    public void getAccessToken(Table table) {


        for (TableRow row : table.getTableRows()) {

            getAuthentication.getAccessToken(row.getCell("phone_number"), row.getCell("otp_number"));

            try {
                accessTokenList.storeAccessTokens(row.getCell("phone_number"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}

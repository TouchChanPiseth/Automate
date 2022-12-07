import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class AccessTokenList extends BaseClass {
    private static final String FILE_PATH = System.getProperty("user.dir") + "/" + System.getenv("access_token_file_path");

    public void storeAccessTokens(String user) throws IOException {

        String value = getSavedValueForScenario("token");
        try {
            FileInputStream file = new FileInputStream(new File(FILE_PATH));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Cell cell = null;

            if (user.contentEquals("+85585200025")) {
                cell = sheet.getRow(0).getCell(1);
                cell.setCellValue(value);
            } else if (user.contentEquals("+85585200015")) {
                cell = sheet.getRow(1).getCell(1);
                cell.setCellValue(value);

            }
            else if (user.contentEquals("+Invalid")) {
                cell = sheet.getRow(2).getCell(1);
                cell.setCellValue(value);
            }
            else if (user.contentEquals("+85599897586")) {
                cell = sheet.getRow(3).getCell(1);
                cell.setCellValue(value);
            } else if (user.contentEquals("+85585213250")) {
                cell = sheet.getRow(4).getCell(1);
                cell.setCellValue(value);
            } else if (user.contentEquals("+85561900298")) {
                cell = sheet.getRow(5).getCell(1);
                cell.setCellValue(value);
            } else if (user.contentEquals("+85561450308")) {
                cell = sheet.getRow(6).getCell(1);
                cell.setCellValue(value);
            } else if (user.contentEquals("+85511343724")) {
                cell = sheet.getRow(7).getCell(1);
                cell.setCellValue(value);
            } else if (user.contentEquals("+85599363071")) {
                cell = sheet.getRow(8).getCell(1);
                cell.setCellValue(value);
            } else if (user.contentEquals("+85585213288")) {
                cell = sheet.getRow(9).getCell(1);
                cell.setCellValue(value);
            } else if (user.contentEquals("+85585213351")) {
                cell = sheet.getRow(10).getCell(1);
                cell.setCellValue(value);
            } else if (user.contentEquals("+85585213178")) {
                cell = sheet.getRow(11).getCell(1);
                cell.setCellValue(value);
            } else if (user.contentEquals("+855973767384")) {
                cell = sheet.getRow(12).getCell(1);
                cell.setCellValue(value);
            } else {
                cell = sheet.getRow(13).getCell(1);
                cell.setCellValue(value);
            }


            file.close();
            FileOutputStream output_file = new FileOutputStream(new File(FILE_PATH));
            workbook.write(output_file);
            output_file.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

}

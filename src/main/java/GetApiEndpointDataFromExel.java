import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

public abstract class GetApiEndpointDataFromExel {

    private static final String FILE_PATH = System.getProperty("user.dir") + "/" + System.getenv("api_document_path");
    private static String value = "";
    static int column;

    public GetApiEndpointDataFromExel(){

    }

    public static String getDataFromExcel(int row, int column) {
        try {
            FileInputStream e = new FileInputStream(new File(FILE_PATH));
            XSSFWorkbook workbook = new XSSFWorkbook(e);
            Sheet workSheet = workbook.getSheetAt(0);
            value = workSheet.getRow(row).getCell(column).getStringCellValue();
        } catch (FileNotFoundException var5) {
            var5.printStackTrace();
        } catch (IOException var6) {
            var6.printStackTrace();
        }

        return value;
    }

    public static String getAPIEndpoint(String apiEndpointName) throws IOException {
        int row = findRowNumber(apiEndpointName);
        int column = findColumnNumber(apiEndpointName) + 1;
        return getDataFromExcel(row, column);
    }
    public static String getHttpMethod(String apiEndpointName) throws IOException {
        int row = findRowNumber(apiEndpointName);
        int column = findColumnNumber(apiEndpointName) + 2;
        return getDataFromExcel(row, column);
    }

    public static String getRequestTemplate(String apiEndpointName) throws IOException {
        int row = findRowNumber(apiEndpointName);
        int column = findColumnNumber(apiEndpointName) + 3;
        return getDataFromExcel(row, column);
    }


    public static int findColumnNumber(String cellContent) throws IOException {
        FileInputStream inputStream = new FileInputStream(FILE_PATH);
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        Sheet firstSheet = workbook.getSheetAt(0);
        Iterator iterator = firstSheet.iterator();
        CellAddress columnNumber = null;

        while(true) {
            while(iterator.hasNext()) {
                Row nextRow = (Row)iterator.next();
                Iterator cellIterator = nextRow.cellIterator();

                while(cellIterator.hasNext()) {
                    Cell cell = (Cell)cellIterator.next();
                    if(cell.getCellType() == 1) {
                        String text = cell.getStringCellValue();
                        if(cellContent.equals(text)) {
                            column = cell.getColumnIndex();
                            break;
                        }
                    }
                }
            }

            workbook.close();
            return column;
        }
    }

    public static int findRowNumber(String cellContent) throws IOException {
        FileInputStream excelFile = new FileInputStream(new File(FILE_PATH));
        XSSFWorkbook workbook = new XSSFWorkbook(excelFile);
        Sheet workSheet = workbook.getSheetAt(0);
        Iterator var4 = workSheet.iterator();

        while(var4.hasNext()) {
            Row row = (Row)var4.next();
            Iterator var6 = row.iterator();

            while(var6.hasNext()) {
                Cell cell = (Cell)var6.next();
                if(cell.getCellType() == 1 && cell.getRichStringCellValue().getString().trim().equals(cellContent)) {
                    return row.getRowNum();
                }
            }
        }

        return 0;
    }

}

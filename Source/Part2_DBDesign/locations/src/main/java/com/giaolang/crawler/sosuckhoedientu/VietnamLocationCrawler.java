package com.giaolang.crawler.sosuckhoedientu;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * @author giao.lang | fb/giao.lang.bis | https://github.com/doit-now | youtube/channel/UChsPO5CLUjOWfgwjfC2Y-Wg
 * version 21.11
 */

public class VietnamLocationCrawler {

    private static final int DELAY_IN_MILLIS = 500; //nghỉ ngơi 1 giây chờ sang thao tác/hành động khác
    private static final String DRIVER_PATH = "chromedriver.exe";
    private static final String TARGET_WEBSITE = "https://tiemchungcovid19.gov.vn/portal/portal-report";

    private static WebDriver myBrowser;

    public static void main(String[] args) throws InterruptedException, IOException {

        init();                                                //1. Khởi động trang web cần lấy dữ liệu
        List<String> vnLocations = crawlVietnamLocations();    //2. Lấy về danh sách các địa giới hành chính toàn VN
        writeArrayListToFile("vn-locations.txt", vnLocations); //3. Ghi dữ liệu lấy được ra tập tin
    }

    //Khởi động trình duyệt và trang tiemchungcovid19
    //sử dụng cách mở trình duyệt dùng thư viện Boni García cho nhanh gọn
    public static void init() throws InterruptedException {

        WebDriverManager.chromedriver().setup(); //tự động download mọi thứ cần thiết về local .m2 repository
        ChromeOptions opt = new ChromeOptions();
        opt.addArguments("--incognito"); //mở Chrome ở chế độ ẩn danh

        myBrowser = new ChromeDriver(opt);
        myBrowser.manage().window().maximize();
        //CẦN bung full màn hình vì trang web responsive ở chế độ hẹp sẽ sinh cấu trúc tag khác

        myBrowser.get(TARGET_WEBSITE);
        Thread.sleep(DELAY_IN_MILLIS); //chờ chút để tải trang xong
    }

    //Khởi động trình duyệt và trang tiemchungcovid19
    //sử dụng cách mở trình duyệt kiểu truyền thống: phải kiểm tra độ tương thích version của trình duyệt và webdriver
    public static void initV1() throws InterruptedException {

        System.setProperty("webdriver.chrome.driver", DRIVER_PATH);
        ChromeOptions opt = new ChromeOptions();
        opt.addArguments("--incognito"); //mở Chrome ở chế độ ẩn danh

        myBrowser = new ChromeDriver(opt);
        myBrowser.manage().window().maximize();
        //CẦN bung full màn hình vì trang web responsive ở chế độ hẹp sẽ sinh cấu trúc tag khác

        myBrowser.get(TARGET_WEBSITE);
        Thread.sleep(DELAY_IN_MILLIS);
    }

    //Ghi một list các chuỗi ra tập tin .txt. Mỗi chuỗi ở trên 1 dòng. Ghi theo bảng mã UTF-8
    public static void writeArrayListToFile(String fileName, List<String> list) throws InterruptedException, IOException {

        System.out.println("Write file...");
        FileWriter writer = new FileWriter(fileName, StandardCharsets.UTF_8);
        for (String item : list) {
            writer.write(item + System.lineSeparator()); //xuống hàng tương thích các OS 
        }
        writer.close();
        System.out.println("Write file successfully!!!");
    }

    //Lấy danh sách tất cả Phường/Xã trực thuộc mỗi Quận/Huyện ứng với một Tỉnh/TP bất kì 
    public static List<String> crawlVietnamLocations() throws InterruptedException {
        
        String shortLocation, fullLocation; //lưu tên ghép của các địa danh
        //sortLocation: Tỉnh/TP <tab> Quận/Huyện
        //fullLocation: Tỉnh/TP <tab> Quận/Huyện <tab> Phường/Xã
        
        List<String> vnLocations = new ArrayList();
        List<String> provinceList = getProvinceList(); //1. Lấy danh sách tên các Tỉnh/TP

        for (String province : provinceList) {
            
        //for (int i = 0; i < 1; i++) { /test thử với vài Tỉnh/TP nào đó
            //String province = provinceList.get(i);
            
            List<String> districtList = getDistrictListByProvince(province); //2. Mỗi Tỉnh/TP lấy danh sách tên các Quận/Huyện trực thuộc
            //System.out.println("Province > Districts: " + province + " : " + districtList); //In thử danh sách Quận-Huyện trực thuộc Tỉnh/TP tương ứng

            for (String district : districtList) {      //3. Lấy danh sách Phường/Xã trực thuộc mỗi Quận/Huyện ứng với Tỉnh/TP 
                List<String> wardList = getWardListByDistrict(district);
                // in thử danh sách các Phường/Xã của từng Quận/Huyện trực thuộc 1 Tỉnh/TP bất kì 
                //System.out.println("Province > District > Wards: " + province + " | " + district + " | " + wardList);

                shortLocation = province + "\t" + district; //ghép từng Tỉnh/TP với từng Quận/Huyện
                for (String ward : wardList) {
                    fullLocation = shortLocation + "\t" + ward;  //và ghép nốt với từng Phường/Xã
                    vnLocations.add(fullLocation);  //add từng Phường/Xã vào từng Quận/Huyện của từng Tỉnh/TP
                }
                //System.out.println("VN Locations: " + vnLocations); //In thử toàn bộ Phường/Xã của 1 Quận/Huyện
            }
        }

        //System.out.println("VN Locations: " + vnLocations); //In thử toàn bộ địa giới hành chính ở VN
        return vnLocations;
    }

    //Lấy danh sách các Quận/Huyện theo Tỉnh/TP bất kì và lưu lại để duyệt dần khi tìm Phường/Xã tương ứng
    public static List<String> getWardListByDistrict(String district) throws InterruptedException {

        //QUAN TRỌNG!!! Gọi lại combo Quận/Huyện để xóa danh sách Phường/Xã lần trước
        //để còn có thể render lại Phường/Xã cho lần click sau
        WebElement cboDistrict = myBrowser.findElement(By.xpath("//main[@id='main']/section[2]/div/div/form/div/div[8]/div/ng-select/div/span"));
        cboDistrict.click();
        Thread.sleep(DELAY_IN_MILLIS);

        //chọn một Quận/Huyện bất kì đưa vào ô combo Quận/Huyện và nhấn Enter    
        WebElement txtDistrict = myBrowser.findElement(By.xpath("//ng-select[@bindvalue='districtCode']//input[@role='combobox']"));
        txtDistrict.sendKeys(district);
        txtDistrict.sendKeys(Keys.ENTER);
        Thread.sleep(DELAY_IN_MILLIS);

        //click để xổ ra danh sách Phường/Xã thuộc Quận/Huyện vừa chọn
        WebElement cboWard = myBrowser.findElement(By.xpath("//main[@id='main']/section[2]/div/div/form/div/div[9]/div/ng-select/div/span"));
        cboWard.click();
        Thread.sleep(DELAY_IN_MILLIS);

        //lấy về danh sách elements, mỗi element là 1 Phường/Xã
        List<WebElement> lstWard = myBrowser.findElements(By.xpath("//ng-dropdown-panel//div[@role='option']"));

        List<String> wardList = new ArrayList();
        for (WebElement iteWard : lstWard) {
            wardList.add(iteWard.getText());  //lấy tên của Phường/Xã lưu lại vào mảng
        }
        //System.out.println("Ward List: " + wardList); //in kiểm tra danh sách Phường/Xã

        return wardList;
    }

    //Lấy danh sách các Quận/Huyện theo Tỉnh/TP bất kì và lưu lại để duyệt dần khi tìm Phường/Xã tương ứng
    public static List<String> getDistrictListByProvince(String province) throws InterruptedException {

        //QUAN TRỌNG!!! Gọi lại combo Tỉnh/TP để xóa danh sách ở combo Quận/Huyện của lần trước
        //để còn có thể render lại Quận/Huyện cho lần click sau khi đổi sang Tỉnh/TP khác      
        WebElement cboProvince = myBrowser.findElement(By.xpath("//main[@id='main']/section[2]/div/div/form/div/div[7]/div/ng-select/div/span"));
        Thread.sleep(1000);
        cboProvince.click();

        //chọn một Tỉnh/TP bất kì đưa vào ô combo Tỉnh/TP và nhấn Enter               
        WebElement txtProvince = myBrowser.findElement(By.xpath("//ng-select[@bindvalue='provinceCode']//input[@role='combobox']"));
        txtProvince.sendKeys(province);
        txtProvince.sendKeys(Keys.ENTER);
        Thread.sleep(DELAY_IN_MILLIS);

        //click combo Quận/Huyện để xổ ra danh sách Quận/Huyện thuộc Tỉnh/TP vừa chọn
        WebElement cboDistrict = myBrowser.findElement(By.xpath("//main[@id='main']/section[2]/div/div/form/div/div[8]/div/ng-select/div/span"));
        cboDistrict.click();
        Thread.sleep(DELAY_IN_MILLIS);

        //lấy về danh sách elements, mỗi element là 1 Quận/Huyện
        List<WebElement> lstDistrict = myBrowser.findElements(By.xpath("//ng-dropdown-panel//div[@role='option']"));

        //lưu lại danh sách tên các Quận/Huyện dùng cho lựa chọn sau này (List<String>)     
        List<String> districtList = new ArrayList();
        for (WebElement iteDistrict : lstDistrict) {
            districtList.add(iteDistrict.getText());
        }
        //System.out.println("District List: " + districtList); //in kiểm tra danh sách Quận/Huyện tương ứng 1 Tỉnh/TP

        return districtList;
    }

    //Lấy danh sách các Tỉnh/TP lưu lại để duyệt dần
    public static List<String> getProvinceList() throws InterruptedException {

        //click combo Tỉnh/TP để xổ ra danh sách Tỉnh/TP
        WebElement cboProvince = myBrowser.findElement(By.xpath("//main[@id='main']/section[2]/div/div/form/div/div[7]/div/ng-select/div/span"));
        cboProvince.click();
        Thread.sleep(DELAY_IN_MILLIS);

        //lấy về danh sách elements, mỗi element là 1 Tỉnh/TP
        List<WebElement> lstProvince = myBrowser.findElements(By.xpath("//ng-dropdown-panel//div[@role='option']"));

        //lưu lại danh sách tên các Tỉnh/TP dùng cho lựa chọn sau này (List<String>)        
        List<String> provinceList = new ArrayList();

        for (WebElement iteProvince : lstProvince) {
            provinceList.add(iteProvince.getText());
        }
        //System.out.println("Province List: " + provinceList); //in kiểm tra danh sách Tỉnh/TP
        cboProvince.click();  //đóng lại danh sách Tỉnh/TP

        return provinceList;
    }
}

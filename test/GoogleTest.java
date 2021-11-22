import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleTest {

    private WebDriver driver;

    @BeforeClass
    public void setupClass() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
//        WebDriverManager.firefoxdriver().setup();
//        driver = new FirefoxDriver();
    }

    @AfterTest
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void test() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("test/resources/google.properties"));
        driver.get("https://www.google.com/");
        String textToSearch = properties.getProperty("textToSearch");
        WebElement webElement = driver.findElement(By.name("q"));
        webElement.sendKeys(textToSearch);
        webElement.sendKeys(Keys.ENTER);
        String textResultStats = driver.findElement(By.xpath("//div[@id='result-stats']")).getText();
        final String regex = ".*?([\\d ]+) \\((\\d+\\,\\d+) (сек.)\\)";

        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(textResultStats);
        int resultStatsQuantity = 0;

        while (matcher.find()) {
            System.out.println("Full match: " + matcher.group(0));
            String resultFirst = matcher.group(1).replaceAll("\\s+","");
            resultStatsQuantity = Integer.parseInt(resultFirst);
        }
        int resultsNumber = Integer.parseInt(properties.getProperty("resultsNumber"));
        Assert.assertTrue(resultsNumber<resultStatsQuantity);

        checkResults(textToSearch);
        driver.findElement(By.xpath("//span[text()='Следующая']")).click();
        checkResults(textToSearch);
    }

    private void checkResults(String textToSearch) {
        int elementsQuantity = driver.findElements(By.xpath("//div[@class='g']")).size();
        for (int i = 0; i<elementsQuantity; i++){
            String textElement = driver.findElements(By.xpath("//div[@class='g']")).get(i).getText().toLowerCase();
            Assert.assertTrue(textElement.contains(textToSearch.toLowerCase()));
        }
    }
}

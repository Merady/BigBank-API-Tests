package org.example;

import com.codeborne.selenide.Selenide;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class LoanPageUITests {

    private final String url = "https://taotlus.bigbank.ee/?amount=5000&interestRate=16.8&period=60&productName=SMALL_LOAN&loanPurpose=DAILY_SETTLEMENTS&bbmedium=small_loan";

    @BeforeMethod
    public void openPage() {
        // Open the page before each test
        open(url);
    }

    @Test
    public void verifyLoanAmountDisplayedCorrectly() {
        $("input[name='header-calculator-amount']").shouldHave(value("5,000"));
    }

    @Test
    public void verifyPeriodDisplayedCorrectly() {
        $("input[name='header-calculator-period']").shouldHave(value("60"));
    }


    @Test
    public void verifyApplyButtonIsEnabled() {
        $("button[type='submit']").shouldBe(enabled);
    }

    @Test
    public void testLoanFormSubmission() {
        // Fill out the form and submit
        // Set amount
        $("input[name='header-calculator-amount']").sendKeys(Keys.CONTROL + "a", Keys.DELETE);
        $("input[name='header-calculator-amount']").setValue("1000");

        // Set loan period
        $("input[name='header-calculator-period']").sendKeys(Keys.CONTROL + "a", Keys.DELETE);
        $("input[name='header-calculator-period']").setValue("60");

        // Submit the form
        $("button[class='bb-calculator-modal__submit-button bb-button bb-button--label bb-button--mint bb-button--md bb-button--block']").click();

    }
}

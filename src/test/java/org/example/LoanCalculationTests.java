package org.example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;

public class LoanCalculationTests {

    private Map<String, Object> baseRequestBody;

    @BeforeClass
    public void setup() {
        // Base URI setup
        RestAssured.baseURI = "https://taotlus.bigbank.ee/api/v1/loan";
    }

    @BeforeMethod
    public void createBaseRequestBody() {
        // Initialize the base request body
        baseRequestBody = new HashMap<>();
        baseRequestBody.put("currency", "EUR");
        baseRequestBody.put("productType", "SMALL_LOAN_EE01");
        baseRequestBody.put("maturity", 60);
        baseRequestBody.put("administrationFee", 3.49);
        baseRequestBody.put("conclusionFee", 100);
        baseRequestBody.put("amount", 1000);
        baseRequestBody.put("monthlyPaymentDay", 15);
        baseRequestBody.put("interestRate", 16.8);
    }

    @Test
    public void testValidLoanCalculation() {
        // Act
        Response response = given()
                .contentType(ContentType.JSON)
                .body(baseRequestBody)
                .when()
                .post("/calculate")
                .then()
                .statusCode(200)
                .extract().response();

        // Assert the total amount and other key values
        Double totalRepayableAmount = response.jsonPath().getDouble("totalRepayableAmount");
        Assert.assertNotNull(totalRepayableAmount, "Total Amount is null!");
        Assert.assertTrue(totalRepayableAmount > 0, "Total Amount should be greater than 0!");

        Double monthlyPayment = response.jsonPath().getDouble("monthlyPayment");
        Assert.assertNotNull(monthlyPayment, "Monthly Payment is null!");
        Assert.assertTrue(monthlyPayment > 0, "Monthly Payment should be greater than 0!");

        Double apr = response.jsonPath().getDouble("apr");
        Assert.assertNotNull(apr, "Interest Rate is null!");
        Assert.assertEquals(apr, 32.5, "APR should be present");
    }

    @Test
    public void testMissingAmountField() {
        // Remove the currency field from the request body
        baseRequestBody.remove("amount");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(baseRequestBody)
                .when()
                .post("/calculate")
                .then()
                .statusCode(400)
                .extract().response();

        // Assert that the response contains an error message
        String errorMessage = response.jsonPath().getString("message");
        Assert.assertNotNull(errorMessage, "Error message is null!");
        //Assert.assertEquals(errorMessage, "should have required property 'amount'", "Expected error message for missing amount.");
    }

    @Test
    public void testInvalidMaturity() {
        // Modify the currency field to an invalid value
        baseRequestBody.put("maturity", -60);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(baseRequestBody)
                .when()
                .post("/calculate")
                .then()
                .statusCode(400)
                .extract().response();

        // Assert that the response contains an error message
        String errorMessage = response.jsonPath().getString("error");
        Assert.assertNotNull(errorMessage, "Error message is null!");
        Assert.assertEquals(errorMessage, "Maturity must be positive", "Expected error message for invalid maturity.");
    }

    @Test
    public void testNegativeAmount() {
        // Modify the amount to be negative
        baseRequestBody.put("amount", -1000);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(baseRequestBody)
                .when()
                .post("/calculate")
                .then()
                .statusCode(400)
                .extract().response();

        // Assert that the response contains an error message
        String errorMessage = response.jsonPath().getString("error");
        Assert.assertNotNull(errorMessage, "Error message is null!");
        Assert.assertEquals(errorMessage, "Amount must be positive", "Expected error message for negative amount.");
    }

    @Test
    public void testInvalidProductType() {
        // Modify the product type to an invalid value
        baseRequestBody.put("productType", "INVALID_TYPE");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(baseRequestBody)
                .when()
                .post("/calculate")
                .then()
                .statusCode(400)
                .extract().response();

        // Assert that the response contains an error message
        String errorMessage = response.jsonPath().getString("error");
        Assert.assertNotNull(errorMessage, "Error message is null!");
        Assert.assertEquals(errorMessage, "Invalid product type", "Expected error message for invalid product type.");
    }

    @Test
    public void testMaturityBoundary() {
        // Set the maturity to the lower boundary (e.g., 1 month)
        baseRequestBody.put("maturity", 1);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(baseRequestBody)
                .when()
                .post("/calculate")
                .then()
                .statusCode(200)
                .extract().response();

        // Assert that the total amount is calculated correctly
        Double totalRepayableAmount = response.jsonPath().getDouble("totalRepayableAmount");
        Assert.assertNotNull(totalRepayableAmount, "Total Amount is null!");
        Assert.assertTrue(totalRepayableAmount > 0, "Total Amount should be greater than 0!");
    }

    @Test
    public void testHighInterestRate() {
        // Set a high interest rate for the test
        baseRequestBody.put("interestRate", 50.0);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(baseRequestBody)
                .when()
                .post("/calculate")
                .then()
                .statusCode(200)
                .extract().response();

        // Assert that the APR rate in the response increases
        Double responseApr = response.jsonPath().getDouble("apr");
        Assert.assertNotNull(responseApr, "APR Rate is null!");
        Assert.assertTrue(responseApr > 32.5, "APR increases compared default values");
    }
}

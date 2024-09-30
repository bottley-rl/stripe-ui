package cordova.plugin.stripeuiplugin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.*;

import java.util.HashMap;
import android.util.Log;
import androidx.compose.ui.graphics.Color;

public class CheckoutActivity extends AppCompatActivity {
    Intent resultIntent = new Intent();
    HashMap<String, String> resultMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resultMap.clear();
        Intent receivedIntent = getIntent();

        String publishableKey = receivedIntent.getStringExtra("publishableKey");
        String companyName = receivedIntent.getStringExtra("companyName");
        String paymentIntent = receivedIntent.getStringExtra("paymentIntent");
        String setupIntent = receivedIntent.getStringExtra("setupIntent");
        String customerId = receivedIntent.getStringExtra("customerId");
        String ephemeralKey = receivedIntent.getStringExtra("ephemeralKey");
        String appleMerchantCountryCode = receivedIntent.getStringExtra("appleMerchantCountryCode");

        // Billing details
        String billingEmail = receivedIntent.getStringExtra("billingEmail");
        String billingName = receivedIntent.getStringExtra("billingName");
        String billingPhone = receivedIntent.getStringExtra("billingPhone");
        String billingCity = receivedIntent.getStringExtra("billingCity");
        String billingCountry = receivedIntent.getStringExtra("billingCountry");
        String billingLine1 = receivedIntent.getStringExtra("billingLine1");
        String billingLine2 = receivedIntent.getStringExtra("billingLine2");
        String billingPostalCode = receivedIntent.getStringExtra("billingPostalCode");
        String billingState = receivedIntent.getStringExtra("billingState");

        boolean mobilePayEnabled = receivedIntent.getBooleanExtra("mobilePayEnabled", false);

        Log.d("CheckoutActivity", "publishableKey: " + publishableKey);
        Log.d("CheckoutActivity", "companyName: " + companyName);
        Log.d("CheckoutActivity", "paymentIntent: " + paymentIntent);
        Log.d("CheckoutActivity", "setupIntent: " + setupIntent);
        Log.d("CheckoutActivity", "customerId: " + customerId);
        Log.d("CheckoutActivity", "ephemeralKey: " + ephemeralKey);
        Log.d("CheckoutActivity", "appleMerchantCountryCode: " + appleMerchantCountryCode);
        Log.d("CheckoutActivity", "billingEmail: " + billingEmail);
        Log.d("CheckoutActivity", "billingName: " + billingName);
        Log.d("CheckoutActivity", "billingPhone: " + billingPhone);
        Log.d("CheckoutActivity", "billingCity: " + billingCity);
        Log.d("CheckoutActivity", "billingCountry: " + billingCountry);
        Log.d("CheckoutActivity", "billingPostalCode: " + billingPostalCode);
        Log.d("CheckoutActivity", "billingState: " + billingState);

        try {
            assert publishableKey != null;
            assert companyName != null;

            PaymentConfiguration.init(this, publishableKey);

            PaymentSheet paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

            PaymentSheet.Appearance appearance = new PaymentSheet.Appearance(
                // Light Mode
                new PaymentSheet.Colors(
                    Color.rgb(214, 128, 33), // primary 
                    Color.rgb(255, 255, 255), // surface
                    Color.rgb(214, 128, 33), // component
                    Color.TRANSPARENT, // componentBorder
                    Color.BLACK, // componentDivider
                    Color.BLACK, // onComponent  
                    Color.rgb(115, 117, 123), // onSurface
                    Color.BLACK, // subtitle
                    Color.rgb(157, 160, 172), // placeholderText
                    Color.rgb(214, 128, 33), // appBarIcon
                    Color.RED // error
                ), 
                // Dark Mode
                new PaymentSheet.Colors(
                    Color.rgb(214, 128, 33), // primary 
                    Color.rgb(255, 255, 255), // surface
                    Color.rgb(214, 128, 33), // component
                    Color.TRANSPARENT, // componentBorder
                    Color.BLACK, // componentDivider
                    Color.BLACK, // onComponent  
                    Color.rgb(115, 117, 123), // onSurface
                    Color.BLACK, // subtitle
                    Color.rgb(157, 160, 172), // placeholderText
                    Color.rgb(214, 128, 33), // appBarIcon
                    Color.RED // error
                ),
                new PaymentSheet.Shapes(12.0f, 1.0f),
                new PaymentSheet.Typography(1.0f, null),
                new PaymentSheet.PrimaryButton(
                     // Light Mode
                    new PaymentSheet.PrimaryButtonColors(
                        Color.rgb(214, 128, 33),
                        Color.rgb(255, 255, 255),
                        Color.rgb(214, 128, 33),
                        Color.rgb(45, 211, 111),
                        Color.rgb(255, 255, 255)
                    ),
                    // Dark Mode
                    new PaymentSheet.PrimaryButtonColors(
                        Color.rgb(214, 128, 33),
                        Color.rgb(255, 255, 255),
                        Color.rgb(214, 128, 33),
                        Color.rgb(45, 211, 111),
                        Color.rgb(255, 255, 255)
                    ),
                    new PaymentSheet.PrimaryButtonShape(20f, 2f),
                    new PaymentSheet.PrimaryButtonTypography(null, null)
                )
            );

            Log.d("CheckoutActivity", "paymentSheet");

            PaymentSheet.Address billingAddress = new PaymentSheet.Address(billingCity, billingCountry, billingLine1, billingLine2, billingPostalCode, billingState);
            Log.d("CheckoutActivity", "Address");
            PaymentSheet.BillingDetails billingDetails = new PaymentSheet.BillingDetails(billingAddress, billingEmail, billingName, billingPhone);
            Log.d("CheckoutActivity", "BillingDetails");
            PaymentSheet.CustomerConfiguration customerConfig = (customerId != null && !customerId.isEmpty() && ephemeralKey != null && !ephemeralKey.isEmpty()) 
                ? new PaymentSheet.CustomerConfiguration(customerId, ephemeralKey)
                : null;
            Log.d("CheckoutActivity", "customerConfig");

            PaymentSheet.GooglePayConfiguration googlePayConfig = mobilePayEnabled
                    ? new PaymentSheet.GooglePayConfiguration(PaymentSheet.GooglePayConfiguration.Environment.Test, appleMerchantCountryCode)
                    : null;

            Log.d("CheckoutActivity", "googlePayConfig");

            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration(companyName, customerConfig, googlePayConfig, appearance, billingDetails);

            Log.d("CheckoutActivity", "configuration");

            if (paymentIntent == null) {
                 Log.d("CheckoutActivity", "paymentIntent equals null type");
            }

            if (paymentIntent.equals("null")) {
                 Log.d("CheckoutActivity", "paymentIntent equals 'null'");
            }

            if (paymentIntent.isEmpty()) {
                Log.d("CheckoutActivity", "paymentIntent is empty");
            }

            if (paymentIntent != null && !paymentIntent.isEmpty() && !paymentIntent.equals("null")) {
                Log.d("CheckoutActivity", "paymentIntent: " + paymentIntent);
                paymentSheet.presentWithPaymentIntent(paymentIntent, configuration);
            } else if (setupIntent != null) {
                Log.d("CheckoutActivity", "setupIntent: " + setupIntent);
                paymentSheet.presentWithSetupIntent(setupIntent, configuration);
            } else {
                Log.d("CheckoutActivity", "Missing both paymentIntent && setupIntent");
            }

        } catch (Exception e) {
            Log.e("CheckoutActivity", "Error in PaymentSheet initialization", e);
            resultMap.put("code", "2");
            resultMap.put("message", "PAYMENT_FAILED");
            resultMap.put("error", e.getMessage());
            resultIntent.putExtra("result", resultMap);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        resultMap.clear();
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            resultMap.put("code", "0");
            resultMap.put("message", "PAYMENT_COMPLETED");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            resultMap.put("code", "1");
            resultMap.put("message", "PAYMENT_CANCELED");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            resultMap.put("code", "2");
            resultMap.put("message", "PAYMENT_FAILED");
            resultMap.put("error", ((PaymentSheetResult.Failed) paymentSheetResult).getError().getMessage());
        }
        resultIntent.putExtra("result", resultMap);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}

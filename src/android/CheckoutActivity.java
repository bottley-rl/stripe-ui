package cordova.plugin.stripeplugin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.util.HashMap;
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
        String merchantDisplayName = receivedIntent.getStringExtra("companyName");
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

        try {
            assert publishableKey != null;
            assert merchantDisplayName != null;
            PaymentConfiguration.init(this, publishableKey);
            PaymentSheet paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

            PaymentSheet.Appearance appearance = new PaymentSheet.Appearance(
                // Light Mode
                new PaymentSheet.Colors(
                    android.graphics.Color.rgb(214, 128, 33), // primary
                    android.graphics.Color.rgb(255, 255, 255), // surface
                    android.graphics.Color.rgb(255, 255, 255), // component
                    android.graphics.Color.rgb(230, 235, 241), // componentBorder
                    android.graphics.Color.rgb(230, 235, 241), // componentDivider
                    android.graphics.Color.rgb(62, 63, 76), // onComponent (Text)
                    android.graphics.Color.rgb(62, 63, 76), // onSurface (Text)
                    android.graphics.Color.rgb(62, 63, 76), // subtitle
                    android.graphics.Color.rgb(157, 160, 172), // placeholderText
                    android.graphics.Color.rgb(214, 128, 33), // appBarIcon
                    android.graphics.Color.rgb(237, 28, 36) // error
                ), 
                // Dark Mode
                new PaymentSheet.Colors(
                    android.graphics.Color.rgb(214, 128, 33), // primary
                    android.graphics.Color.rgb(255, 255, 255), // surface
                    android.graphics.Color.rgb(255, 255, 255), // component
                    android.graphics.Color.rgb(230, 235, 241), // componentBorder
                    android.graphics.Color.rgb(230, 235, 241), // componentDivider
                    android.graphics.Color.rgb(62, 63, 76), // onComponent (Text)
                    android.graphics.Color.rgb(62, 63, 76), // onSurface (Text)
                    android.graphics.Color.rgb(62, 63, 76), // subtitle
                    android.graphics.Color.rgb(157, 160, 172), // placeholderText
                    android.graphics.Color.rgb(214, 128, 33), // appBarIcon
                    android.graphics.Color.rgb(237, 28, 36) // error
                ),
                new PaymentSheet.Shapes(4f, 0.5f),
                new PaymentSheet.Typography(1.0f, null),
                new PaymentSheet.PrimaryButton(
                     // Light Mode
                    new PaymentSheet.PrimaryButtonColors(
                        android.graphics.Color.rgb(214, 128, 33), // background
                        android.graphics.Color.rgb(255, 255, 255), // onBackground (Text)
                        android.graphics.Color.TRANSPARENT, // border
                        android.graphics.Color.rgb(45, 211, 111), // successBackgroundColor
                        android.graphics.Color.rgb(255, 255, 255)  // onSuccessBackgroundColor (Text)
                    ),
                    // Dark Mode
                    new PaymentSheet.PrimaryButtonColors(
                        android.graphics.Color.rgb(214, 128, 33), // background
                        android.graphics.Color.rgb(255, 255, 255), // onBackground (Text)
                        android.graphics.Color.TRANSPARENT, // border
                        android.graphics.Color.rgb(45, 211, 111), // successBackgroundColor
                        android.graphics.Color.rgb(255, 255, 255)  // onSuccessBackgroundColor (Text)
                    ),
                    new PaymentSheet.PrimaryButtonShape(4f, 0.5f),
                    new PaymentSheet.PrimaryButtonTypography(null, null)
                )
            );

            PaymentSheet.Address billingAddress = new PaymentSheet.Address(billingCity, billingCountry, billingLine1, billingLine2, billingPostalCode, billingState);
            PaymentSheet.BillingDetails defaultBillingDetails = new PaymentSheet.BillingDetails(billingAddress, billingEmail, billingName, billingPhone);
            PaymentSheet.BillingDetailsCollectionConfiguration billingDetailsCollectionConfiguration = new PaymentSheet.BillingDetailsCollectionConfiguration(
                PaymentSheet.BillingDetailsCollectionConfiguration.CollectionMode.Always,
                PaymentSheet.BillingDetailsCollectionConfiguration.CollectionMode.Automatic,
                PaymentSheet.BillingDetailsCollectionConfiguration.CollectionMode.Automatic,
                PaymentSheet.BillingDetailsCollectionConfiguration.AddressCollectionMode.Automatic,
                false
            );
            PaymentSheet.CustomerConfiguration customer = (customerId != null && !customerId.isEmpty() && ephemeralKey != null && !ephemeralKey.isEmpty()) 
                ? new PaymentSheet.CustomerConfiguration(customerId, ephemeralKey)
                : null;

            PaymentSheet.GooglePayConfiguration googlePay = mobilePayEnabled
                    ? new PaymentSheet.GooglePayConfiguration(PaymentSheet.GooglePayConfiguration.Environment.Test, appleMerchantCountryCode)
                    : null;

            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration(merchantDisplayName, customer, googlePay, null, defaultBillingDetails, null, true, true, appearance, null, billingDetailsCollectionConfiguration);

            if (paymentIntent != null && !paymentIntent.isEmpty() && !paymentIntent.equals("null")) {
                paymentSheet.presentWithPaymentIntent(paymentIntent, configuration);
            } else if (setupIntent != null) {
                paymentSheet.presentWithSetupIntent(setupIntent, configuration);
            }

        } catch (Exception e) {
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
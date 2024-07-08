package cordova.plugin.stripeuiplugin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.util.HashMap;

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
        String paymentIntentClientSecret = receivedIntent.getStringExtra("paymentIntentClientSecret");
        String customerId = receivedIntent.getStringExtra("customerId");
        String customerEphemeralKeySecret = receivedIntent.getStringExtra("customerEphemeralKeySecret");
        String countryCode = receivedIntent.getStringExtra("countryCode");
        String currencyCode = receivedIntent.getStringExtra("currencyCode");
        String billingEmail = receivedIntent.getStringExtra("billingEmail");
        String billingName = receivedIntent.getStringExtra("billingName");
        String billingPhone = receivedIntent.getStringExtra("billingPhone");
        String billingCity = receivedIntent.getStringExtra("billingCity");
        String billingCountry = receivedIntent.getStringExtra("billingCountry");
        String billingLine1 = receivedIntent.getStringExtra("billingLine1");
        String billingLine2 = receivedIntent.getStringExtra("billingLine2");
        String billingPostalCode = receivedIntent.getStringExtra("billingPostalCode");
        String billingState = receivedIntent.getStringExtra("billingState");
        boolean mobilePayEnabled = receivedIntent.getBooleanExtra("mobilePayEnabled", true);
        boolean googlePayProd = receivedIntent.getBooleanExtra("googlePayProd", false);
        try {
            assert publishableKey != null;
            assert paymentIntentClientSecret != null;
            assert companyName != null;
            assert customerId != null;
            assert customerEphemeralKeySecret != null;
            assert countryCode != null;
            PaymentConfiguration.init(this, publishableKey);
            PaymentSheet paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
            PaymentSheet.Address billingAddress = new PaymentSheet.Address(billingCity, billingCountry, billingLine1, billingLine2, billingPostalCode, billingState);
            PaymentSheet.BillingDetails billingDetails = new PaymentSheet.BillingDetails(billingAddress, billingEmail, billingName, billingPhone);
            PaymentSheet.CustomerConfiguration customerConfig = new PaymentSheet.CustomerConfiguration(customerId, customerEphemeralKeySecret);
            PaymentSheet.GooglePayConfiguration googlePayConfig = mobilePayEnabled ? new PaymentSheet.GooglePayConfiguration(googlePayProd ? PaymentSheet.GooglePayConfiguration.Environment.Production : PaymentSheet.GooglePayConfiguration.Environment.Test, countryCode, currencyCode) : null;
            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration(companyName, customerConfig, googlePayConfig, null, billingDetails);
            paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration);
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

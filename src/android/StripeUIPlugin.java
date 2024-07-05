package cordova.plugin.stripeuiplugin;

import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StripeUIPlugin extends CordovaPlugin {
    private CallbackContext callback;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        switch (action) {
            case "presentPaymentSheet":
                this.presentPaymentSheet(args, callbackContext);
                break;
            default:
                return false;
        }
        return true;
    }

    private void presentPaymentSheet(JSONArray args, CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                callback = callbackContext;
                JSONObject paymentConfig = !args.getString(0).equals("null") ? args.getJSONObject(0) : null;
                JSONObject billingConfig = !args.getString(1).equals("null") ? args.getJSONObject(1) : null;
                assert paymentConfig != null;
                String publishableKey = paymentConfig.getString("publishableKey");
                String companyName = paymentConfig.getString("companyName");
                String paymentIntent = paymentConfig.getString("paymentIntent");
                String customerId = paymentConfig.getString("customerId");
                String ephemeralKey = paymentConfig.getString("ephemeralKey");
                String appleMerchantCountryCode = paymentConfig.getString("appleMerchantCountryCode");
                boolean mobilePayEnabled = paymentConfig.getBoolean("mobilePayEnabled");
                Intent intent = new Intent(cordova.getActivity().getApplicationContext(), CheckoutActivity.class);
                intent.putExtra("publishableKey", publishableKey);
                intent.putExtra("companyName", companyName);
                intent.putExtra("paymentIntent", paymentIntent);
                intent.putExtra("customerId", customerId);
                intent.putExtra("ephemeralKey", ephemeralKey);
                intent.putExtra("appleMerchantCountryCode", appleMerchantCountryCode);
                intent.putExtra("mobilePayEnabled", mobilePayEnabled);
                if (billingConfig != null) {
                    String billingEmail = billingConfig.getString("billingEmail");
                    String billingName = billingConfig.getString("billingName");
                    String billingPhone = billingConfig.getString("billingPhone");
                    String billingCity = billingConfig.getString("billingCity");
                    String billingCountry = billingConfig.getString("billingCountry");
                    String billingLine1 = billingConfig.getString("billingLine1");
                    String billingLine2 = billingConfig.getString("billingLine2");
                    String billingPostalCode = billingConfig.getString("billingPostalCode");
                    String billingState = billingConfig.getString("billingState");
                    intent.putExtra("billingEmail", billingEmail);
                    intent.putExtra("billingName", billingName);
                    intent.putExtra("billingPhone", billingPhone);
                    intent.putExtra("billingCity", billingCity);
                    intent.putExtra("billingCountry", billingCountry);
                    intent.putExtra("billingLine1", billingLine1);
                    intent.putExtra("billingLine2", billingLine2);
                    intent.putExtra("billingPostalCode", billingPostalCode);
                    intent.putExtra("billingState", billingState);
                }
                cordova.setActivityResultCallback(this);
                cordova.getActivity().startActivityForResult(intent, 1);
            } catch (Throwable e) {
                e.printStackTrace();
                callbackContext.error(e.getMessage());
            }
        });
    }

    private JSONObject mapToJSON(HashMap<String, String> map) {
        JSONObject message = new JSONObject();
        for (Map.Entry<String, String> pairs : map.entrySet()) {
            try {
                message.put(pairs.getKey(), pairs.getValue());
            } catch (JSONException ignored) {
            }
        }
        return message;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 1) {
            if (resultCode == -1) {
                HashMap<String, String> resultMap = (HashMap<String, String>) intent.getSerializableExtra("result");
                String data = resultMap != null ? mapToJSON(resultMap).toString() : "OK";
                callback.success(data);
            }
        }
    }

}

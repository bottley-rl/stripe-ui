package cordova.plugin.stripeplugin;

import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;

import com.stripe.android.Stripe;
import com.stripe.android.model.SetupIntent;

public class StripePlugin extends CordovaPlugin {
    private Stripe stripe;
    private CallbackContext callbackContext;
    private static CallbackContext persistentCallbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        persistentCallbackContext = callbackContext;
        if (action.equals("presentPaymentSheet")) {
            JSONObject paymentConfig = !args.getString(0).equals("null") ? args.getJSONObject(0) : null;
            JSONObject billingConfig = !args.getString(1).equals("null") ? args.getJSONObject(1) : null;
            this.presentPaymentSheet(paymentConfig, billingConfig);
            return true;
        } else if (action.equals("retrieveSetupIntent")) {
            String clientSecret = args.getString(0);
            this.retrieveSetupIntent(clientSecret);
            return true;
        }
        return false;
    }

    private void presentPaymentSheet(JSONObject paymentConfig, JSONObject billingConfig) {
        cordova.getThreadPool().execute(() -> {
            try {
                assert paymentConfig != null;   
                String publishableKey = paymentConfig.getString("publishableKey");
                String companyName = paymentConfig.getString("companyName");
                String paymentIntent = paymentConfig.optString("paymentIntentClientSecret", null);
                String setupIntent = paymentConfig.optString("setupIntentClientSecret", null);
                String customerId = paymentConfig.optString("customerId", null);
                String ephemeralKey = paymentConfig.optString("customerEphemeralKeySecret", null);
                String merchantCountryCode = paymentConfig.optString("merchantCountryCode", null);
                boolean mobilePayEnabled = paymentConfig.optBoolean("mobilePayEnabled", false);
                boolean isProductionEnv = paymentConfig.optBoolean("isProductionEnv", false);
                String primaryButtonLabel = paymentConfig.optString("primaryButtonLabel", null);
                Intent intent = new Intent(cordova.getActivity().getApplicationContext(), CheckoutActivity.class);
                intent.putExtra("publishableKey", publishableKey);
                intent.putExtra("companyName", companyName);
                intent.putExtra("paymentIntent", paymentIntent);
                intent.putExtra("setupIntent", setupIntent);
                intent.putExtra("customerId", customerId);
                intent.putExtra("ephemeralKey", ephemeralKey);
                intent.putExtra("merchantCountryCode", merchantCountryCode);
                intent.putExtra("mobilePayEnabled", mobilePayEnabled);
                intent.putExtra("isProductionEnv", isProductionEnv);
                intent.putExtra("primaryButtonLabel", primaryButtonLabel);

                if (billingConfig != null) {
                    String billingEmail = billingConfig.optString("billingEmail", null);
                    String billingName = billingConfig.optString("billingName", null);
                    String billingPhone = billingConfig.optString("billingPhone", null);
                    String billingCity = billingConfig.optString("billingCity", null);
                    String billingCountry = billingConfig.optString("billingCountry", null);
                    String billingLine1 = billingConfig.optString("billingLine1", null);
                    String billingLine2 = billingConfig.optString("billingLine2", null);
                    String billingPostalCode = billingConfig.optString("billingPostalCode", null);
                    String billingState = billingConfig.optString("billingState", null);
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

                // Initialize Stripe if it's not already initialized
                if (stripe == null) {
                    stripe = new Stripe(cordova.getContext(), publishableKey);
                }

                cordova.setActivityResultCallback(this);
                cordova.getActivity().startActivityForResult(intent, 1);
            } catch (Throwable e) {
                e.printStackTrace();
                callbackContext.error(e.getMessage());
            }
        });
    }

    private void retrieveSetupIntent(String clientSecret) {
        cordova.getThreadPool().execute(() -> {
            try {
                SetupIntent setupIntent = stripe.retrieveSetupIntentSynchronous(clientSecret);
                if (setupIntent != null) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", setupIntent.getId());
                    result.put("clientSecret", setupIntent.getClientSecret());
                    result.put("paymentMethodId", setupIntent.getPaymentMethodId());
                    result.put("paymentMethod", setupIntent.getPaymentMethod());
                    result.put("created", setupIntent.getCreated());

                    callbackContext.success(new JSONObject(result));
                } else {
                    callbackContext.error("SetupIntent retrieval failed");
                }
            } catch (Throwable e) {
                Log.e("StripePlugin", "Error retrieving SetupIntent", e);
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
            if (callbackContext == null) {
                if (persistentCallbackContext != null) {
                    callbackContext = persistentCallbackContext;
                } else {
                    Log.e("StripeUIPlugin", "CallbackContext is null");
                    return;
                }
            }
            if (resultCode == -1) {
                try {
                    HashMap<String, String> resultMap = (HashMap<String, String>) intent.getSerializableExtra("result");
                    HashMap<String, String> defaultMap = new HashMap<String, String>();
                    defaultMap.put("message", "OK");
                    JSONObject data = resultMap != null ? mapToJSON(resultMap) : mapToJSON(defaultMap);
                    callbackContext.success(data);
                } catch (Exception e) {
                        callbackContext.error("Failed to parse result: " + e.getMessage());
                }
            } else {
                callbackContext.error("Operation canceled or failed");
            }
        }
    }

}
var exec = require('cordova/exec');
module.exports = {
	presentPaymentSheet: function (paymentConfig, billingConfig, success, error) {
		exec(success, error, "StripePlugin", "presentPaymentSheet", [paymentConfig, billingConfig]);
	},
	retrieveSetupIntent: function (clientSecret, success, error) {
		exec(success, error, "StripePlugin", "retrieveSetupIntent", [clientSecret])
  }
};
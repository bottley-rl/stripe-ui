var exec = require('cordova/exec');
module.exports = {
	presentPaymentSheet: function (paymentConfig, billingConfig, success, error) {
		exec(success, error, "StripeUIPlugin", "presentPaymentSheet", [paymentConfig, billingConfig]);
	},
	confirmSetupIntent: function (config, success, error) {
		exec(success, error, "StripeUIPlugin", "confirmSetupIntent", [config]);
	}
};
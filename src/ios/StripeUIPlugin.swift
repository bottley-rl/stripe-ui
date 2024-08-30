import UIKit
import StripePaymentSheet

@objc(StripeUIPlugin) class StripeUIPlugin : CDVPlugin {
    var paymentSheet: PaymentSheet?
    @objc(presentPaymentSheet:)
    func presentPaymentSheet(command: CDVInvokedUrlCommand){
        let paymentConfig = (command.argument(at: 0) ?? [String: Any]()) as? [String: Any] ?? [String: Any]()
        let billingConfig = (command.argument(at: 1) ?? [String: Any]()) as? [String: Any] ?? [String: Any]()
        let publishableKey = (paymentConfig["publishableKey"] ?? "") as? String ?? ""
        let companyName = (paymentConfig["companyName"] ?? "") as? String ?? ""
        let paymentIntentClientSecret = (paymentConfig["paymentIntentClientSecret"] ?? "") as? String ?? ""
        let setupIntentClientSecret = (paymentConfig["setupIntentClientSecret"] ?? "") as? String ?? ""
        let customerId = (paymentConfig["customerId"] ?? "") as? String ?? ""
        let customerEphemeralKeySecret = (paymentConfig["customerEphemeralKeySecret"] ?? "") as? String ?? ""
        let appleMerchantId = (paymentConfig["appleMerchantId"] ?? "") as? String ?? ""
        let appleMerchantCountryCode = (paymentConfig["appleMerchantCountryCode"] ?? "") as? String ?? ""
        let mobilePayEnabled = (paymentConfig["mobilePayEnabled"] ?? false) as? Bool ?? false
        let returnURL = (paymentConfig["returnURL"] ?? "") as? String ?? ""


        let billingEmail = (billingConfig["billingEmail"] ?? "") as? String ?? ""
        let billingName = (billingConfig["billingName"] ?? "") as? String ?? ""
        
        // STPAPIClient.shared.logLevel = .debug
        
        STPAPIClient.shared.publishableKey = publishableKey

        var configuration = PaymentSheet.Configuration()
        
        configuration.style = .alwaysLight

        if returnURL != "" {
            configuration.returnURL = returnURL
        }
      
        if companyName != "" {
            configuration.merchantDisplayName = companyName
        }
        if customerId != "" && customerEphemeralKeySecret != "" {
             configuration.customer = .init(id: customerId, ephemeralKeySecret: customerEphemeralKeySecret)
        }
        
        if mobilePayEnabled && appleMerchantId != "" && appleMerchantCountryCode != "" {
            configuration.applePay = .init(merchantId: appleMerchantId, merchantCountryCode: appleMerchantCountryCode)
        }

        configuration.allowsDelayedPaymentMethods = true
        configuration.defaultBillingDetails.email = billingEmail
        configuration.defaultBillingDetails.name = billingName
        
        configuration.billingDetailsCollectionConfiguration.name = .always
         configuration.billingDetailsCollectionConfiguration.postal_code = .always

        if paymentIntentClientSecret != "" {
            self.paymentSheet = PaymentSheet(paymentIntentClientSecret: paymentIntentClientSecret, configuration: configuration)
        } else {
            self.paymentSheet = PaymentSheet(setupIntentClientSecret: setupIntentClientSecret, configuration: configuration)
        }

        paymentSheet?.present(from: self.viewController) { paymentResult in
            switch paymentResult {
                case .completed:
                    let message = ["code": "0", "message": "PAYMENT_COMPLETED"] as [AnyHashable : Any]
                    let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: message)
                    self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
                case .canceled:
                    let message = ["code": "1", "message": "PAYMENT_CANCELED"] as [AnyHashable : Any]
                    let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: message)
                    self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
                case .failed(let error):
                    let message = ["code": "2", "message": "PAYMENT_FAILED", "error":"\(error.localizedDescription)"] as [AnyHashable : Any]
                    let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: message)
                    self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            }
        }
    }

    @objc(confirmSetupIntent:)
    func confirmSetupIntent(command: CDVInvokedUrlCommand) {
        // STPAPIClient.shared.logLevel = .debug
        let paymentConfig = (command.argument(at: 0) ?? [String: Any]()) as? [String: Any] ?? [String: Any]()
        let setupIntentClientSecret = (paymentConfig["setupIntentClientSecret"] ?? "") as? String ?? ""
        let publishableKey = (paymentConfig["publishableKey"] ?? "") as? String ?? ""

        print("Payment Config: \(paymentConfig)")
        print("SetupIntent Client Secret: \(setupIntentClientSecret)")
        print("Publishable Key: \(publishableKey)")

        // Check if the setupIntentClientSecret and publishableKey are not empty
        if setupIntentClientSecret.isEmpty || publishableKey.isEmpty {
            let message = ["code": "2", "message": "SETUP_INTENT_CONFIRMATION_FAILED", "error": "Invalid SetupIntentClientSecret or PublishableKey"] as [AnyHashable: Any]
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: message)
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        STPAPIClient.shared.publishableKey = publishableKey

        let setupIntentParams = STPSetupIntentConfirmParams(clientSecret: setupIntentClientSecret)
        
        STPAPIClient.shared.confirmSetupIntent(with: setupIntentParams, expand: ["payment_method"]) { setupIntent, error in
            if let error = error {
                let message = ["code": "2", "message": "SETUP_INTENT_CONFIRMATION_FAILED", "error": "\(error.localizedDescription)"] as [AnyHashable: Any]
                let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: message)
                self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
                return
            }
            print("SetupIntent Response: \(String(describing: setupIntent))")
            
            if let paymentMethodID = setupIntent?.paymentMethodID {
                let message = ["code": "0", "message": "SETUP_INTENT_CONFIRMED", "paymentMethodID": paymentMethodID] as [AnyHashable: Any]
                let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: message)
                self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            } else {
                    let message = ["code": "2", "message": "SETUP_INTENT_CONFIRMATION_FAILED", "error": "Payment method ID not found"] as [AnyHashable: Any]
                    let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: message)
                    self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            }
        }
    }
}
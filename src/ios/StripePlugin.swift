import UIKit
import PassKit
import StripePaymentSheet

@objc(StripePlugin) class StripePlugin : CDVPlugin {
    var paymentSheet: PaymentSheet?
    @objc(presentPaymentSheet:)
    func presentPaymentSheet(command: CDVInvokedUrlCommand){

        // Payment Configuration Details
        let paymentConfig = (command.argument(at: 0) ?? [String: Any]()) as? [String: Any] ?? [String: Any]()
        let publishableKey = (paymentConfig["publishableKey"] ?? "") as? String ?? ""
        let companyName = (paymentConfig["companyName"] ?? "") as? String ?? ""
        let paymentIntentClientSecret = (paymentConfig["paymentIntentClientSecret"] ?? "") as? String ?? ""
        let setupIntentClientSecret = (paymentConfig["setupIntentClientSecret"] ?? "") as? String ?? ""
        let customerId = (paymentConfig["customerId"] ?? "") as? String ?? ""
        let customerEphemeralKeySecret = (paymentConfig["customerEphemeralKeySecret"] ?? "") as? String ?? ""
        let appleMerchantId = (paymentConfig["appleMerchantId"] ?? "") as? String ?? ""
        let merchantCountryCode = (paymentConfig["merchantCountryCode"] ?? "") as? String ?? ""
        let mobilePayEnabled = (paymentConfig["mobilePayEnabled"] ?? false) as? Bool ?? false
        let returnURL = (paymentConfig["returnURL"] ?? "") as? String ?? ""
        let primaryButtonLabel = (paymentConfig["primaryButtonLabel"] ?? "") as? String ?? ""
        let applePaymentSummaryItems = (paymentConfig["applePaymentSummaryItems"] ?? []) as? [[String: Any]] ?? []

        print("applePaymentSummaryItems: \(applePaymentSummaryItems)")

        // Customer Billing Details
        let billingConfig = (command.argument(at: 1) ?? [String: Any]()) as? [String: Any] ?? [String: Any]()
        let billingEmail = (billingConfig["billingEmail"] ?? "") as? String ?? ""
        let billingName = (billingConfig["billingName"] ?? "") as? String ?? ""
        
        STPAPIClient.shared.publishableKey = publishableKey

        var useMobilePay = false
        var configuration = PaymentSheet.Configuration()
        
        // Currently dark mode not supported
        configuration.style = .alwaysLight

        // Deeplink return URL after bank / 3rd party auth
        if returnURL != "" {
            configuration.returnURL = returnURL
        }
      
        if companyName != "" {
            configuration.merchantDisplayName = companyName
        }
        if customerId != "" && customerEphemeralKeySecret != "" {
            configuration.customer = .init(id: customerId, ephemeralKeySecret: customerEphemeralKeySecret)
        }
        
        if mobilePayEnabled {
            print("mobilePayEnabled is enabled")
            configuration.applePay = .init(merchantId: appleMerchantId, merchantCountryCode: merchantCountryCode)
        }

        configuration.allowsDelayedPaymentMethods = true
        configuration.defaultBillingDetails.email = billingEmail
        configuration.defaultBillingDetails.name = billingName
        configuration.defaultBillingDetails.address.country = merchantCountryCode
        
        configuration.billingDetailsCollectionConfiguration.name = .always
        configuration.billingDetailsCollectionConfiguration.address = .automatic
        if primaryButtonLabel != "" {
          configuration.primaryButtonLabel = primaryButtonLabel
        }

        // Payment Sheet UI
        var appearance = PaymentSheet.Appearance()
        appearance.colors.primary = UIColor(red: 214/255, green: 128/255, blue: 33/255, alpha: 1)
        appearance.primaryButton.textColor = UIColor(red: 255/255, green: 255/255, blue: 255/255, alpha: 1)
        configuration.appearance = appearance

        /*
            Need to support both PaymentIntent API & SetupIntent API 
        */
        if paymentIntentClientSecret != "" {
            self.paymentSheet = PaymentSheet(paymentIntentClientSecret: paymentIntentClientSecret, configuration: configuration)
        } else {
            self.paymentSheet = PaymentSheet(setupIntentClientSecret: setupIntentClientSecret, configuration: configuration)
        }

        paymentSheet?.present(from: self.viewController) { paymentResult in
            switch paymentResult {
                case .completed:
                    let message = ["code": "0", "message": "PAYMENT_COMPLETED", "useMobilePay": useMobilePay] as [AnyHashable : Any]
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

    @objc(retrieveSetupIntent:)
    func retrieveSetupIntent(command: CDVInvokedUrlCommand){
        let clientSecret = (command.argument(at: 0) ?? "") as? String ?? ""

        STPAPIClient.shared.retrieveSetupIntent(withClientSecret: clientSecret) { setupIntent, error in
            var message = [:] as [AnyHashable : Any]
            if (error != nil) {
                message = ["error":"\(error?.localizedDescription ?? "")"]
            } else {
                let clientSecret = (setupIntent?.clientSecret ?? "") as String
                let paymentMethodId = (setupIntent?.paymentMethodID ?? "") as String
                let id = (setupIntent?.stripeID ?? "") as String
                let created = (setupIntent?.created.timeIntervalSince1970 ?? 0) as TimeInterval
                
                message = ["id": id, "clientSecret": clientSecret, "paymentMethodId": paymentMethodId, "created": created] as [AnyHashable : Any]
            }
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: message)
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    }
}

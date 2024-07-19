import UIKit
import Stripe

@objc(StripeUIPlugin) class StripeUIPlugin : CDVPlugin {
    var paymentSheet: PaymentSheet?
    @objc(presentPaymentSheet:)
    func presentPaymentSheet(command: CDVInvokedUrlCommand){
        let paymentConfig = (command.argument(at: 0) ?? [String: Any]()) as? [String: Any] ?? [String: Any]()
        let publishableKey = (paymentConfig["publishableKey"] ?? "") as? String ?? ""
        let companyName = (paymentConfig["companyName"] ?? "") as? String ?? ""
        let paymentIntentClientSecret = (paymentConfig["paymentIntentClientSecret"] ?? "") as? String ?? ""
        let customerId = (paymentConfig["customerId"] ?? "") as? String ?? ""
        let customerEphemeralKeySecret = (paymentConfig["customerEphemeralKeySecret"] ?? "") as? String ?? ""
        let appleMerchantId = (paymentConfig["appleMerchantId"] ?? "") as? String ?? ""
        let appleMerchantCountryCode = (paymentConfig["appleMerchantCountryCode"] ?? "") as? String ?? ""
        let mobilePayEnabled = (paymentConfig["mobilePayEnabled"] ?? false) as? Bool ?? false
        let returnURL = (paymentConfig["returnURL"] ?? "") as? String ?? ""
        STPAPIClient.shared.publishableKey = publishableKey

        // MARK: Create a PaymentSheet instance
        var configuration = PaymentSheet.Configuration()

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
        if paymentIntentClientSecret != "" {
            self.paymentSheet = PaymentSheet(paymentIntentClientSecret: paymentIntentClientSecret, configuration: configuration)
        } else {
           let intentConfig = PaymentSheet.IntentConfiguration(mode: .setup(currency: "USD")) 
            { [weak self] _, _, intentCreationCallback in
                self?.handleConfirm(intentCreationCallback)
            }
            self.paymentSheet = PaymentSheet(intentConfiguration: intentConfig, configuration: configuration)
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
}

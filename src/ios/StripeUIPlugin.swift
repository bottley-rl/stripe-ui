import UIKit
import Stripe

@objc(StripeUIPlugin) class StripeUIPlugin : CDVPlugin {
    var paymentSheet: PaymentSheet?
    @objc(presentPaymentSheet:)
    func presentPaymentSheet(command: CDVInvokedUrlCommand){
        let paymentConfig = (command.argument(at: 0) ?? [String: Any]()) as? [String: Any] ?? [String: Any]()
        // let billingConfig = (command.argument(at: 1) ?? [String: Any]()) as? [String: Any] ?? [String: Any]()
        let publishableKey = (paymentConfig["publishableKey"] ?? "") as? String ?? ""
        let companyName = (paymentConfig["companyName"] ?? "") as? String ?? ""
        let paymentIntent = (paymentConfig["paymentIntent"] ?? "") as? String ?? ""
        let customerId = (paymentConfig["customerId"] ?? "") as? String ?? ""
        let ephemeralKey = (paymentConfig["ephemeralKey"] ?? "") as? String ?? ""
        let appleMerchantId = (paymentConfig["appleMerchantId"] ?? "") as? String ?? ""
        let appleMerchantCountryCode = (paymentConfig["appleMerchantCountryCode"] ?? "") as? String ?? ""
        let mobilePayEnabled = (paymentConfig["mobilePayEnabled"] ?? false) as? Bool ?? false
        // let billingEmail = (billingConfig["billingEmail"] ?? "") as? String ?? ""
        // let billingName = (billingConfig["billingName"] ?? "") as? String ?? ""
        // let billingPhone = (billingConfig["billingPhone"] ?? "") as? String ?? ""
        // let billingCity = (billingConfig["billingCity"] ?? "") as? String ?? ""
        // let billingCountry = (billingConfig["billingCountry"] ?? "") as? String ?? ""
        // let billingLine1 = (billingConfig["billingLine1"] ?? "") as? String ?? ""
        // let billingLine2 = (billingConfig["billingLine2"] ?? "") as? String ?? ""
        // let billingPostalCode = (billingConfig["billingPostalCode"] ?? "") as? String ?? ""
        // let billingState = (billingConfig["billingState"] ?? "") as? String ?? ""
        StripeAPI.defaultPublishableKey = publishableKey
        var configuration = PaymentSheet.Configuration()
        if companyName != "" {
            configuration.merchantDisplayName = companyName
        }
        if customerId != "" && ephemeralKey != "" {
            configuration.customer = .init(id: customerId, ephemeralKeySecret: ephemeralKey)
        }
        if mobilePayEnabled && appleMerchantId != "" && appleMerchantCountryCode != "" {
            configuration.applePay = .init(merchantId: appleMerchantId, merchantCountryCode: appleMerchantCountryCode)
        }
        // if !billingConfig.isEmpty {
        //     configuration.defaultBillingDetails.email = billingEmail
        //     configuration.defaultBillingDetails.name = billingName
        //     configuration.defaultBillingDetails.phone = billingPhone
        //     configuration.defaultBillingDetails.address.city = billingCity
        //     configuration.defaultBillingDetails.address.country = billingCountry
        //     configuration.defaultBillingDetails.address.line1 = billingLine1
        //     configuration.defaultBillingDetails.address.line2 = billingLine2
        //     configuration.defaultBillingDetails.address.postal_code = billingPostalCode
        //     configuration.defaultBillingDetails.address.state = billingState
        // }
        if paymentIntent != "" {
            self.paymentSheet = PaymentSheet(paymentIntentClientSecret: paymentIntent, configuration: configuration)
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
        } else {
            let message = ["code": "2", "message": "PAYMENT_FAILED", "error":"NO_PAYMENT_INTENT"] as [AnyHashable : Any]
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: message)
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    }
}

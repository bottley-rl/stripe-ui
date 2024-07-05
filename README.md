# cordova-plugin-stripeui
Cordova plugin for Stripe Prebuilt UI on [Android](https://stripe.com/docs/payments/accept-a-payment?platform=android) and [iOS](https://stripe.com/docs/payments/accept-a-payment?platform=ios)

## Demo project
 --> [Ionic5/Cordova](https://github.com/gecsbernat/cordova-plugin-stripeui-demo) <--

## Sample backend
 --> [NodeJs/Cloud-function](https://github.com/gecsbernat/cordova-plugin-stripeui-server) <--

## Features

- It uses the [Stripe Android SDK](https://github.com/stripe/stripe-android) and [Stripe iOS SDK](https://github.com/stripe/stripe-ios) single step UI.
- Creates Stripe Customer from input.
- Accept payment.
- Apple Pay support.
- Google Pay support.

## Installation
```sh
ionic cordova plugin add https://github.com/gecsbernat/cordova-plugin-stripeui.git
```

## Requirements
- Stripe account.
- Secret key and Publishable key (See Sample backend).
- Apple Merchant ID and Apple Merchant Country Code [Apple Pay](https://stripe.com/docs/payments/accept-a-payment?platform=ios&ui=payment-sheet#apple-pay).
- Google Pay setup [Google Pay](https://stripe.com/docs/payments/accept-a-payment?platform=android&ui=payment-sheet#google-pay).

## Backend
- You should host the backend code on your server or in a firebase cloud function (See Sample backend).

## Usage

- Sample typescript service code:
```typescript
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Platform } from '@ionic/angular';

declare const StripeUIPlugin: any;

export interface PaymentResult {
    customerId?: string;
    code?: string;
    message?: string;
    error?: string;
}

export interface PaymentConfig {
    publishableKey?: string;
    companyName?: string;
    customerId?: string;
    paymentIntent?: string;
    ephemeralKey?: string;
    appleMerchantId?: string;
    appleMerchantCountryCode?: string;
    mobilePayEnabled?: boolean;
}

export interface BillingConfig {
    billingEmail?: string;
    billingName?: string;
    billingPhone?: string;
    billingCity?: string;
    billingCountry?: string;
    billingLine1?: string;
    billingLine2?: string;
    billingPostalCode?: string;
    billingState?: string;
}

@Injectable({ providedIn: 'root' })
export class StripePaymentService {

    private isCordova: boolean;
    private SERVER_URL = 'YOUR_BACKEND_URL/payment';

    constructor(
        private platform: Platform,
        private http: HttpClient
    ) {
        this.platform.ready().then(async () => {
            this.isCordova = this.platform.is('cordova');
        });
    }

    makePayment(amount: number, currency: string, customerId: string = null, customerEmail: string = null, customerName: string = null, billingConfig: BillingConfig): Promise<PaymentResult> {
        return new Promise((resolve, reject) => {
            if (this.isCordova) {
                const body = {
                    amount: amount,
                    currency: currency,
                    customerId: customerId,
                    customerEmail: customerEmail,
                    customerName: customerName
                };
                const subscribe = this.http.post(this.SERVER_URL, body).subscribe((result: any) => {
                    const paymentConfig: PaymentConfig = {
                        publishableKey: result.publishableKey,
                        companyName: result.companyName,
                        paymentIntent: result.paymentIntent,
                        customerId: result.customerId,
                        ephemeralKey: result.ephemeralKey,
                        appleMerchantId: result.appleMerchantId,
                        appleMerchantCountryCode: result.appleMerchantCountryCode,
                        mobilePayEnabled: true
                    }
                    this.presentPaymentSheet(paymentConfig, billingConfig).then((result) => {
                        result.customerId = paymentConfig.customerId;
                        resolve(result);
                    }).catch((error) => {
                        reject(error);
                    });
                    subscribe.unsubscribe(); return;
                }, (error) => {
                    reject(error);
                    subscribe.unsubscribe(); return;
                });
            } else {
                reject('NOT_CORDOVA'); return;
            }
        });
    }

    private presentPaymentSheet(paymentConfig: PaymentConfig, billingConfig: BillingConfig): Promise<PaymentResult> {
        return new Promise((resolve, reject) => {
            if (this.isCordova) {
                StripeUIPlugin.presentPaymentSheet(paymentConfig, billingConfig, (success: any) => {
                    try {
                        const result = JSON.parse(success) as PaymentResult;
                        resolve(result);
                    } catch (unused) {
                        resolve(success);
                    }
                    return;
                }, (error: any) => {
                    reject(error); return;
                });
            } else {
                reject('NOT_CORDOVA'); return;
            }
        });
    }

}
```

- In your payment page:
```typescript
....
  async payment() {
    try {
      this.loading = true;
      // customerId, customerEmail, customerName, billingConfig can be null.
      // customerId should be your saved customer from prevoius payment.
      const paymentResult = await this.stripeService.makePayment(this.amount, this.currency, this.customerId, this.customerEmail, this.customerName, this.billingConfig);
      const code = paymentResult.code ? Number(paymentResult.code) : -1;
      this.loading = false;
      if (code === 0) {
        // PAYMENT_COMPLETED
        this.savePayment(paymentResult);
      } else if (code === 1) {
        // PAYMENT_CANCELED
      } else if (code === 2) {
        // PAYMENT_FAILED
      }
    } catch (error) {
      this.loading = false;
      console.log(error);
    }
  }

  savePayment(paymentResult: PaymentResult) {
    // TODO: save the payment and customer in your database for later use...
    // customerId?: string; code?: string; message?: string; error?: string;
    console.log({ paymentResult });
    this.dismiss(true);
  }
  ....
```

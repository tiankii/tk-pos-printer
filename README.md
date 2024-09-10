# POS Print Tiankii App

## Overview

Our Android APK, tk-pos-printer, encapsulates [NyxPrinterClient](https://github.com/yyzz2333/NyxPrinterClient), an SDK that enables receipt printing from Bitcoinzie POS machines. It's a fork from the version created by [BlinkBTC for Bitcoinzie POS](https://github.com/GaloyMoney/pos-print-companion). This lightweight app operates entirely in the background without a user interface, ensuring seamless integration with your system. The lack of a UI not only simplifies the user experience but also resolves the issue of switching between applications when using Deeplink. It leverages deep linking to receive data and automatically trigger print commands, allowing for efficient, hands-free receipt printing. Furthermore, the APK is optimized for stability and performance, making it an essential tool for reliable POS operations.

## Description

- By operating without a user interface, the app runs seamlessly in the background, eliminating the need to switch between applications.
- The integration with the NyxPrinterClient SDK ensures smooth and fully compatible receipt printing with Bitcoinzie POS machines.
- Through deep links, the app can receive and interpret print commands from URLs, enhancing communication between applications efficiently.
  
## Building the App

To build the app, you need to have the Java Development Kit (JDK-17) installed on your computer. Use the following command to compile the app:

```shell
./gradlew assembleDebug
```

usage
```javascript
// Define the parameters
var username = 'tiankiiUsername';
var amount = 'TransactionAmount';
var paymentHash = 'UniquePaymentHash';

// Encode the parameters and construct the deep link URL
var deepLinkUrl = var deepLinkUrl = `tiankii-pos-printer://print?storeName=${encodeURIComponent(storeName)}&appName=${encodeURIComponent(appName)}&invoiceId=${encodeURIComponent(invoiceId)}&date=${encodeURIComponent(date)}&rate=${encodeURIComponent(rate)}&total=${encodeURIComponent(total)}&tip=${encodeURIComponent(tip)}`;

// Redirect to the deep link URL to initiate the printing process
window.location.href = deepLinkUrl;
```

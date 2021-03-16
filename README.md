# ars_sms_retriever
[![Fork](https://img.shields.io/github/forks/arsamme/flutter-sms-retriever?style=social)](https://github.com/arsamme/flutter-sms-retriever/fork)&nbsp; [![Star](https://img.shields.io/github/stars/arsamme/flutter-sms-retriever?style=social)](https://github.com/arsamme/flutter-sms-retriever)&nbsp; [![Watches](https://img.shields.io/github/watchers/arsamme/flutter-sms-retriever?style=social)](https://github.com/arsamme/flutter-sms-retriever/)&nbsp; [![Get the library](https://img.shields.io/badge/Get%20library-pub-blue)](https://pub.dev/packages/ars_sms_retriever)&nbsp; [![Example](https://img.shields.io/badge/Example-Ex-success)](https://pub.dev/packages/ars_sms_retriever/example)

Flutter plugin for retrieving OTP code sent in sms automatically and without getting SMS permission in Android. You can find full API document [here](https://developers.google.com/identity/sms-retriever/)

## Getting Started

Install
``` yaml
dependencies:
  flutter_paginator: ^1.0.0
```

Import
``` dart
import 'package:ars_sms_retriever/ars_sms_retriever.dart';
```

### App signature
To retrieve a app signature. App signature should be placed at end of SMS so SmsRetriever API can verify SMS is sent from your server.
``` dart
String appSignature = await SmsRetriever.getAppSignature();
```

### Request phone number
You can request android to open a dialog with user's phone numbers, then user can select one.
``` dart
String phoneNumber = await ArsSmsRetriever.requestPhoneNumber();
```

### Start listening for SMS
To start listening for an incoming SMS
``` dart
String smsCode = await ArsSmsRetriever.startListening();
```

### Close receiver after getting SMS
Stop listening after getting the SMS
``` dart
ArsSmsRetriever.stopListening();
```

#### Note
SMS format should be like this :

``` text
Your example code is:
123456
appSignature
```
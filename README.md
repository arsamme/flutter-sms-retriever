# Android Sms Retriever 
[![Fork](https://img.shields.io/github/forks/arsamme/flutter-sms-retriever?style=social)](https://github.com/arsamme/flutter-sms-retriever/fork)&nbsp; [![Star](https://img.shields.io/github/stars/arsamme/flutter-sms-retriever?style=social)](https://github.com/arsamme/flutter-sms-retriever)&nbsp; [![Watches](https://img.shields.io/github/watchers/arsamme/flutter-sms-retriever?style=social)](https://github.com/arsamme/flutter-sms-retriever/)&nbsp; [![pub package](https://img.shields.io/pub/v/android_sms_retriever.svg)](https://pub.dartlang.org/packages/android_sms_retriever)&nbsp; [![Example](https://img.shields.io/badge/Example-Ex-success)](https://pub.dev/packages/android_sms_retriever/example)

Flutter plugin for retrieving OTP code sent in sms automatically and without getting SMS permission in Android. This package has support for Smart Lock credentials, you can use for storing user's phone number and retrieving whenever you want. Read more [here](https://developers.google.com/identity/sms-retriever/)

## Example
![example](https://user-images.githubusercontent.com/21082113/111798027-ecafe600-88de-11eb-902d-681bc42e2f4f.gif)


## Getting Started

Install
```yaml
dependencies:
  android_sms_retriever: ^1.2.1
```

Import
```dart
import 'package:android_sms_retriever/android_sms_retriever.dart';
```

### App signature
Use this function to get application signature. App signature should be placed at end of SMS so SmsRetriever API can verify SMS is sent from your server.
```dart
String appSignature = await AndroidSmsRetriever.getAppSignature();
```

### Request phone number
You can request android to open a dialog with user's phone numbers, then user can select one.
```dart
String phoneNumber = await AndroidSmsRetriever.requestPhoneNumber();
```

### Store phone number
You can store user's phone number to use later, For example: When user uninstalls application and installs again.
You should pass your application's website URL and phone number to this function.
```dart
await AndroidSmsRetriever.storePhoneNumber('https://arsam.me','09027777254');
```

### Retrieve stored phone number
To retrieve stored phone number use this function.
You should pass your application's website URL to this function.
```dart
String phoneNumber = await AndroidSmsRetriever.retrieveStoredPhoneNumber('https://arsam.me');
```

### Delete stored phone number
To delete stored phone number use this function.
You should pass your application's website URL and phone number to this function.
```dart
await AndroidSmsRetriever.deleteStoredPhoneNumber('https://arsam.me','09027777254');
```

### Start listening for SMS
Use this function to start listening for an incoming SMS. When sms received message will be returned.
```dart
String message = await AndroidSmsRetriever.startSmsListener();
```

### Close receiver after getting SMS
Stop listening for SMS. It's better to stop listener after getting message.
```dart
AndroidSmsRetriever.stopSmsListener();
```

### Request one time SMS consent
Using this function, when sms received android will ask user to let application use message and extract code, even if sms message does not contain application signature.
You can pass sender phone number in order to detect messages sent from specific sender.
```dart
String smsCode = await AndroidSmsRetriever.requestOneTimeConsentSms('+9850003001');
```

#### Note
SMS format should be like this :

```text
Your example code is: 123456

appSignature
```

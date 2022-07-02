## 1.3.3
- Safe execute unregister receiver

## 1.3.2
- FIX

## 1.3.1
- Added `SmsRetriever.SEND_PERMISSION` permission

## 1.3.0
- `startSmsListener` renamed to `listenForSms`
- `requestOneTimeConsentSms` renamed to `listenForOneTimeConsent`
- Added `stopOneTimeConsentListener` to stop consent receiver.

## 1.2.3
- Stop receiver on `onDetachedFromActivity`
- FIX: `Caused by java.lang.IllegalStateException: Reply already submitted`

## 1.2.2
- Replace `FlutterActivity` with `FlutterFragmentActivity` according to [issue 3](https://github.com/arsamme/flutter-sms-retriever/issues/3) and [PR 4](https://github.com/arsamme/flutter-sms-retriever/pull/4)

## 1.2.1
- Fix twice reply issue

## 1.2.0
- Fix return errors.

## 1.0.0
- Set `useAndroidX` to true

## 1.0.0
- Initial Release

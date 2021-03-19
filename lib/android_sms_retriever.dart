import 'dart:async';

import 'package:flutter/services.dart';

class AndroidSmsRetriever {
  static const MethodChannel _channel =
      const MethodChannel('ars_sms_retriever/method_ch');

  static Future<String> getAppSignature() async {
    final String smsCode = await _channel.invokeMethod('getAppSignature');
    return smsCode;
  }

  static Future<String> requestPhoneNumber() async {
    final String phoneNumber =
        await _channel.invokeMethod('requestPhoneNumber');
    return phoneNumber;
  }

  static Future<void> storePhoneNumber(String url, String phoneNumber) async {
    await _channel.invokeMethod('storePhoneNumber', {
      'url': url,
      'phoneNumber': phoneNumber,
    });
  }

  static Future<String> retrieveStoredPhoneNumber(String url) async {
    final String phoneNumber =
        await _channel.invokeMethod('retrieveStoredPhoneNumber', {'url': url});
    return phoneNumber;
  }

  static Future<void> deleteStoredPhoneNumber(String url, String phoneNumber) async {
    await _channel
        .invokeMethod('deleteStoredPhoneNumber', {'url':url,'phoneNumber': phoneNumber});
  }

  static Future<String> startSmsListener() async {
    final String smsCode = await _channel.invokeMethod('startSmsListener');
    return smsCode;
  }

  static Future<void> stopSmsListener() async {
    await _channel.invokeMethod('stopSmsListener');
  }

  static Future<String> requestOneTimeConsentSms(
      {String senderPhoneNumber}) async {
    final String smsCode = await _channel.invokeMethod(
        'requestOneTimeConsentSms', {'senderPhoneNumber': senderPhoneNumber});
    return smsCode;
  }
}

import 'dart:async';

import 'package:flutter/services.dart';

/// AndroidSmsRetriever
class AndroidSmsRetriever {
  /// @nodoc
  static const MethodChannel _channel =
      const MethodChannel('ars_sms_retriever/method_ch');

  /// Use this function to get application signature. App signature should be placed at end of SMS so SmsRetriever API can verify SMS is sent from your server.
  static Future<String> getAppSignature() async {
    final String applicationSignature =
        await _channel.invokeMethod('getAppSignature');
    return applicationSignature;
  }

  /// You can request android to open a dialog with user's phone numbers, then user can select one.
  static Future<String> requestPhoneNumber() async {
    final String phoneNumber =
        await _channel.invokeMethod('requestPhoneNumber');
    return phoneNumber;
  }

  /// You can store user's phone number to use later, For example: When user uninstalls application and installs again.
  /// You should pass your application's website URL and phone number to this function.
  static Future<void> storePhoneNumber(String url, String phoneNumber) async {
    await _channel.invokeMethod('storePhoneNumber', {
      'url': url,
      'phoneNumber': phoneNumber,
    });
  }

  /// To retrieve stored phone number use this function.
  /// You should pass your application's website URL to this function.
  static Future<String> retrieveStoredPhoneNumber(String url) async {
    final String phoneNumber =
        await _channel.invokeMethod('retrieveStoredPhoneNumber', {'url': url});
    return phoneNumber;
  }

  /// To delete stored phone number use this function.
  /// You should pass your application's website URL and phone number to this function.
  static Future<void> deleteStoredPhoneNumber(
    String url,
    String phoneNumber,
  ) async {
    await _channel.invokeMethod(
        'deleteStoredPhoneNumber', {'url': url, 'phoneNumber': phoneNumber});
  }

  /// Use this function to start listening for an incoming SMS. When sms received message will be returned.
  static Future<String> startSmsListener() async {
    final String smsCode = await _channel.invokeMethod('startSmsListener');
    return smsCode;
  }

  /// Stop listening for SMS. It's better to stop listener after getting message.
  static Future<void> stopSmsListener() async {
    await _channel.invokeMethod('stopSmsListener');
  }

  /// Using this function, when sms received android will ask user to let application use message and extract code, even if sms message does not contain application signature.
  /// You can pass sender phone number in order to detect messages sent from specific sender.
  static Future<String> requestOneTimeConsentSms(
      {String? senderPhoneNumber}) async {
    final String smsCode = await _channel.invokeMethod(
        'requestOneTimeConsentSms', {'senderPhoneNumber': senderPhoneNumber});
    return smsCode;
  }
}

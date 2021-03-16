import 'dart:async';

import 'package:flutter/services.dart';

class ArsSmsRetriever {
  static const MethodChannel _channel = const MethodChannel('ars_sms_retriever/method_ch');

  static Future<String> getAppSignature() async {
    final String smsCode = await _channel.invokeMethod('getAppSignature');
    return smsCode;
  }

  static Future<String> requestPhoneNumber() async {
    final String phoneNumber = await _channel.invokeMethod('requestPhoneNumber');
    return phoneNumber;
  }

  static Future<String> startListening() async {
    final String smsCode = await _channel.invokeMethod('startListening');
    return smsCode;
  }

  static Future<void> stopListening() async {
    await _channel.invokeMethod('stopListening');
  }
}

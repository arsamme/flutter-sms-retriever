import 'package:android_sms_retriever/android_sms_retriever.dart';
import 'package:flutter/material.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _applicationSignature = "";
  String _smsCode = "";
  String _requestedPhoneNumber = "";
  String _storedPhoneNumber = "";

  bool isListening = false;
  bool consentLoading = false;

  @override
  void initState() {
    super.initState();

    AndroidSmsRetriever.getAppSignature().then((value) {
      setState(() {
        _applicationSignature = value ?? 'Signature Not Found';
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        appBar: AppBar(
          title: const Text('SMS retriever example app'),
          backgroundColor: Colors.blue,
        ),
        body: Builder(
          builder: (BuildContext context) {
            return Padding(
              padding: EdgeInsets.symmetric(vertical: 8, horizontal: 16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text('Application Signature: $_applicationSignature'),
                  Container(
                    margin: const EdgeInsets.only(top: 16),
                    child:
                        Text('Requested Phone Number: $_requestedPhoneNumber'),
                  ),
                  Container(
                    margin: const EdgeInsets.only(top: 16),
                    child: Text('Stored Phone Number: $_storedPhoneNumber'),
                  ),
                  Container(
                    margin: const EdgeInsets.only(top: 16),
                    child: Text('Received SMS code: $_smsCode \n'),
                  ),
                  ElevatedButton(
                    onPressed: () {
                      AndroidSmsRetriever.requestPhoneNumber().then((value) {
                        setState(() {
                          _requestedPhoneNumber =
                              value ?? 'Phone Number Not Found';
                        });
                      });
                    },
                    child: Text('Request Phone Number'),
                  ),
                  ElevatedButton(
                    onPressed: () async {
                      await AndroidSmsRetriever.storePhoneNumber(
                        'https://arsam.me',
                        '09034562774',
                      );
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(
                          content: Text('Phone number saved!'),
                        ),
                      );
                    },
                    child: Text('Store Phone Number'),
                  ),
                  ElevatedButton(
                    onPressed: () {
                      setState(() {
                        _storedPhoneNumber = 'RETRIEVING';
                      });
                      AndroidSmsRetriever.retrieveStoredPhoneNumber(
                        'https://arsam.me',
                      ).then((value) {
                        setState(() {
                          _storedPhoneNumber =
                              value ?? 'Phone Number Not Found';
                        });
                      });
                    },
                    child: Text('Retrieve Stored Phone Number'),
                  ),
                  ElevatedButton(
                    onPressed: () async {
                      await AndroidSmsRetriever.deleteStoredPhoneNumber(
                        'https://arsam.me',
                        '09034562774',
                      );

                      setState(() {
                        _storedPhoneNumber = 'DELETED';
                      });
                    },
                    child: Text('Delete Stored Phone Number'),
                  ),
                  ElevatedButton(
                    onPressed: () {
                      setState(() {
                        _smsCode = 'LISTENING';
                      });

                      AndroidSmsRetriever.startSmsListener().then((value) {
                        setState(() {
                          final intRegex = RegExp(r'\d+', multiLine: true);
                          final code = intRegex
                              .allMatches(value ?? 'Phone Number Not Found')
                              .first
                              .group(0);
                          _smsCode = code ?? 'NO CODE';
                          AndroidSmsRetriever.stopSmsListener();
                        });
                      });
                    },
                    child: Text('Start SMS Listener'),
                  ),
                  ElevatedButton(
                    onPressed: () {
                      setState(() {
                        _smsCode = 'Waiting for consent';
                      });

                      AndroidSmsRetriever.requestOneTimeConsentSms()
                          .then((value) {
                        setState(() {
                          final intRegex = RegExp(r'\d+', multiLine: true);
                          final code = intRegex
                              .allMatches(value ?? 'Phone Number Not Found')
                              .first
                              .group(0);
                          _smsCode = code ?? 'NO CODE';
                        });
                      });
                    },
                    child: Text('Request One Time SMS Consent'),
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }

  String getCode(String sms) {
    final intRegex = RegExp(r'\d+', multiLine: true);
    final code = intRegex.allMatches(sms).first.group(0);
    return code ?? 'NO CODE';
  }
}

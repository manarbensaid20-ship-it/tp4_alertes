
import 'package:flutter/services.dart';

class CanauxNatifs {
  static const MethodChannel _channel =
  MethodChannel('alerte/notifier');

  static const MethodChannel _workChannel =
  MethodChannel('sync/planifier');
  static const MethodChannel _serviceChannel =
  MethodChannel('service/surveillance');

  static Future<bool> demarrerService() async {
    final result = await _serviceChannel.invokeMethod<bool>(
      'demarrerService',
    );
    return result ?? false;
  }

  static Future<bool> arreterService() async {
    final result = await _serviceChannel.invokeMethod<bool>(
      'arreterService',
    );
    return result ?? false;
  }
  // Demander permission notifications
  static Future<bool> demanderPermissionNotif() async {
    final result = await _channel.invokeMethod<bool>(
      'demanderPermission',
    );
    return result ?? false;

  }

  // Envoyer une notification
  static Future<bool> notifier({
    required String titre,
    required String message,
  }) async {
    final result = await _channel.invokeMethod<bool>(
      'notifier',
      {
        'titre': titre,
        'message': message,
      },
    );
    return result ?? false;
  }

  // Récupérer une action initiale
  static Future<String?> actionInitiale() async {
    return await _channel.invokeMethod<String>('actionInitiale');
  }

  // Écouter les actions
  static void ecouterActions(
      Function(String action) callback,
      ) {
    _channel.setMethodCallHandler((call) async {
      if (call.method == 'surAction') {
        final action = call.arguments as String;
        callback(action);
      }
    });
  }

  // Planifier la vérification périodique
  static Future<bool> planifierVerification(int seuil) async {
    final result = await _workChannel.invokeMethod<bool>(
      'planifier',
      {'seuil': seuil},
    );
    return result ?? false;
  }

  // Vérifier la batterie immédiatement
  static Future<bool> verifierMaintenant(int seuil) async {
    final result = await _workChannel.invokeMethod<bool>(
      'maintenant',
      {'seuil': seuil},
    );
    return result ?? false;
  }

  // Annuler la vérification périodique
  static Future<bool> annulerVerification() async {
    final result = await _workChannel.invokeMethod<bool>(
      'annuler',
    );
    return result ?? false;
  }
}

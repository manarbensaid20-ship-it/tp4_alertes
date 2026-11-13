
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'native/canaux.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'TP4 Alertes',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
      ),
      home: const PageAlertes(),
    );
  }
}

class PageAlertes extends StatefulWidget {
  const PageAlertes({super.key});

  @override
  State<PageAlertes> createState() => _PageAlertesState();
}

class _PageAlertesState extends State<PageAlertes> {
  String etat = 'Permission non vérifiée';
  String derniereAction = 'Aucune action';

  double seuil = 20;
  String etatWork = 'WorkManager non configuré';

  String etatService = 'Service arrêté';

  @override
  void initState() {
    super.initState();

    CanauxNatifs.ecouterActions((action) {
      if (!mounted) return;
      setState(() {
        derniereAction = action;
      });
    });

    recupererActionInitiale();
  }

  Future<void> recupererActionInitiale() async {
    try {
      final action = await CanauxNatifs.actionInitiale();

      if (!mounted || action == null) return;

      setState(() {
        derniereAction = action;
      });
    } on PlatformException catch (e) {
      debugPrint('Erreur action initiale: ${e.message}');
    }
  }

  void afficherMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message)),
    );
  }

  // Permission notifications
  Future<void> demanderPermission() async {
    try {
      final autorisee =
      await CanauxNatifs.demanderPermissionNotif();

      if (!mounted) return;

      setState(() {
        etat = autorisee
            ? 'Permission accordée'
            : 'Permission refusée';
      });

      afficherMessage(etat);
    } on PlatformException catch (e) {
      afficherMessage('Erreur : ${e.message}');
    }
  }

  // Envoyer une notification
  Future<void> envoyerAlerte() async {
    try {
      final envoyee = await CanauxNatifs.notifier(
        titre: 'Patient critique',
        message: 'Fréquence cardiaque : 142 bpm',
      );

      if (!mounted) return;

      afficherMessage(
        envoyee
            ? 'Notification envoyée avec succès !'
            : 'Notification non envoyée. Vérifie la permission.',
      );
    } on PlatformException catch (e) {
      afficherMessage('Erreur : ${e.message}');
    }
  }

  // Planifier WorkManager
  Future<void> planifier() async {
    try {
      final success = await CanauxNatifs.planifierVerification(
        seuil.round(),
      );

      if (!mounted) return;

      setState(() {
        etatWork = success
            ? 'Vérification planifiée chaque 15 minutes'
            : 'Échec de planification';
      });

      afficherMessage(etatWork);
    } on PlatformException catch (e) {
      afficherMessage('Erreur WorkManager : ${e.message}');
    }
  }

  // Vérification immédiate
  Future<void> verifierMaintenant() async {
    try {
      final success = await CanauxNatifs.verifierMaintenant(
        seuil.round(),
      );

      if (!mounted) return;

      afficherMessage(
        success
            ? 'Vérification immédiate lancée !'
            : 'Échec de vérification',
      );
    } on PlatformException catch (e) {
      afficherMessage('Erreur WorkManager : ${e.message}');
    }
  }

  // Annuler WorkManager
  Future<void> annuler() async {
    try {
      final success =
      await CanauxNatifs.annulerVerification();

      if (!mounted) return;

      setState(() {
        etatWork = success
            ? 'Vérification périodique annulée'
            : 'Échec d’annulation';
      });

      afficherMessage(etatWork);
    } on PlatformException catch (e) {
      afficherMessage('Erreur WorkManager : ${e.message}');
    }
  }

  // Démarrer Foreground Service
  Future<void> demarrerService() async {
    try {
      final success = await CanauxNatifs.demarrerService();

      if (!mounted) return;

      setState(() {
        etatService = success
            ? 'Service actif'
            : 'Échec du démarrage';
      });

      afficherMessage(etatService);
    } on PlatformException catch (e) {
      afficherMessage('Erreur Service : ${e.message}');
    }
  }

  // Arrêter Foreground Service
  Future<void> arreterService() async {
    try {
      final success = await CanauxNatifs.arreterService();

      if (!mounted) return;

      setState(() {
        etatService = success
            ? 'Service arrêté'
            : 'Service déjà arrêté ou non démarré';
      });

      afficherMessage(etatService);
    } on PlatformException catch (e) {
      afficherMessage('Erreur Service : ${e.message}');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('TP4 - Alertes natives'),
        backgroundColor: Colors.blue,
        foregroundColor: Colors.white,
      ),

      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),

        child: Column(
          children: [
            const Icon(
              Icons.notifications_active,
              size: 65,
              color: Colors.blue,
            ),

            const SizedBox(height: 15),

            const Text(
              'Gestion des notifications',
              style: TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.bold,
              ),
            ),

            const SizedBox(height: 15),

            Text(etat),

            const SizedBox(height: 10),

            Text(
              'Dernière action : $derniereAction',
              style: const TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 16,
              ),
            ),

            const SizedBox(height: 20),

            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: demanderPermission,
                icon: const Icon(Icons.lock_open),
                label: const Text('Autoriser les notifications'),
              ),
            ),

            const SizedBox(height: 10),

            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: envoyerAlerte,
                icon: const Icon(Icons.notifications),
                label: const Text('Envoyer une alerte'),
              ),
            ),

            const Divider(height: 40),

            const Icon(
              Icons.battery_charging_full,
              size: 55,
              color: Colors.orange,
            ),

            const SizedBox(height: 10),

            const Text(
              'Surveillance de la batterie',
              style: TextStyle(
                fontSize: 21,
                fontWeight: FontWeight.bold,
              ),
            ),

            const SizedBox(height: 10),

            Text(
              'Seuil de batterie : ${seuil.round()}%',
              style: const TextStyle(fontSize: 17),
            ),

            Slider(
              value: seuil,
              min: 5,
              max: 50,
              divisions: 45,
              label: '${seuil.round()}%',
              onChanged: (value) {
                setState(() {
                  seuil = value;
                });
              },
            ),

            const SizedBox(height: 10),

            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: planifier,
                icon: const Icon(Icons.schedule),
                label: const Text('Planifier (15 minutes)'),
              ),
            ),

            const SizedBox(height: 10),

            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: verifierMaintenant,
                icon: const Icon(Icons.play_arrow),
                label: const Text('Vérifier maintenant'),
              ),
            ),

            const SizedBox(height: 10),

            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: annuler,
                icon: const Icon(Icons.cancel),
                label: const Text('Annuler la surveillance'),
              ),
            ),

            const SizedBox(height: 15),

            Text(
              etatWork,
              textAlign: TextAlign.center,
              style: const TextStyle(
                fontWeight: FontWeight.bold,
                color: Colors.blue,
              ),
            ),

            const Divider(height: 40),

            // FOREGROUND SERVICE

            const Icon(
              Icons.settings_backup_restore,
              size: 55,
              color: Colors.green,
            ),

            const SizedBox(height: 10),

            const Text(
              'Foreground Service',
              style: TextStyle(
                fontSize: 21,
                fontWeight: FontWeight.bold,
              ),
            ),

            const SizedBox(height: 15),

            Text(
              'État : $etatService',
              style: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Colors.green,
              ),
            ),

            const SizedBox(height: 15),

            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: demarrerService,
                icon: const Icon(Icons.play_circle_fill),
                label: const Text('Démarrer le service'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.green,
                  foregroundColor: Colors.white,
                ),
              ),
            ),

            const SizedBox(height: 10),

            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: arreterService,
                icon: const Icon(Icons.stop_circle),
                label: const Text('Arrêter le service'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.red,
                  foregroundColor: Colors.white,
                ),
              ),
            ),

            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }
}

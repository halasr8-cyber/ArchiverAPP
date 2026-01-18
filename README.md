#  Application Android – Archivage de fichiers

##  Objectif du projet
Cette application Android permet d’archiver des fichiers manuellement ou automatiquement
à l’aide de **WorkManager**, en respectant les bonnes pratiques vues en classe.

Le projet a été réalisé dans le cadre du module :
**Tests Unitaires & Tests UI (Espresso)**.

---

##  Fonctionnalités principales

###  Archivage manuel
- L’utilisateur choisit un dossier
- En cliquant sur le bouton "Lancer l’archivage", les anciens fichiers sont déplacés vers un dossier **Archive**

###  Archivage automatique
- Activé via un **Switch**
- Utilise **WorkManager (PeriodicWorkRequest)**
- L’archivage s’exécute automatiquement toutes les 24 heures

###  Notification
- Une notification s’affiche après chaque archivage
- Indique le nombre de fichiers archivés

---

##  Tests réalisés

###  Tests Unitaires (JVM)
- Tests des fonctions métier (logique d’archivage)
- Utilisation de :
  - `@Test`
  - `@Before`
  - `assertEquals`
  - `assertTrue`

###  Tests UI – Espresso
- Simulation du comportement utilisateur :
  - Cliquer sur les boutons
  - Activer / désactiver le Switch
  - Vérifier la présence des éléments UI
- Tests exécutés sur un appareil réel

---

##  Outils et technologies utilisées
- Kotlin
- Android Studio
- WorkManager
- EasyPermissions
- Espresso (UI Testing)
- Git & GitHub
##  Réalisé par
Étudiante en NTIC – ISTA- Ahlam Sour/hajar ettabti

## 👩‍🎓 Réalisé par
Étudiante en NTIC – ISTA

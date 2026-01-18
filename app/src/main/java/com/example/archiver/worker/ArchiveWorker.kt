package com.example.archiver.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.documentfile.provider.DocumentFile
import androidx.work.Worker
import androidx.work.WorkerParameters


class ArchiveWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {


    override fun doWork(): Result {
        // Cette méthode fait tout le travail d'archivage. Elle est appelée automatiquement par WorkManager.
        // Elle doit retourner Result.success() si tout va bien, ou Result.failure() si erreur.

        // Étape 1 : Récupérer l'adresse du dossier choisi (passée depuis MainActivity)
        val uriStr = inputData.getString("folder_uri") ?: return Result.failure()  // "folder_uri" est la clé des données. Si null, échouer.
        val rootUri = android.net.Uri.parse(uriStr)  // Convertir la chaîne en Uri (adresse du dossier)

        // Étape 2 : Accéder au dossier avec DocumentFile (pour Scoped Storage)
        val rootDir = DocumentFile.fromTreeUri(applicationContext, rootUri) ?: return Result.failure()
        // DocumentFile permet d'accéder aux fichiers sans permissions directes. Si échec, arrêter.

        // Étape 3 : Trouver ou créer le dossier "Archive" à l'intérieur du dossier choisi
        val archiveDir = rootDir.findFile("Archive")  // Chercher si "Archive" existe déjà
            ?: rootDir.createDirectory("Archive")  // Si non, le créer
            ?: return Result.failure()  // Si impossible, échouer

        // Étape 4 : Compter les fichiers archivés
        var count = 0  // Variable pour compter combien de fichiers ont été archivés

        // Étape 5 : Lister et traiter chaque fichier dans le dossier racine
        rootDir.listFiles().forEach { file ->  // Pour chaque fichier dans le dossier
            if (file.isFile && isOldFile(file)) {  // Si c'est un fichier (pas un dossier) ET s'il est ancien
                moveFile(file, archiveDir)  // Déplacer le fichier vers "Archive"
                count++  // Incrémenter le compteur
            }
        }

        // Étape 6 : Envoyer une notification à l'utilisateur avec le résultat
        sendNotification(count)

        // Étape 7 : Retourner succès (travail terminé)
        return Result.success()
    }
    // MÉTHODE : Vérifier si un fichier est ancien

    private fun isOldFile(file: DocumentFile): Boolean {
        // Cette méthode dit si un fichier est "ancien" et doit être archivé.
        // Actuellement : fichiers modifiés il y a plus de 1 minute (pour test rapide).
        // Selon votre cahier : devrait être 30 jours. Changez à : val thirtyDays = 30L * 24 * 60 * 60 * 1000

        val oneMinute = 60 * 1000L  // 1 minute en millisecondes (1000 ms = 1 seconde)
        return System.currentTimeMillis() - file.lastModified() > oneMinute
        // System.currentTimeMillis() = heure actuelle en ms.
        // file.lastModified() = dernière modification du fichier en ms.
        // Si différence > 1 minute, retourner true (ancien).
    }


    // MÉTHODE : Déplacer un fichier vers le dossier Archive

    private fun moveFile(source: DocumentFile, targetDir: DocumentFile) {
        // Cette méthode "déplace" un fichier : elle le copie vers "Archive", puis supprime l'original.
        // Pourquoi copier au lieu de renommer ? Parce que DocumentFile ne permet pas toujours le renommage direct.

        // Étape 1 : Créer un nouveau fichier dans le dossier cible ("Archive")
        val newFile = targetDir.createFile(
            source.type ?: "application/octet-stream",  // Type MIME (ex. : "image/jpeg"), défaut si inconnu
            source.name ?: "file"  // Nom du fichier, défaut si null
        ) ?: return  // Si échec de création, arrêter

        // Étape 2 : Copier le contenu du fichier source vers le nouveau
        applicationContext.contentResolver.openInputStream(source.uri)?.use { input ->
            // Ouvrir le fichier source en lecture (input stream)
            applicationContext.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                // Ouvrir le nouveau fichier en écriture (output stream)
                input.copyTo(output)  // Copier tout le contenu
            }
        }

        // Étape 3 : Supprimer le fichier original (comme un "déplacement")
        source.delete()
    }


    // MÉTHODE : Envoyer une notification

    private fun sendNotification(count: Int) {
        // Cette méthode envoie une notification à l'utilisateur pour informer du résultat.
        // Comme dans vos cours WorkManager avec notifications.

        // Étape 1 : Obtenir le service de notifications
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "archive_channel"  // ID unique pour le canal de notification

        // Étape 2 : Créer le canal (nécessaire pour Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Archivage automatique",  // Nom du canal
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        // etape 3Construire la notification
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle("Archivage automatique")
            .setContentText("$count fichiers archivés")
            .build()  // Créer l'objet notification

        // Étape 4 : Afficher la notification
        manager.notify(1, notification)
    }
}

package com.example.archiver


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.work.*
import com.example.archiver.worker.ArchiveWorker
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit



class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    // c'est class hérite de AppCompatActivity (écran d'Android) et implémente EasyPermissions.PermissionCallbacks
    // (pour recevoir les réponses aux demandes de permissions).

    private lateinit var btnArchive: Button  // Bouton pour lancer l'archivage manuel
    private lateinit var btnChooseFolder: Button  // Bouton pour choisir le dossier à archiver
    private lateinit var switchAuto: SwitchCompat  // Interrupteur pour activer/désactiver l'automatique

    private var folderUri: Uri? = null  // Adresse du dossier choisi (null si pas choisi). Uri est comme une "clé" pour accéder au dossier.


    companion object {
        const val PICK_FOLDER = 100  // Code pour identifier la demande de choix de dossier
        const val STORAGE_PERMISSION = 200  // Code pour identifier la demande de permission stockage
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btnChooseFolder = findViewById(R.id.btnChooseFolder)
        btnArchive = findViewById(R.id.btnArchive)
        switchAuto = findViewById(R.id.switchAutoArchive)



        // Bouton "Choisir un dossier" : Vérifier permission, puis ouvrir sélecteur
        btnChooseFolder.setOnClickListener {
            checkPermission()  // Appelle la méthode pour vérifier la permission
        }

        // Bouton "Lancer l'archivage" : Vérifier si dossier choisi, puis archiver
        btnArchive.setOnClickListener {
            if (folderUri == null) {  // Si aucun dossier choisi
                Toast.makeText(this, "Choisissez un dossier d'abord", Toast.LENGTH_SHORT).show()  // Message d'erreur
            } else {
                startManualArchive()  // Lance l'archivage manuel
            }
        }

        // Switch "Archivage automatique" : Activer/désactiver selon état
        switchAuto.setOnCheckedChangeListener { _, isChecked ->  // isChecked = true si activé, false si désactivé
            if (isChecked) {  // Si activé
                if (folderUri == null) {  // Mais pas de dossier choisi
                    Toast.makeText(this, "Choisissez un dossier avant", Toast.LENGTH_SHORT).show()
                    switchAuto.isChecked = false  // Désactiver automatiquement le switch
                } else {
                    startAutoArchive()  // Lancer l'automatique
                }
            } else {  // Si désactivé
                stopAutoArchive()  // Arrêter l'automatique
            }
        }
    }


    // MÉTHODES POUR LES PERMISSIONS (EasyPermissions)

    private fun checkPermission() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

            openFolderPicker()
        } else {

            EasyPermissions.requestPermissions(
                this,
                "L'application a besoin d'accéder au stockage",
                STORAGE_PERMISSION,  // Code de requête
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    // Ouvrir le sélecteur de dossier système
    private fun openFolderPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)  // Action pour choisir un dossier
        startActivityForResult(intent, PICK_FOLDER)  // Lance l'activité et attend le résultat
    }

    // Recevoir le résultat du sélecteur de dossier
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FOLDER && resultCode == Activity.RESULT_OK) {  // Si succès
            folderUri = data?.data  // Récupérer l'adresse du dossier choisi
            folderUri?.let {
                // Donner une permission persistante pour lire/écrire dans ce dossier
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                Toast.makeText(this, "Dossier sélectionné", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // MÉTHODES POUR L'ARCHIVAGE

    // Explication détaillée de l'archivage :
    // - L'archivage déplace les fichiers anciens  vers un dossier "Archive".
    // - Ça se fait en arrière-plan avec WorkManager pour ne pas bloquer l'app.
    // - "Manuel" = une fois, quand l'utilisateur clique.
    // - "Automatique" = toutes les 24 heures, même si l'app est fermée.
    // - Le vrai travail est fait par ArchiveWorker .

    // Lancer l'archivage manuel (une fois)
    private fun startManualArchive() {
        // Créer une "requête de travail" (WorkRequest) pour une exécution unique
        val workRequest = OneTimeWorkRequestBuilder<ArchiveWorker>()  // ArchiveWorker = la classe qui fait le travail
            .setInputData(workDataOf("folder_uri" to folderUri.toString()))  // Passer l'adresse du dossier au Worker
            .build()  // Construire la requête

        // Ajouter la requête à WorkManager pour exécution
        WorkManager.getInstance(this).enqueue(workRequest)
        Toast.makeText(this, "Archivage manuel lancé", Toast.LENGTH_SHORT).show()
    }

    // Lancer l'archivage automatique (toutes les 24 heures)
    private fun startAutoArchive() {
        // Définir des contraintes : quand exécuter (ex. : batterie pas faible)
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)  // Seulement si batterie > 15%
            .build()

        // Créer une requête périodique (répétée)
        val workRequest = PeriodicWorkRequestBuilder<ArchiveWorker>(
            24, TimeUnit.HOURS  // Intervalle : 24 heures (minimum 15 min pour WorkManager)
        )
            .setConstraints(constraints)  // Appliquer les contraintes
            .setInputData(workDataOf("folder_uri" to folderUri.toString()))  // Passer le dossier
            .build()

        // Ajouter comme travail unique (remplace si existe)
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AUTO_ARCHIVE",  // Nom unique
            ExistingPeriodicWorkPolicy.REPLACE,  // Remplacer si déjà existe
            workRequest
        )

        Toast.makeText(this, "Archivage automatique activé", Toast.LENGTH_SHORT).show()
    }

    // Arrêter l'archivage automatique
    private fun stopAutoArchive() {
        WorkManager.getInstance(this).cancelUniqueWork("AUTO_ARCHIVE")  // Annuler par nom
        Toast.makeText(this, "Archivage automatique désactivé", Toast.LENGTH_SHORT).show()
    }


    // MÉTHODES POUR LES CALLBACKS EASY PERMISSIONS
    // Permissions accordées
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == STORAGE_PERMISSION) {
            openFolderPicker()
        }
    }

    // Permissions refusées
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            // Refus définitif → Ouvrir dialogue pour aller dans paramètres
            AppSettingsDialog.Builder(this).build().show()
        } else {
            Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show()
        }
    }

    // Résultat de la demande de permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}
package com.example.archiver

import com.example.archiver.utils.ArchiveUtils
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ArchiveUtilsTest {

    private lateinit var archiveUtils: ArchiveUtils

    @Before
    fun setup() {
        archiveUtils = ArchiveUtils()
    }

    // =========================
    // Test isOldFile()
    // =========================

    @Test
    fun isOldFile_fichierAncien_retourneTrue() {
        // Arrange
        val currentTime = 10_000L
        val lastModified = 1_000L
        val limit = 5_000L

        // Act
        val result = archiveUtils.isOldFile(currentTime, lastModified, limit)

        // Assert
        assertTrue(result)
    }

    @Test
    fun isOldFile_fichierRecent_retourneFalse() {
        val currentTime = 10_000L
        val lastModified = 8_000L
        val limit = 5_000L

        val result = archiveUtils.isOldFile(currentTime, lastModified, limit)

        assertFalse(result)
    }

    // =========================
    // Test shouldArchive()
    // =========================

    @Test
    fun shouldArchive_fichiersExistants_retourneTrue() {
        val result = archiveUtils.shouldArchive(3)
        assertTrue(result)
    }

    @Test
    fun shouldArchive_aucunFichier_retourneFalse() {
        val result = archiveUtils.shouldArchive(0)
        assertFalse(result)
    }
}

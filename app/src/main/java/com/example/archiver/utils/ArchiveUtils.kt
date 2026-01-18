package com.example.archiver.utils

class ArchiveUtils {

    fun isOldFile(
        currentTime: Long,
        lastModified: Long,
        limitMillis: Long
    ): Boolean {
        return (currentTime - lastModified) > limitMillis
    }

    fun shouldArchive(fileCount: Int): Boolean {
        return fileCount > 0
    }
}

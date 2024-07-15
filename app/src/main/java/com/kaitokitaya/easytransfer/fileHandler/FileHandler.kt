package com.kaitokitaya.easytransfer.fileHandler

import android.health.connect.datatypes.units.Length
import android.os.Environment
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object FileHandler {

    fun getAllList(targetRote: String = ""): List<File>? {
        val root = Environment.getExternalStorageDirectory().absolutePath
        val directory = File(root, targetRote)
//        val directories = directory.listFiles( FileFilter {
//            it.isDirectory
//        })?.toList()
        return directory.listFiles()?.toList()
    }

    fun getAllFilesFromAbsPath(absPath: String): List<File>? {
        val files = File(absPath)
        return files.listFiles()?.toList()
    }

    fun getParentPath(path: String): String {
        return path.substringBeforeLast("/")
    }

    // Judge whether it is necessary to convert file name because of containing special character.
    fun convertFileName(fileName: String): String {
        return URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
    }

    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast(".", missingDelimiterValue = "")
    }

    fun getShortName(input: String, maxLength: Int): String {
        return if (input.length > maxLength) {
            return "${input.take(maxLength - 3)}･･･"
        } else {
            input
        }
    }

}
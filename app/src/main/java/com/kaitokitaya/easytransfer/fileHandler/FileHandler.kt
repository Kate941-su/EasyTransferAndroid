package com.kaitokitaya.easytransfer.fileHandler

import android.os.Environment
import java.io.File

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



}
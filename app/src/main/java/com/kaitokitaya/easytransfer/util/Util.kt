package com.kaitokitaya.easytransfer.util

import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date

object Util {
    fun getDataTime(epoch: Long): String {
        val dateFormatter = SimpleDateFormat("MM/dd/yyyy")
        val newDate = Date(epoch)
        return dateFormatter.format(newDate)
    }
}
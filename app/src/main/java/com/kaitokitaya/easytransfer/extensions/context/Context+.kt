package com.kaitokitaya.easytransfer.extensions.context

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import timber.log.Timber

fun Context.sendEmail(to: String, subject: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "vnd.android.cursor.item/email"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // TODO: Error Handling
        Timber.tag("Context.sendEmail").d(e)
    } catch(e: Throwable) {
        // TODO: Error Handling
        Timber.tag("Context.sendEmail").d(e)
    }
}
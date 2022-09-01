package com.potyvideo.library.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

val Int.MB: Int
    get() = this * 1_000_000

fun Context.getActivity(): Activity? {
    return if (this is ContextWrapper) {
        if (this is Activity) this else baseContext.getActivity()
    } else null
}
package com.ifarbod.myapplication

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import java.lang.Class
import java.lang.reflect.Field
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : Activity() {
    companion object {
        const val TAG = "Zargun"
        private const val TEXT_SIZE = 22.0f
    }

    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        textView = TextView(this).apply {
            setTextIsSelectable(true)
            gravity = Gravity.CENTER
            textSize = TEXT_SIZE
            text = "One UI"
        }

        setContentView(textView)

        printInfo()
    }

    private fun printInfo() {
        textView.text = null

        val release = getProp("ro.build.version.release_or_codename")
        val sdk = getProp("ro.build.version.sdk")

        textView.append("Raw data\n")
        textView.append("Release: $release, SDK: $sdk\n")
        textView.append("\nSecurity patch:")
        textView.append(getProp("ro.build.version.security_patch"))
        textView.append("\nSEM: ")
        textView.append(getProp("ro.build.version.sem"))
        textView.append("\nSEP: ")
        textView.append(getProp("ro.build.version.sep"))
        textView.append("\nOne UI: ")
        textView.append(getProp("ro.build.version.oneui"))
        textView.append("\nSEP category: ")
        textView.append(getFloatingFeature("SEC_FLOATING_FEATURE_COMMON_CONFIG_SEP_CATEGORY"))
        textView.append("\nBranding name: ")
        textView.append(getFloatingFeature("SEC_FLOATING_FEATURE_SETTINGS_CONFIG_BRAND_NAME"))

        textView.append("\nProcessed data\n")
        textView.append("\nOne UI: ${isOneUI()}")
        textView.append("\nSamsung Experience: ${isSamsungExperience()}")
        textView.append("\n")
        textView.append("One UI ")

        if (isOneUiCore())
        {
            textView.append("Core ")
        }

        textView.append(parseOneUiVersion())
        textView.append("\n")
    }    

    private fun getProp(name: String): String? {
        val line: String?
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop $name")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
        } catch (ex: IOException) {
            Log.e(TAG, "Unable to read prop $name", ex)
            return null
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return line
    }

    private fun isOneUI(): Boolean {
        try {
            val f: Field = Build.VERSION::class.java.getDeclaredField("SEM_PLATFORM_INT")
            f.isAccessible = true
            val semPlatformInt = f.get(null) as Int
            if (semPlatformInt < 100000) {
                // Samsung Experience then
                return false
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun isSamsungExperience(): Boolean {
        try {
            val f: Field = Build.VERSION::class.java.getDeclaredField("SEM_PLATFORM_INT")
            f.isAccessible = true
            val semPlatformInt = f.get(null) as Int
            if (semPlatformInt < 100000) {
                // Samsung Experience then
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    private fun isOneUiCore(): Boolean {
        return getFloatingFeature("SEC_FLOATING_FEATURE_COMMON_CONFIG_SEP_CATEGORY").equals("sep_lite_new")
    }

    private fun getOneUiVersion(): Int {
        try {
            val f: Field = Build.VERSION::class.java.getDeclaredField("SEM_PLATFORM_INT")
            f.isAccessible = true
            val semPlatformInt = f.get(null) as Int
            return semPlatformInt
        } catch (e: Exception) {
            return 0
        }
    }

    private fun parseOneUiVersion(): String {
        val oneUiOwnVersion = getOneUiOwnVersion()
        //val oneUiOwnVersion = 40_100

        if (oneUiOwnVersion > 0) {
            val major = oneUiOwnVersion / 10_000
            val minor = (oneUiOwnVersion % 10_000) / 100
            val patch = oneUiOwnVersion % 100
            if (patch == 0)
            {
                return "$major.$minor"
            }
            return "$major.$minor.$patch"
        }

        val ONE_UI_VERSION_SEP_VERSION_GAP: Int = 90_000
        val oneuiVer: Int = getOneUiVersion() - ONE_UI_VERSION_SEP_VERSION_GAP

        val major = oneuiVer / 10_000
        val minor = (oneuiVer % 10_000) / 100

        return "$major.$minor"
    }

    private fun getOneUiOwnVersion(): Int {
        val oneui = getProp("ro.build.version.oneui")

        if (oneui.isNullOrBlank())
        {
            return 0
        }

        return oneui.toInt()
    }

    private fun getFloatingFeature(feature: String): String {
        val semFloatingFeatureClass =
                Class.forName("com.samsung.android.feature.SemFloatingFeature")
        val getInstance = semFloatingFeatureClass.getMethod("getInstance")
        val instance = getInstance.invoke(null)
        // hidden_getString on Q+, getString otherwise
        val getString =
                semFloatingFeatureClass.getMethod(
                        "getString",
                        String::class.java
                )

        val string =
                getString.invoke(
                        instance,
                        feature
                ) as
                        String
        return string
    }

    // TODO(iFarbod): check com.samsung.feature.samsung_experience_mobile and com.samsung.feature.samsung_experience_mobile_lite
}

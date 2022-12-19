package com.ifarbod.myapplication

import android.annotation.SuppressLint
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

class MainActivity : Activity()
{
    companion object
    {
        private const val TAG = "Zargun"
        private const val TEXT_SIZE = 22.0f
    }

    private lateinit var textView: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?)
    {
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

    private fun printInfo()
    {
        textView.text = null

        // ro.build.version.release_or_preview_display (13+)
        // ro.build.version.release_or_codename (11+)
        // check codename, if REL => stable
        val release = if (getProp("ro.build.version.release_or_preview_display").isNullOrBlank())
        {
            if (getProp("ro.build.version.release_or_codename").isNullOrBlank())
            {
                getProp("ro.build.version.release")
            }
            else
            {
                getProp("ro.build.version.release_or_codename")
            }
        }
        else
        {
            getProp("ro.build.version.release_or_preview_display")
        }
        val sdk = getProp("ro.build.version.sdk")?.ifBlank { "?" }

        textView.append("Raw data\n\n")
        textView.append("Release: $release, SDK: $sdk\n")
        textView.append("\nSecurity patch: ")
        textView.append(getProp("ro.build.version.security_patch")?.ifBlank { "?" })
        textView.append("\nSEM: ")
        textView.append(getProp("ro.build.version.sem")?.ifBlank { "?" })
        textView.append("\nSEP: ")
        textView.append(getProp("ro.build.version.sep")?.ifBlank { "?" })
        textView.append("\nOne UI: ")
        textView.append(getProp("ro.build.version.oneui")?.ifBlank { "?" })
        textView.append("\nSEP category: ")
        textView.append(getFloatingFeature("SEC_FLOATING_FEATURE_COMMON_CONFIG_SEP_CATEGORY").ifBlank { "?" })
        textView.append("\nBranding name: ")
        textView.append(getFloatingFeature("SEC_FLOATING_FEATURE_SETTINGS_CONFIG_BRAND_NAME").ifBlank { "?" })

        textView.append("\n\nProcessed data\n\n")
        textView.append("\nOne UI: ${isOneUI()}")
        textView.append("\nSamsung Experience: ${isSamsungExperience()}, ${hasSepFeature()}")
        textView.append("\n")
        textView.append("One UI ")

        if (isOneUiCore())
        {
            textView.append("Core ")
        }

        textView.append(parseOneUiVersion())
        textView.append("\n")
    }

    private fun getProp(name: String): String?
    {
        val line: String?
        var input: BufferedReader? = null
        try
        {
            val p = Runtime.getRuntime().exec("getprop $name")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
        }
        catch (ex: IOException)
        {
            Log.e(TAG, "Unable to read prop $name", ex)
            return null
        }
        finally
        {
            if (input != null)
            {
                try
                {
                    input.close()
                }
                catch (e: IOException)
                {
                    e.printStackTrace()
                }
            }
        }
        return line
    }

    private fun isOneUI(): Boolean
    {
        try
        {
            val f: Field = Build.VERSION::class.java.getDeclaredField("SEM_PLATFORM_INT")
            f.isAccessible = true
            val semPlatformInt = f.get(null) as Int
            if (semPlatformInt < 100000)
            {
                // Samsung Experience then
                return false
            }
            return true
        }
        catch (e: Exception)
        {
            return false
        }
    }

    private fun isSamsungExperience(): Boolean
    {
        try
        {
            val f: Field = Build.VERSION::class.java.getDeclaredField("SEM_PLATFORM_INT")
            f.isAccessible = true
            val semPlatformInt = f.get(null) as Int
            if (semPlatformInt < 100000)
            {
                // Samsung Experience then
                return true
            }
            return false
        }
        catch (e: Exception)
        {
            return false
        }
    }

    private fun isOneUiCore(): Boolean
    {
        return getFloatingFeature("SEC_FLOATING_FEATURE_COMMON_CONFIG_SEP_CATEGORY") == "sep_lite_new"
    }

    private fun getOneUiVersion(): Int
    {
        return try
        {
            val f: Field = Build.VERSION::class.java.getDeclaredField("SEM_PLATFORM_INT")
            f.isAccessible = true
            val semPlatformInt = f.get(null) as Int
            semPlatformInt
        }
        catch (e: Exception)
        {
            0
        }
    }

    private fun parseOneUiVersion(): String
    {
        val oneUiOwnVersion = getOneUiOwnVersion()
        //val oneUiOwnVersion = 40_100

        if (oneUiOwnVersion > 0)
        {
            val major = oneUiOwnVersion / 10_000
            val minor = (oneUiOwnVersion % 10_000) / 100
            val patch = oneUiOwnVersion % 100
            if (patch == 0)
            {
                return "$major.$minor"
            }
            return "$major.$minor.$patch"
        }

        val ONE_UI_VERSION_SEP_VERSION_GAP = 90_000
        val oneuiVer: Int = getOneUiVersion() - ONE_UI_VERSION_SEP_VERSION_GAP

        val major = oneuiVer / 10_000
        val minor = (oneuiVer % 10_000) / 100

        return "$major.$minor"
    }

    private fun getOneUiOwnVersion(): Int
    {
        val oneui = getProp("ro.build.version.oneui")

        if (oneui.isNullOrBlank())
        {
            return 0
        }

        return oneui.toInt()
    }

    private fun getFloatingFeature(feature: String): String
    {
        val semFloatingFeatureClass = Class.forName("com.samsung.android.feature.SemFloatingFeature")
        val getInstance = semFloatingFeatureClass.getMethod("getInstance")
        val instance = getInstance.invoke(null)
        // hidden_getString on Q+, getString otherwise
        val getString = semFloatingFeatureClass.getMethod(
            "getString", String::class.java
        )

        return getString.invoke(
            instance, feature
        ) as String
    }

    private fun hasSepFeature(): Boolean
    {
        return packageManager.hasSystemFeature("com.samsung.feature.samsung_experience_mobile") || packageManager.hasSystemFeature(
            "com.samsung.feature.samsung_experience_mobile_lite"
        )
    }

}

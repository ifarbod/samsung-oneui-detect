package com.ifarbod.myapplication

import android.app.Activity
import android.os.Build
import android.os.Bundle
import java.lang.Class
import java.lang.reflect.Field

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        System.out.println(getFloatingFeature())
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
        val ONE_UI_VERSION_SEP_VERSION_GAP: Int = 90000
        val oneuiVer: Int = getOneUiVersion() - ONE_UI_VERSION_SEP_VERSION_GAP

        val major = oneuiVer / 10000
        val minor = (oneuiVer % 10000) / 100

        return "$major.$minor"
    }

    // todo: getOneUiOwnVersion: SystemProperties.getInt("ro.build.version.oneui", 0);

    private fun getFloatingFeature(): String {
        val semFloatingFeatureClass =
                Class.forName("com.samsung.android.feature.SemFloatingFeature")
        val getInstance = semFloatingFeatureClass.getMethod("getInstance")
        val instance = getInstance.invoke(null)
        // hidden_getString on Q+, getString otherwise
        val getString =
                semFloatingFeatureClass.getMethod(
                        "getString",
                        String::class.java,
                        String::class.java
                )

        val string =
                getString.invoke(
                        instance,
                        "SEC_FLOATING_FEATURE_COMMON_CONFIG_SEP_CATEGORY",
                        "hey"
                ) as
                        String
        return string
    }
}

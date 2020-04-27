package org.ranjanistic.skivvy.manager

import android.content.res.Resources
import org.ranjanistic.skivvy.R
import org.ranjanistic.skivvy.Skivvy

class InputSpeechManager(val resources: Resources,val skivvy: Skivvy) {

    fun removeBeforeNegative(line: String): String {
        var l = String()
        for (r in arrayOf(
            resources.getStringArray(R.array.disruptions),
            resources.getStringArray(R.array.denials)
        )) {
            for (k in r) {
                if (line.contains(k)) {
                    l = line.replaceBeforeLast(k, "").replace(k, "").trim()
                }
            }
        }
        return l
    }

    fun removeBeforePositive(line: String): String {
        var l = String()
        for (k in resources.getStringArray(R.array.acceptances)) {
            if (line.contains(k)) {
                l = line.replaceBeforeLast(k, "").replace(k, "").trim()
            }
        }
        return l
    }

    fun containsAgreement(line: String): Boolean {
        for (k in resources.getStringArray(R.array.acceptances)) {
            if (line.contains(k)) {
                return true
            }
        }
        return false
    }

    fun containsDisagreement(line: String): Boolean {
        for (r in arrayOf(
            resources.getStringArray(R.array.disruptions),
            resources.getStringArray(R.array.denials)
        )) {
            for (k in r) {
                if (line.contains(k)) {
                    return true
                }
            }
        }
        return false
    }
}
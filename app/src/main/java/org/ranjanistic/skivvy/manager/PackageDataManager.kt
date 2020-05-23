package org.ranjanistic.skivvy.manager

import android.content.Intent
import android.graphics.drawable.Drawable
import org.ranjanistic.skivvy.Skivvy
import java.util.*
import kotlin.collections.ArrayList

/**
 * Device packages manager class, to hold the details of packages
 * on the device for direct supply to Skivvy on demand.
 * @author Priyanshu Ranjan
 */

class PackageDataManager(val skivvy: Skivvy) {
    private val pData = skivvy.packageDataManager
    private var packagesAppName: Array<String?>? = null
    fun packageAppName(): Array<String?>? = this.packagesAppName
    private var packagesName: Array<String?>? = null
    fun packagesName(): Array<String?>? = this.packagesName
    private var packagesMain: Array<Intent?>? = null
    fun packagesMain(): Array<Intent?>? = this.packagesMain
    private lateinit var packagesIcon: Array<Drawable?>
    private var packagesTotal: Int = 0

    //For receiving package manager data at instant
    data class PackageData(val size: Int) {
        var appName: Array<String?> = arrayOfNulls(size)
        var appPackage: Array<String?> = arrayOfNulls(size)
        var appIntent: Array<Intent?> = arrayOfNulls(size)
        var appIcon: Array<Drawable?> = arrayOfNulls(size)

    }

    fun setTotalPackages(totalPackages: Int) {
        this.packagesTotal = totalPackages
    }

    fun getTotalPackages(): Int = this.packagesTotal

    fun setPackagesDetail(packageData: PackageData) {
        this.packagesAppName = packageData.appName
        this.packagesIcon = packageData.appIcon
        this.packagesMain = packageData.appIntent
        this.packagesName = packageData.appPackage
    }

    fun getPackageAppName(index: Int): String? {
        return this.packagesAppName?.get(index)
    }

    fun getPackageName(index: Int): String? {
        return this.packagesName?.get(index)
    }

    fun getPackageIntent(index: Int): Intent? {
        return packagesMain?.get(index)
    }

    fun getPackageIcon(index: Int): Drawable? {
        return packagesIcon[index]
    }

    @ExperimentalStdlibApi
    fun appNameOfPackage(packageName: String): String? {
        var index = 0
        while (index < pData.getTotalPackages()) {
            if (packageName == pData.getPackageName(index))
                pData.getPackageAppName(index)?.let { return it.capitalize(skivvy.locale) }
            ++index
        }
        return null
    }

    fun iconOfPackage(packageName: String? = null, appName: String? = null): Drawable? {
        var index = 0
        while (index < pData.getTotalPackages()) {
            if (packageName != null) {
                if (packageName == pData.getPackageName(index))
                    pData.getPackageIcon(index)?.let { return it }
            }
            if (appName != null) {
                if (appName == pData.getPackageAppName(index))
                    pData.getPackageIcon(index)?.let { return it }
            }
            ++index
        }
        return null
    }

    fun isThereAnyAppNamed(name: String, startFrom: Int = 0): Boolean {
        var index = startFrom
        while (index < pData.getTotalPackages()) {
            if (name == pData.getPackageAppName(index)) {
                return true
            }
            ++index
        }
        return false
    }

    fun indicesOfAppsBeginningWith(letter: Char): ArrayList<Int> {
        val indices: ArrayList<Int> = ArrayList()
        var index = 0
        while (index < pData.getTotalPackages()) {
            if (pData.getPackageAppName(index)?.get(0) == letter) {
                indices.add(index)
            }
            ++index
        }
        return indices
    }
}
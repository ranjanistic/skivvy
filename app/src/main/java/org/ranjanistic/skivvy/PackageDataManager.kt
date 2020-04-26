package org.ranjanistic.skivvy

import android.content.Intent
import android.graphics.drawable.Drawable

/**
 * Device packages manager class, to hold the details of packages
 * on the device for direct supply to Skivvy on demand.
 * @author Priyanshu Ranjan
 */

class PackageDataManager {
    private lateinit var packagesAppName: Array<String?>
    private lateinit var packagesName: Array<String?>
    private lateinit var packagesMain: Array<Intent?>
    private lateinit var packagesIcon: Array<Drawable?>
    private var packagesTotal: Int = 0

    //For receiving package manager data at instant
    data class PackageData(val size:Int){
        var appName: Array<String?> = arrayOfNulls(size)
        var appPackage: Array<String?> = arrayOfNulls(size)
        var appIntent: Array<Intent?> = arrayOfNulls(size)
        var appIcon: Array<Drawable?> = arrayOfNulls(size)
    }

    fun setTotalPackages(totalPackages:Int){
        this.packagesTotal = totalPackages
    }
    fun getTotalPackages():Int{
        return this.packagesTotal
    }
    fun setPackagesDetail(packageData: PackageData){
        this.packagesAppName = packageData.appName
        this.packagesIcon = packageData.appIcon
        this.packagesMain = packageData.appIntent
        this.packagesName = packageData.appPackage
    }
    fun getPackageAppName(index:Int):String?{
        return this.packagesAppName[index]
    }
    fun getPackageName(index:Int):String?{
        return this.packagesName[index]
    }
    fun getPackageIntent(index:Int):Intent?{
        return packagesMain[index]
    }
    fun getPackageIcon(index: Int):Drawable?{
        return packagesIcon[index]
    }
}
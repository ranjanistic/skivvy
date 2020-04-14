package org.ranjanistic.skivvy

import android.content.Intent
import android.graphics.drawable.Drawable

class PackageData {
    private lateinit var packagesAppName: Array<String?>
    private lateinit var packagesName: Array<String?>
    private lateinit var packagesMain: Array<Intent?>
    private lateinit var packagesIcon: Array<Drawable?>
    private var packagesTotal: Int = 0
    fun setTotalPackages(totalPackages:Int){
        this.packagesTotal = totalPackages
    }
    fun getTotalPackages():Int{
        return this.packagesTotal
    }
    fun setPackageInitials(appNames:Array<String?>, packageNames:Array<String?>,packageIntents:Array<Intent?>,packageIcons:Array<Drawable?>){
        this.packagesAppName = appNames
        this.packagesName = packageNames
        this.packagesMain = packageIntents
        this.packagesIcon = packageIcons
    }
    fun setPackageDetails(index:Int,appName:String, packageName:String,packageIntent:Intent,packageIcon:Drawable){
        this.packagesAppName[index] = appName
        this.packagesName[index] = packageName
        this.packagesMain[index] = packageIntent
        this.packagesIcon[index] = packageIcon
    }
    fun getPackageAppNames():Array<String?>{
        return this.packagesAppName
    }
    fun getPackageNames():Array<String?>{
        return this.packagesName
    }
    fun getPackageIntents():Array<Intent?>{
        return packagesMain
    }
    fun getPackageIcons():Array<Drawable?>{
        return packagesIcon
    }
}
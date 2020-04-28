package org.ranjanistic.skivvy.manager

import android.content.res.Resources
import org.ranjanistic.skivvy.R
import org.ranjanistic.skivvy.Skivvy
import kotlin.math.PI

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

    fun containsDisruption(line:String):Boolean{
        for (k in resources.getStringArray(R.array.disruptions)) {
            if (line.contains(k)) {
                return true
            }
        }
        return false
    }

    fun expressionize(expression: String): String {
        var finalExpression = expression
        val toBeRemoved = arrayOf(
            " ", "calculate", "compute", "solve", "whatis",
            "what's", "tellme", "thevalueof", "valueof"
        )
        val toBePercented = arrayOf("%of", "percentof")
        val toBeModded = arrayOf("%", "mod")
        val toBeLogged = arrayOf("naturallogof", "naturallog")
        val toBeLog = arrayOf("logof")
        val toBeMultiplied = arrayOf("x", "multipliedby", "times","into", "and")
        val toBeDivided = arrayOf("dividedby", "by", "upon", "over", "÷", "divideby", "divide")
        val toBeAdded = arrayOf("add", "plus", "or")
        val toBeSubtracted = arrayOf("minus", "negative", "subtract")
        val toBeNumerized = arrayOf("hundred")
        val toBePowered = arrayOf(
            "raisedtothepowerof", "raisetothepowerof", "raisedtothepower", "raisetothepower",
            "tothepowerof", "tothepower", "raisedto", "raiseto", "raised", "raise", "kipower"
        )
        val toBeCuberooted = arrayOf("cuberoot", "thirdroot")
        val toBeRooted = arrayOf("squareroot", "root", "secondroot")
        val toBeSquared = arrayOf("square")
        val toBeCubed = arrayOf("cube")
        val toBeZeroed = arrayOf("zero")
        val toBePI = arrayOf("pi")
        val formatArrays = arrayOf(
            toBeRemoved, toBePercented, toBeModded, toBeLogged, toBeLog,
            toBeMultiplied, toBeDivided, toBeAdded, toBeSubtracted, toBeNumerized
            , toBePowered, toBeCuberooted, toBeRooted, toBeSquared, toBeCubed, toBeZeroed,
            toBePI
        )
        val replacingArray =
            arrayOf(
                "", "p", "m", "ln", "log", "*", "/", "+",
                "-", "100", "^", "cbrt", "sqrt", "^2", "^3","0",
                PI.toString()
            )
        var formatIndex = 0
        while (formatIndex < formatArrays.size) {
            var formatArrayIndex = 0
            while (formatArrayIndex < formatArrays[formatIndex].size) {
                finalExpression = finalExpression.replace(
                    formatArrays[formatIndex][formatArrayIndex],
                    replacingArray[formatIndex]
                )
                ++formatArrayIndex
            }
            ++formatIndex
        }
        return finalExpression
    }

}
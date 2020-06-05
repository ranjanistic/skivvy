package org.ranjanistic.skivvy.manager

import android.util.Log
import java.util.*
import kotlin.math.PI

class InputSpeechManager {

    private val nothing = ""
    private val space = " "

    fun removeStringsIn(line: String, stringListArray: Array<Array<String>>): String {
        var l = line
        for (r in stringListArray) {
            for (k in r) {
                l = l.replace(k, nothing)
            }
        }
        return l.replace(space, nothing)
    }

    /**Removes everything before (and including, if [excludeLast] is false) any word  from [stringListArray] occurring in [line] at last, and returns the remaining.
     * @param line: The given string which is to be truncated according to [stringListArray].
     * @param stringListArray:The array of list of strings from which [line] will be truncated as per the last occurrence of any string from it in [line].
     * @param excludeLast: Whether to exclude the removal of last occurrence of string from [stringListArray] in [line] or not.
     * @return: The truncated string having remaining data as string or null.
     */
    fun removeBeforeLastStringsIn(
        line: String,
        stringListArray: Array<Array<String>>,
        excludeLast: Boolean = false
    ): String {
        var l = line
        for (r in stringListArray) {
            for (k in r) {
                if (l.contains(k)) {
                    l = if (excludeLast) l.replaceBeforeLast(k, nothing).trim()
                    else l.replaceBeforeLast(k, nothing).replace(k, nothing).trim()
                    Log.d(TAG, "removebefore: left = $l")
                }
            }
        }
        return l.trim()
    }

    fun isStringInArray(line:String, stringListArray: Array<Array<String>>):Boolean{
        for(r in stringListArray){
            for(k in r){
                if(k.toLowerCase(Locale.getDefault()) == line.toLowerCase(Locale.getDefault()))
                    return true
            }
        }
        return false
    }

    /**If given [line] contains any string in given [stringListArray], return true, else false
     * @param line : The given string which is to be inspected.
     * @param stringListArray: The array of list of strings from which [line] will be inspected for occurrence.
     * @param isSingle: If given [line] is to be treated as single response or not (avoids possibility of multiple valid responses in string).
     * @return: Returns boolean according to the occurrence of any string from [stringListArray] in given [line].
     * @note: The space is padded in [line] whenever it is singular [isSingle] (i.e., with no spaces),
     * and it shouldn't be a substring of any other string.
     * For example - 'no' in knowledge cannot be treated as a valid response, therefore ' no ' is checked for occurrence.
     */
    fun containsString(
        line: String,
        stringListArray: Array<Array<String>>,
        isSingleLine: Boolean = false
    ): Boolean {
        Log.d(TAG, "checking contains in $line")
        for (r in stringListArray) {
            loop@ for (k in r) {
                return when (isSingleLine) {
                    true -> {
                        if (" $line ".contains(k))
                            true
                        else continue@loop
                    }
                    false -> {
                        if (line.contains(k)) {
                            Log.d(TAG, "contains $k in $line")
                            true
                        } else continue@loop
                    }
                }
            }
        }
        return false
    }

    //TODO: setting icon attribute for different themes
    //TODO: Check for substring after contained string
    fun finallySaidLineFromList(line: String, stringListArray: Array<Array<String>>)
            : Boolean = containsString(
        removeBeforeLastStringsIn(line, stringListArray, true).trim(),
        stringListArray
    )


    fun removeDuplicateStrings(stringArray: Array<String?>): Array<String?> {
        var size = stringArray.size
        var i = 0
        while (i < size) {
            var j = i + 1
            while (j < size) {
                if (stringArray[i] == stringArray[j]) {
                    stringArray[j] = nothing
                }
                ++j
            }
            ++i
        }
        for (k in stringArray) {
            if (k == nothing || k == null) {
                --size
            }
        }
        var count = 0
        val newArray = arrayOfNulls<String>(size)
        for (k in stringArray) {
            if (k != nothing && k != null) {
                newArray[count] = k
                ++count
            }
        }
        return newArray
    }

    /**
     *  This returns the index of the array of strings from [stringListArray], if any string of it occurs at last in the [line]
     *  else returns null.
     *  @param line The line to be checked for occurrence
     *  @param stringListArray The array of array of strings, from which the occurrence in [line] is to be checked.
     */
    data class Remaining(var index: Int = -1, var remaining: String = "")

    val TAG = "indexCheck"
    fun indexOfFinallySaidArray(line: String, stringListArray: Array<Array<String>>): Remaining {
        val remaining = removeBeforeLastStringsIn(line, stringListArray, true)
        Log.d(TAG, "indexoffilnnaly: remaining $remaining")
        for ((index, k) in stringListArray.withIndex()) {
            if (containsString(remaining, arrayOf(k))) {
                return Remaining(index, remaining)
            }
        }
        return Remaining(-1, remaining)
    }

    //formats given string to expression form, irrespective of it is mathematical expression or not
    fun expressionize(expression: String): String {
        var finalExpression = expression
        val toBeRemoved = arrayOf(
            space, "calculate", "compute", "solve", "whatis",
            "what's", "tellme", "thevalueof", "valueof"
        )
        val toBeFactorial = arrayOf("factorialof", "factorial")
        val toBePercented = arrayOf("%of", "percentof")
        val toBeModded = arrayOf("%", "mod")
        val toBeLogged =
            arrayOf("naturallogarithmof", "naturallogarithm", "naturallogof", "naturallog")
        val toBeLog = arrayOf("logarithmof", "logarithm", "logof")
        val toBeMultiplied = arrayOf("x", "multipliedby", "times", "into")
        val toBeDivided = arrayOf("dividedby", "by", "upon", "over", "÷", "divideby", "divide")
        val toBeAdded = arrayOf("add", "plus")
        val toBeSubtracted = arrayOf("minus", "negative", "subtract")
        val toBePowered = arrayOf(
            "raisedtothepowerof", "raisetothepowerof", "raisedtothepower", "raisetothepower",
            "tothepowerof", "tothepower", "raisedto", "raiseto", "raised", "raise", "kipower"
        )
        val toBeCuberooted = arrayOf("cuberoot", "thirdroot")
        val toBeRooted = arrayOf("squareroot", "root", "secondroot")
        val toBeSquared = arrayOf("square")
        val toBeCubed = arrayOf("cube")
        val toBePI = arrayOf("pi")

        val formatArrays = arrayOf(
            toBeRemoved, toBeFactorial, toBePercented, toBeModded, toBeLogged, toBeLog,
            toBeMultiplied, toBeDivided, toBeAdded, toBeSubtracted, toBePowered,
            toBeCuberooted, toBeRooted, toBeSquared, toBeCubed, toBePI
        )
        val replacingArray =
            arrayOf(
                nothing, "fact", "p", "m", "ln", "log", "*", "/", "+",
                "-", "^", "cbrt", "sqrt", "^2", "^3",
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
        val toBeNumbered = arrayOf("zero", "one", "two", "hundred", "thousand")
        val numbers = arrayOf("0", "1", "2", "100", "1000")
        formatIndex = 0
        while (formatIndex < numbers.size) {
            finalExpression =
                finalExpression.replace(toBeNumbered[formatIndex], numbers[formatIndex])
            ++formatIndex
        }
        return finalExpression
    }

}
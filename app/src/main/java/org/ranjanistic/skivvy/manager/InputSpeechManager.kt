package org.ranjanistic.skivvy.manager

import kotlin.math.PI

class InputSpeechManager {

    private val nothing = ""
    private val space = " "

    fun removeStringsIn(line:String, stringListArray: Array<Array<String>>):String{
        var l  = line
        for(r in stringListArray){
            for(k in r){
                l = l.replace(k, nothing)
            }
        }
        return l.replace(space, nothing)
    }

    /**Removes everything before (and including) any word  from [stringListArray] occurring in [line] at last, and returns the remaining.
     * @param line: The given string which is to be truncated according to [stringListArray].
     * @param stringListArray:The array of list of strings from which [line] will be truncated as per the last occurrence of any string from it in [line].
     * @return: The truncated string having remaining data as string or null.
     */
    fun removeBeforeLastStringsIn(line: String, stringListArray: Array<Array<String>>): String {
        var l = String()
        for (r in stringListArray) {
            for (k in r) {
                if (line.contains(k)) {
                    l = line.replaceBeforeLast(k, nothing).replace(k, nothing).trim()
                }
            }
        }
        return l.trim()
    }

    /**If given [line] contains any string in given [stringListArray], return true, else false
     * @param line : The given string which is to be inspected.
     * @param stringListArray: The array of list of strings from which [line] will be inspected for occurrence.
     * @param isSingle: If given [line] is to be treated as single response or not (avoids possibility of multiple valid responses in string).
     * @return: Returns boolean according to the occurrence of any string from [stringListArray] in given [line].
     */
    fun containsString(
        line: String,
        stringListArray: Array<Array<String>>,
        isSingle: Boolean = false
    ): Boolean {
        for (r in stringListArray) {
            loop@ for (k in r) {
                return when (isSingle) {
                    true -> {
                        if (line.contains(" $k "))
                            true
                        else continue@loop
                    }
                    false ->{
                        if (line.contains(k))
                            true
                        else continue@loop
                    }
                }
            }
        }
        return false
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
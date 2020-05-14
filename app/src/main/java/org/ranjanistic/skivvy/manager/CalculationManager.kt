package org.ranjanistic.skivvy.manager

import org.ranjanistic.skivvy.R
import org.ranjanistic.skivvy.Skivvy
import kotlin.math.*

@ExperimentalStdlibApi
class CalculationManager(var skivvy: Skivvy) {
    private val nothing = ""
    val mathFunctions = arrayOf(
        "sin",
        "cos",
        "tan",
        "cot",
        "sec",
        "cosec",
        "log",
        "ln",
        "sqrt",
        "cbrt",
        "exp",
        "fact"
    )
    val operators = arrayOf("^", "p", "/", "*", "m", "-", "+")
    fun totalOperatorsInExpression(expression: String): Int {
        var expIndex = 0
        var totalOps = 0
        while (expIndex < expression.length) {
            var opIndex = 0
            while (opIndex < this.operators.size) {
                if (expression[expIndex].toString() == this.operators[opIndex]) {
                    ++totalOps              //counting total
                }
                ++opIndex
            }//5+5*6/2+3*4+cos60
            ++expIndex
        }
        return totalOps
    }

    /**
     *  The following block stores the position of operators in the given expression
     *  in  a new array (of Integers), irrespective of repetition of operators.
     *  @param expression: the expression string
     *  @return the array of positions of operators in given string
     */
    fun positionsOfOperatorsInExpression(expression: String): Array<Int?> {
        var expIndex = 0
        val expOperatorPos = arrayOfNulls<Int>(totalOperatorsInExpression(expression))
        var expOpIndex = 0
        while (expIndex < expression.length) {
            var opIndex = 0
            while (opIndex < this.operators.size) {
                if (expression[expIndex].toString() == this.operators[opIndex]) {
                    expOperatorPos[expOpIndex] = expIndex         //saving operator positions
                    ++expOpIndex
                }
                ++opIndex
            }
            ++expIndex
        }
        return expOperatorPos
    }

    /**
     * The following block extracts values from given expression, char by char, and stores them
     * in an array of Strings, by grouping digits in form of numbers at the same index as string,
     * and this.operators in the expression at a separate index if array of Strings.
     *  For ex - Let the given expression be :   1234/556*89+4-23
     *  Starting from index = 0, the following block will store digits till '/'  at index =0 of empty array of Strings, then
     *  will store '/' itself at index =  1 of empty array of Strings. Then proceeds to store 5, 5  and 6
     *  at the same index = 2 of e.a. of strings. And stores the next operator '*' at index = 3, and so on.
     *  Thus a distinction between operands and operators is created and stored in a new array (of strings).
     *  @param expression: takes expression as a string
     *  @param sizeOfArray: takes size of segmented array to be formed (2*total operators+1)
     *  @return the segmented array of expression
     */
    fun segmentizeExpression(expression: String, sizeOfArray: Int): Array<String?>? {
        val arrayOfExpression = arrayOfNulls<String>(sizeOfArray)
        val expOperatorPos = positionsOfOperatorsInExpression(expression)
        var expArrayIndex = 0
        var positionInExpression = expArrayIndex
        var positionInOperatorPos = positionInExpression
        while (positionInOperatorPos < expOperatorPos.size && positionInExpression < expression.length) {
            while (positionInExpression < expOperatorPos[positionInOperatorPos]!!) {
                if (arrayOfExpression[expArrayIndex] == null) {
                    arrayOfExpression[expArrayIndex] = expression[positionInExpression].toString()
                } else {
                    arrayOfExpression[expArrayIndex] += expression[positionInExpression].toString()
                }
                ++positionInExpression
            }
            ++expArrayIndex
            if (positionInExpression == expOperatorPos[positionInOperatorPos]) {
                if (arrayOfExpression[expArrayIndex] == null) {
                    arrayOfExpression[expArrayIndex] = expression[positionInExpression].toString()
                } else {
                    arrayOfExpression[expArrayIndex] += expression[positionInExpression].toString()
                }
                ++expArrayIndex
            }
            ++positionInExpression
            ++positionInOperatorPos
            if (positionInOperatorPos >= expOperatorPos.size) {
                while (positionInExpression < expression.length) {
                    if (arrayOfExpression[expArrayIndex] == null) {
                        arrayOfExpression[expArrayIndex] =
                            expression[positionInExpression].toString()
                    } else {
                        arrayOfExpression[expArrayIndex] += expression[positionInExpression].toString()
                    }
                    ++positionInExpression
                }
            }
        }
        //if operator comes first, place zero at null index
        if (arrayOfExpression[arrayOfExpression.size - 1] == null)
            return null
        if (arrayOfExpression[0] == null) {
            arrayOfExpression[0] = "0"
        }
        return arrayOfExpression
    }
    val input = InputSpeechManager()

    fun evaluateFunctionsInExpressionArray(arrayOfExpression: Array<String?>): Array<String?>? {
        var fin = 0
        while (fin < arrayOfExpression.size) {
            if (arrayOfExpression[fin]!!.contains(skivvy.textPattern)) {        //TODO: try functions array instead of regular text
                if (!arrayOfExpression[fin]!!.contains(skivvy.numberPattern)) {
                    if (arrayOfExpression[fin + 1]!! == "+") {
                        arrayOfExpression[fin + 1] = nothing
                        var lk = fin + 2
                        while (lk < arrayOfExpression.size) {
                            if (lk == fin + 2) {
                                arrayOfExpression[lk - 2] += arrayOfExpression[lk]
                            } else {
                                arrayOfExpression[lk - 2] = arrayOfExpression[lk]
                            }
                            ++lk
                        }
                        arrayOfExpression[arrayOfExpression.size - 1] = nothing
                        arrayOfExpression[arrayOfExpression.size - 2] = nothing
                    } else if (arrayOfExpression[fin + 1]!! == "-") {
                        if (arrayOfExpression[fin + 2]!!.contains(skivvy.numberPattern)) {
                            arrayOfExpression[fin + 2] =
                                (0 - arrayOfExpression[fin + 2]!!.toFloat()).toString()
                            var lk = fin + 2
                            while (lk < arrayOfExpression.size) {
                                if (lk == fin + 2) {
                                    arrayOfExpression[lk - 2] += arrayOfExpression[lk]
                                } else {
                                    arrayOfExpression[lk - 2] = arrayOfExpression[lk]
                                }
                                ++lk
                            }
                            arrayOfExpression[arrayOfExpression.size - 1] = nothing
                            arrayOfExpression[arrayOfExpression.size - 2] = nothing
                        } else return null
                    } else return null
                }
                arrayOfExpression[fin] = operateFuncWithConstant(arrayOfExpression[fin]!!)
                arrayOfExpression[fin] = handleExponentialTerm(arrayOfExpression[fin]!!)
                if (arrayOfExpression[fin]!! == nothing || !arrayOfExpression[fin]!!.contains(skivvy.numberPattern))
                    return null
            }
            ++fin
        }
        return arrayOfExpression
    }

    /**
     * If [value] contains E: The exponential term , then this considers the value to be very small (if E-) or very large (if only E),
     * and returns 0 and infinity respectively overriding them.
     * @note Although this approach is ambiguous, and certainly not very good for proper evaluation, however, considering that
     * most of the time any calculation results into a very large or small number, it is denoted by any computer with an E (case sensitive that's why),
     * and thus, this works real good for most of the time, and it is very unlikely to have erroneous result using this method, however, not impossible.
     * */
    fun handleExponentialTerm(value:String):String{
        return if(value.contains(skivvy.textPattern)) {
            when {
                value.contains("E-", false) -> {
                    "0"
                }
                value.contains("E", false) -> {
                    skivvy.getString(R.string.infinity)
                }
                else -> nothing
            }
        } else value
    }

    /**
     * To operate function with a constant in beginning (to be multiplied).
     * @param func The function with its surrounding numbers, the right one being the argument.
     * The left one being the constant, intended to be multiplied by the value of the function with given argument.
     * This method first solves the function using [functionOperate] with its argument (on right), and then passes the result
     * to [operate] with '*' (multiplication symbol) and the number on the left of function, thus getting it multiplied.
     * @return The solved function as String. If [func] doesn't have any argument then returns [nothing].
     *
     */
    fun operateFuncWithConstant(func: String):String?{
        val temp = func.replace(skivvy.textPattern,"|")
        val numBefore = temp.substringBefore("|")
        val numAfter = temp.substringAfterLast("|")
        return if(numBefore.contains(skivvy.numberPattern) && numAfter.contains(skivvy.numberPattern)){
            this.functionOperate(
                func.replace(skivvy.numberPattern,nothing).replace(".",nothing)
                    + temp.substringAfterLast("|"))?.toFloat()?.let {
                this.operate(numBefore.toFloat(),'*',
                    it
                ).toString()
            }
        } else if(numAfter.contains(skivvy.numberPattern)){
            this.functionOperate(func)
        } else{
            nothing
        }
    }
    /**
     * Checks if expression doesn't have any illegal characters,
     * and returns true if operatable.
     * @param expression The string of expression to be checked for invalid chars.
     */
    fun isExpressionOperatable(expression: String): Boolean {
        var localExp = expression
        if (!localExp.contains(skivvy.numberPattern)) {
            return false
        } else {
            localExp = localExp.replace(skivvy.numberPattern, nothing)
        }
        val validCharsOfExpression = arrayOf(".")
        val operatorsFunctionsNumbers =
            arrayOf(this.operators, this.mathFunctions, validCharsOfExpression)
        var kkk = 0
        while (kkk < operatorsFunctionsNumbers.size) {
            var kk = 0
            while (kk < operatorsFunctionsNumbers[kkk].size) {
                localExp = localExp.replace(operatorsFunctionsNumbers[kkk][kk], nothing)
                ++kk
            }
            ++kkk
        }
        return localExp == nothing
    }

    /**
     * Checks if given array of string [arrayOfExpression] has only numbers and operators in it.
     * @note The argument must be passed after solving mathematical functions to avoid false return by this method.
     * @param arrayOfExpression The array of Strings forming an expression to be checked.
     * @return Boolean value if [arrayOfExpression] is a qualified array of strings of numbers and mathematical operators only.
     */
    fun isExpressionArrayOnlyNumbersAndOperators(arrayOfExpression: Array<String?>): Boolean {
        var fci = 0
        while (fci < arrayOfExpression.size) {
            if (arrayOfExpression[fci] != null) {
                if (arrayOfExpression[fci]!!.contains(skivvy.textPattern) &&
                    arrayOfExpression[fci]!!.length > 1         //for symbols which are letters
                ) {
                    return false
                }
            } else {
                return false
            }
            ++fci
        }
        return true
    }

    /**
     * Considering having the new array of strings, the proper segmented
     * expression as
     * @param arrayOfExpression The array of strings forming valid mathematical expression of numbers
     * and operators only, with operators at every even position of the array (at odd indices),
     * the following block of code will evaluate the expression according to the BODMAS rule.
     * @return The final answer solved at index = 0 of the given array of expression.
     */
    fun expressionCalculation(arrayOfExpression: Array<String?>): String {
        var nullPosCount = 0
        var opIndex = 0
        while (opIndex < this.operators.size) {
            var opPos = 1
            while (opPos < arrayOfExpression.size - nullPosCount) {
                if (arrayOfExpression[opPos] == this.operators[opIndex]) {
                    if (arrayOfExpression[opPos] == "-") {
                        arrayOfExpression[opPos + 1] =
                            (0 - arrayOfExpression[opPos + 1]!!.toFloat()).toString()
                        arrayOfExpression[opPos] = "+"
                    }
                    try {
                        arrayOfExpression[opPos - 1] = this.operate(
                            arrayOfExpression[opPos - 1]!!.toFloat(),
                            arrayOfExpression[opPos]!!.toCharArray()[0],
                            arrayOfExpression[opPos + 1]!!.toFloat()
                        ).toString()
                    } catch (e: NumberFormatException) {
                        arrayOfExpression[opPos - 1] = "point"      //has multiple decimal points in single number
                    }
                    var j = opPos
                    while (j + 2 < arrayOfExpression.size) {
                        arrayOfExpression[j] = arrayOfExpression[j + 2]
                        ++j
                    }
                    nullPosCount += 2
                    if (arrayOfExpression.size > 3 &&
                        arrayOfExpression[opPos] == this.operators[opIndex]
                    ) {    //if replacing operator is same as the replaced one
                        opPos -= 2            //index two indices back so that it returns at same position again
                    }
                }
                opPos += 2        //next index of operator in array of expression
            }
            ++opIndex       //next operator
        }
        return returnValidResult(arrayOfExpression)
    }

    /**
     * Checks validity of array of strings forming expression [result] if it has mathematically undefined or exceptional results in it.
     * @param result The array of strings forming expression which is to be checked.
     * @return A string of proper answer with possible reason of different cases,
     */
    fun returnValidResult(result: Array<String?>):String{
        return when {
            result.contentDeepToString().contains("point")->
                skivvy.getString(R.string.invalid_expression)
            result.contentDeepToString().contains("NaN")->
                skivvy.getString(R.string.undefined_result)
            else-> formatToProperValue(result[0].toString())     //final result stored at index = 0
        }
    }

    /**
     * This performs mathematical operations between two operands with given operator
     * @param operand1 : The first operand
     * @param operand2: The next operand
     * @param operator: The mathematical operator according to which operation will be performed on [operand1] and [operand2]
     * @return : Returns solved operation result if valid [operator] is provided, else returns null.
     */
    fun operate(operand1: Float, operator: Char, operand2: Float): Float? {
        return when (operator) {
            '/' -> operand1.div(operand2)
            '*' -> operand1.times(operand2)
            '+' -> operand1.plus(operand2)
            '-' -> operand1.minus(operand2)
            'p' -> (operand1.div(100)).times(operand2)
            'm' -> operand1.rem(operand2)
            '^' -> operand1.toDouble().pow(operand2.toDouble()).toFloat()
            else -> null
        }
    }
    private fun functionOperate(func: String): String? {
        try {
            return when {
                func.contains("sin") -> sin(
                    func.replace(skivvy.textPattern, nothing).toFloat().toRadian(skivvy.getAngleUnit())
                ).toString()
                func.contains("cos") -> cos(
                    func.replace(skivvy.textPattern, nothing).toFloat().toRadian(skivvy.getAngleUnit())
                ).toString()
                func.contains("tan") -> tan(
                    func.replace(skivvy.textPattern, nothing).toFloat().toRadian(skivvy.getAngleUnit())
                ).toString()
                func.contains("cot") -> (1.div(tan(
                    func.replace(skivvy.textPattern, nothing).toFloat().toRadian(skivvy.getAngleUnit())
                ))).toString()
                func.contains("sec") -> (1.div(cos(
                    func.replace(skivvy.textPattern, nothing).toFloat().toRadian(skivvy.getAngleUnit())
                ))).toString()
                func.contains("cosec") -> (1.div(sin(
                    func.replace(skivvy.textPattern, nothing).toFloat().toRadian(skivvy.getAngleUnit())
                ))).toString()
                func.contains("log") -> {
                    log(func.replace(skivvy.textPattern, nothing).toFloat(), 10F).toString()
                }
                func.contains("ln") -> {
                    ln1p(func.replace(skivvy.textPattern, nothing).toFloat()).toString()
                }
                func.contains("sqrt") -> {
                    (func.replace(skivvy.textPattern, nothing).toFloat().pow(0.5F)).toString()
                }
                func.contains("cbrt") -> {
                    (func.replace(skivvy.textPattern, nothing).toDouble()
                        .pow(1 / 3.toDouble())).toString()
                }
                func.contains("exp") -> {
                    (exp(func.replace(skivvy.textPattern, nothing).toFloat())).toString()
                }
                func.contains("fact") -> {
                    factorialOf(func.replace(skivvy.textPattern, nothing).toInt()).toString()
                }
                else -> skivvy.getString(R.string.invalid_expression)
            }
        } catch(e:Exception){
            return skivvy.getString(R.string.invalid_expression)
        }
    }

    /**
     * Calculates factorial of [num] and returns it. ([num]!)
     */
    private fun factorialOf(num: Int): Long {
        var result = 1L
        var i = 1
        while (i<=num){
            result *= i
            ++i
        }
        return result
    }

    private fun isFloat(value: String): Boolean {
        return value.toFloat() - value.toFloat().toInt() != 0F
    }

    /**Formats numbers to decimal or non decimals
     * When [value] is
     * 25.0, returns 25
     * 25.9, returns 25.9
     */
    fun formatToProperValue(value: String): String {
        return if (isFloat(value)) {
            value.toFloat().toString()
        } else value.toFloat().toInt().toString()
    }
    private fun Number.toRadian(angle:String): Float {
        return when(angle){
            skivvy.radian->this.toFloat()*1F
            else-> this.toFloat()*PI.div(180).toFloat()
        }
    }
}


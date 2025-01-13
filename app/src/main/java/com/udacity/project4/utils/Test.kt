package com.udacity.project4.utils

class Test {


    fun solution(A: IntArray): Int {
        // Implement your solution here
        if (A.isEmpty()) return -1
        val sortedArray = A.sorted()

        var minNumber = sortedArray[0]
        var maxNumber = sortedArray[sortedArray.size - 1]

        val numberHashSet = hashSetOf<Int>()

        for (num in A ){
            numberHashSet.add(num)
        }

        var result = -1

        for (num in minNumber..maxNumber){
            if (!numberHashSet.contains(num)){
                result = num
                break
            }
        }

        return result
    }
}

fun solution(A: IntArray): Int {

    if (A.isEmpty()) return -1

    val positiveNumbers = A.filter { it > 0 }.toHashSet()

    for (i in 1..(A.size + 1)) {
        if (i !in positiveNumbers) {
            return i
        }
    }

    return -1
}

/*

fun solution(S: String): Int {

    var operationCount = 0
    var lastIndex = S.lastIndex

    // Binary form, skipping leading zeros
    while (lastIndex >= 0 && S[lastIndex] == '0') {
        lastIndex--
    }

    while (lastIndex >= 0) {
        if (S[lastIndex] == '1') {
            // If the bit is 1, it requires a "subtract 1" operation
            operationCount++
        }
        if (lastIndex > 0) {
            // Increment the operation count by
            operationCount++
        }

        lastIndex--
    }

    return operationCount
}

fun solution(S: String): Int {
    var operations = 0
    var index = S.lastIndex

    // Skip leading zeros (from the end of the string)
    while (index >= 0 && S[index] == '0') {
        index--
    }

    // Process the binary string from the least significant bit to the most significant bit
    while (index >= 0) {
        if (S[index] == '1') {
            // If the bit is 1, it requires a "subtract 1" operation
            operations++
            if (index > 0) {
                // After subtraction, a divide by 2 operation is needed if not the last bit
                operations++
            }
        } else {
            // If the bit is 0, a divide by 2 operation is performed
            operations++
        }
        // Move to the next bit (left in the string)
        index--
    }

    return operations
}*/

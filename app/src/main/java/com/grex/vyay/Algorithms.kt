package com.grex.vyay

class Algorithms {
    // Calculate Jaccard similarity between two texts
    fun jaccardSimilarity(text1: String, text2: String): Double {
        val words1 = text1.split(" ").toSet()
        val words2 = text2.split(" ").toSet()
        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size
        return intersection.toDouble() / union.toDouble()
    }
}
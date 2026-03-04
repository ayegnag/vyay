package com.grex.vyay

import kotlinx.coroutines.flow.StateFlow

interface SharedViewModelInterface {
    val prefExpenseThreshold: StateFlow<Double>
    fun setExpenseThreshold(value: Double)
}
package com.grex.vyay

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AssignmentReturn
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CreditScore
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun TagIcon(tags: String?): ImageVector {
    val tagList = tags?.split(",")?.map { it.trim().lowercase() }

    if (tagList == null) {
        return Icons.AutoMirrored.Filled.Label // Default icon
    }

    return when {
        tagList.contains("groceries") -> Icons.Filled.ShoppingCart
        tagList.contains("bills") -> Icons.AutoMirrored.Filled.ReceiptLong
        tagList.contains("emi") -> Icons.Filled.Payment
        tagList.contains("fuel") -> Icons.Filled.LocalGasStation
        tagList.contains("rent") -> Icons.Filled.Home
        tagList.contains("health") -> Icons.Filled.Favorite
        tagList.contains("food") -> Icons.Filled.Restaurant
        tagList.contains("shopping") -> Icons.Filled.ShoppingBag
        tagList.contains("investment") -> Icons.AutoMirrored.Filled.TrendingUp
        tagList.contains("monthly") -> Icons.Filled.DateRange
        tagList.contains("annual") -> Icons.Filled.EventRepeat
        tagList.contains("work") -> Icons.Filled.Work
        tagList.contains("personal") -> Icons.Filled.Person
        tagList.contains("shared") -> Icons.Filled.People
        tagList.contains("tax") -> Icons.Filled.Receipt
        tagList.contains("subscription") -> Icons.Filled.Subscriptions
        tagList.contains("gift") -> Icons.Filled.CardGiftcard
        tagList.contains("entertainment") -> Icons.Filled.LocalMovies
        tagList.contains("education") -> Icons.Filled.School
        tagList.contains("travel") -> Icons.Filled.FlightTakeoff
        tagList.contains("luxury") -> Icons.Filled.Diamond
        tagList.contains("salary") -> Icons.Filled.AttachMoney
        tagList.contains("bonus") -> Icons.Filled.Star
        tagList.contains("commission") -> Icons.Filled.Handshake
        tagList.contains("tax refund") -> Icons.Filled.MoneyOff
        tagList.contains("dividend") -> Icons.Filled.AccountBalance
        tagList.contains("interest") -> Icons.Filled.Percent
        tagList.contains("rental income") -> Icons.Filled.Apartment
        tagList.contains("pension") -> Icons.Filled.Savings
        tagList.contains("loan") -> Icons.Filled.AccountBalance
        tagList.contains("reimbursement") -> Icons.AutoMirrored.Filled.AssignmentReturn
        tagList.contains("cashback") -> Icons.Filled.CreditScore
        else -> Icons.AutoMirrored.Filled.Label // Default icon
    }
}
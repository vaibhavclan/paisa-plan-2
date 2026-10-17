package com.example.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.domain.FinanceCalculator

@Composable
fun CurrencyText(
    amount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Bold,
    prefix: String = ""
) {
    val formatted = FinanceCalculator.formatINR(amount)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (prefix.isNotEmpty()) {
            Text(
                text = prefix,
                style = style,
                color = color,
                fontWeight = fontWeight
            )
        }
        Text(
            text = formatted,
            style = style,
            color = color,
            fontWeight = fontWeight
        )
    }
}

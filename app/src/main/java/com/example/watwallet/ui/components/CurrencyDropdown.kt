package com.example.watwallet.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier


@Composable
fun CurrencyDropdown(
    modifier: Modifier = Modifier,
    currencies: List<String>,
    selectedIndex: Int,
    onCurrencySelected: (Int) -> Unit
) {
    val isCurrencyExpanded = remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = currencies[selectedIndex],
            onValueChange = {},
            label = { Text("Currency") },
            readOnly = true,
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { isCurrencyExpanded.value = true }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isCurrencyExpanded.value = true },
            singleLine = true
        )

        DropdownMenu(
            expanded = isCurrencyExpanded.value,
            onDismissRequest = { isCurrencyExpanded.value = false }
        ) {
            currencies.forEachIndexed { index, currency ->
                DropdownMenuItem(
                    text = { Text(currency) },
                    onClick = {
                        onCurrencySelected(index)
                        isCurrencyExpanded.value = false
                    }
                )
            }
        }
    }
}

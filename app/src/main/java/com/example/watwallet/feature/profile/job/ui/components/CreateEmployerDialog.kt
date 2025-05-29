package com.example.watwallet.feature.profile.job.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.watwallet.ui.components.LabeledInputField

@Composable
fun CreateEmployerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (employerName: String) -> Unit,
) {
    var employerName by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { onDismissRequest() }
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .background(color = Color.White)
                .padding(20.dp)
        ) {

            Text(
                "Add New Employer",
                fontSize = 26.sp,
                modifier = Modifier.align(Alignment.TopStart),
                color = Color.Gray
            )
            Column(
                modifier = Modifier.padding(top = 30.dp)
            ) {
                Spacer(Modifier.height(10.dp))
                LabeledInputField(
                    label = "Employer Name",
                    placeholder = "e.g. The Boardwalk",
                    employerName,
                    onValueChange = { employerName = it }
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.End) {
                    Spacer(Modifier.weight(1f))
                    TextButton({ onDismissRequest() }) {
                        Text("Close")
                    }
                    Spacer(Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onConfirm(employerName)
                            onDismissRequest()
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Confirm")
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Close",
                modifier = Modifier
                    .clickable { onDismissRequest() }
                    .align(Alignment.TopEnd)
            )
        }
    }
}

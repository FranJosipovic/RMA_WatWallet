package com.example.watwallet.feature.profile.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.watwallet.R
import com.example.watwallet.data.repository.Job
import com.example.watwallet.feature.profile.data.JobUI
import com.example.watwallet.utils.DateUtils


@Composable
fun JobCard(job: JobUI, onEdit: (id: String) -> Unit, onDelete: (id: String) -> Unit) {
    Box(
        modifier = Modifier
            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(6.dp))
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.edit_icon),
                tint = Color.Black,
                contentDescription = "Edit icon",
                modifier = Modifier.clickable { onEdit(job.id) }
            )
            Icon(
                painter = painterResource(R.drawable.delete_icon),
                tint = Color.Black,
                contentDescription = "Edit icon",
                modifier = Modifier.clickable {
                    onDelete(job.id)
                }
            )
        }
        Column {
            Text(text = job.employer.name, fontSize = 24.sp)
            Text(text = job.position, color = Color.Gray, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Location", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(job.locationInfo, color = Color.Gray, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Job Description", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(job.description, color = Color.Gray, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                Column {
                    Text(text = "Start Date", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(job.startDate.toString(),
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
                Column {
                    Text(text = "End Date", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        job.endDate.toString(),
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

package com.example.watwallet.feature.profile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.watwallet.data.repository.User

@Composable
fun ProfileCard(user: User) {
    Box(
        modifier = Modifier
            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(12.dp))
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
                    .background(color = Color.Blue, shape = CircleShape)
            ) {
                Text(
                    text = "${user.userInfo.name.first()}${user.userInfo.surname.first()}",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(.1.dp)) {
                Text(text = "${user.userInfo.name} ${user.userInfo.surname}")
                Text(text = user.email)
                Text(text = user.userInfo.phone)
            }
        }
    }
}
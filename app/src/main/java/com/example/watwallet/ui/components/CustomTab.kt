package com.example.watwallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CustomTabView(
    tabs:List<String>,
    activeTabIndex: MutableState<Int>,
    tabContents: List<@Composable () -> Unit>
){
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(color = Color.LightGray, shape = RoundedCornerShape(3.dp))

        ){
            Row(modifier = Modifier
                .padding(3.dp)
                .height(35.dp)
            ) {
                tabs.forEachIndexed { index, tab->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = if(index == activeTabIndex.value) Color.White else Color.LightGray,
                                shape = RoundedCornerShape(2.dp))
                            .clickable { activeTabIndex.value = index }
                    ){
                        Text(tab,
                            modifier = Modifier
                                .align(Alignment.Center),
                            color = if(index == activeTabIndex.value) Color.Black else Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        tabContents.getOrNull(activeTabIndex.value)?.invoke()
    }
}

@Preview
@Composable
fun previewTab() {
    val tabs = listOf("Expense", "Income")
    val activeTabIndex = remember { mutableStateOf(0) }

    val tabContents = listOf<@Composable () -> Unit>(
        {
            Text("Expense Content")
        },
        {
            Text("Income Content")
        }
    )

    Box(
        modifier = Modifier
            .width(200.dp)
            .wrapContentHeight()
            .background(Color.White)
            .padding(8.dp)
    ) {
        CustomTabView(
            tabs = tabs,
            activeTabIndex = activeTabIndex,
            tabContents = tabContents
        )
    }
}

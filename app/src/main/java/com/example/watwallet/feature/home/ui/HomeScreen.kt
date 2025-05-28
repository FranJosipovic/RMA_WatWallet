package com.example.watwallet.feature.home.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.watwallet.core.navigation.NavigationItem
import com.example.watwallet.data.repository.TransactionType
import com.example.watwallet.feature.add.ui.AddScreen
import com.example.watwallet.feature.home.viewmodel.HomeViewModel
import com.example.watwallet.utils.DateUtils
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController){

    val homeViewModel:HomeViewModel = koinViewModel()
    val state by homeViewModel.state

    var isRefreshing by remember { mutableStateOf(false) }

    val pullToRefreshState = rememberPullToRefreshState()

    val coroutineScope = rememberCoroutineScope()

    when {
        state.loading -> {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
        else -> {
            PullToRefreshBox(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                onRefresh = {
                    coroutineScope.launch {
                        isRefreshing = true
                        homeViewModel.refreshTransactions()
                        isRefreshing = false
                    }
                }
            ) {
                Column(modifier = Modifier.padding(10.dp).verticalScroll(rememberScrollState())) {
                    Text("Overview", fontSize = 30.sp)
                    Row (
                        modifier = Modifier.padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ){
                        Card(
                            modifier = Modifier.wrapContentSize().weight(1f),
                            elevation = CardDefaults.elevatedCardElevation(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text("Earnings", fontSize = 24.sp)
                                Text(
                                    text = "$${state.transactionsInfo.earnings}",
                                    modifier = Modifier.padding(top = 20.dp),
                                    fontSize = 30.sp
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.wrapContentSize().weight(1f),
                            elevation = CardDefaults.elevatedCardElevation(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text("Expenses", fontSize = 24.sp)
                                Text(
                                    text = "$${state.transactionsInfo.expenses}",
                                    modifier = Modifier.padding(top = 20.dp),
                                    fontSize = 30.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            navController.navigate(NavigationItem.Add.createRoute(1))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(10.dp))
                            .padding(vertical = 10.dp, horizontal = 20.dp)
                        ,colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = "+ Add Income",
                            textAlign = TextAlign.Center,
                            fontSize = 34.sp,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            navController.navigate(NavigationItem.Add.createRoute(0))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(10.dp))
                            .padding(vertical = 10.dp, horizontal = 20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = "- Add Expense",
                            textAlign = TextAlign.Center,
                            fontSize = 34.sp,
                            color = Color.Black,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Season 2025", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    state.transactionsInfo.transactions.forEach { transaction ->
                        val isExpense = transaction.transactionType == TransactionType.Expense
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(10.dp))
                            .padding(10.dp)
                        ){
                            Column {
                                Text(transaction.description)
                                Text(DateUtils.timestampToLocalDate(transaction.date).toString()/*format(DateTimeFormatter.ofPattern("MMM dd")).capitalize(Locale.ROOT)*/, color = Color.Gray)
                            }
                            Text(
                                text = if (isExpense) "-$${transaction.totalAmount}" else "$${transaction.totalAmount}",
                                color = if (isExpense) Color.Red else Color.Green,
                                modifier = Modifier.align(Alignment.TopEnd)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

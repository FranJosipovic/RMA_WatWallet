package com.example.watwallet.feature.home.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.watwallet.core.navigation.NavigationItem
import com.example.watwallet.data.repository.TransactionGetModel
import com.example.watwallet.data.repository.TransactionType
import com.example.watwallet.feature.home.ui.components.EditExpenseDialog
import com.example.watwallet.feature.home.ui.components.EditIncomeDialog
import com.example.watwallet.feature.home.ui.components.TransactionBottomSheet
import com.example.watwallet.feature.home.ui.components.TransactionCard
import com.example.watwallet.feature.home.viewmodel.EditExpenseFormEvent
import com.example.watwallet.feature.home.viewmodel.EditIncomeFormEvent
import com.example.watwallet.feature.home.viewmodel.HomeViewModel
import com.example.watwallet.ui.components.CustomConfirmDialog
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val showConfirmDialog = remember { mutableStateOf(false) }

    val homeViewModel: HomeViewModel = koinViewModel()
    val state by homeViewModel.state

    var isRefreshing by remember { mutableStateOf(false) }

    val pullToRefreshState = rememberPullToRefreshState()

    val coroutineScope = rememberCoroutineScope()

    var selectedTransaction by rememberSaveable { mutableStateOf<TransactionGetModel?>(null) }

    val deleting by homeViewModel.deleting.collectAsState()
    val finding by homeViewModel.finding.collectAsState()
    val editing by homeViewModel.finding.collectAsState()

    val incomeEditErrorState by homeViewModel.incomeEditErrorState.collectAsState()
    val expenseEditErrorState by homeViewModel.expenseEditErrorState.collectAsState()

    val selectedExpense by homeViewModel.selectedExpense.collectAsState()
    val selectedIncome by homeViewModel.selectedIncome.collectAsState()

    if (selectedExpense != null) {
        EditExpenseDialog(
            editing = editing,
            selectedDate = selectedExpense!!.date,
            onDateSelect = {
                homeViewModel.onEditExpenseEvent(
                    EditExpenseFormEvent.SelectedDateChanged(it)
                )
            },
            onDismissRequest = { homeViewModel.unselectExpense() },
            selectedTag = selectedExpense!!.tag,
            onTagSelect = {
                homeViewModel.onEditExpenseEvent(
                    EditExpenseFormEvent.TagChanged(it)
                )
            },
            amount = selectedExpense!!.amount,
            onAmountChange = {
                homeViewModel.onEditExpenseEvent(
                    EditExpenseFormEvent.AmountChanged(it)
                )
            },
            description = selectedExpense!!.description,
            onDescriptionChange = {
                homeViewModel.onEditExpenseEvent(
                    EditExpenseFormEvent.DescriptionChanged(it)
                )
            },
            onCancel = {
                homeViewModel.onEditExpenseEvent(EditExpenseFormEvent.OnCancel)
            },
            editExpenseErrorState = expenseEditErrorState,
            onSaveChanges = {
                homeViewModel.onEditExpenseEvent(
                    EditExpenseFormEvent.OnSubmit(onSuccess = {
                        homeViewModel.unselectExpense()
                        selectedTransaction = null
                        coroutineScope.launch {
                            isRefreshing = true
                            homeViewModel.refreshTransactions()
                            isRefreshing = false
                        }
                    })
                )
            }
        )
    }

    val jobs by homeViewModel.jobs.collectAsState()

    if (selectedIncome != null) {
        EditIncomeDialog(
            jobs = jobs,
            selectedJob = selectedIncome!!.job,
            onJobSelect = { homeViewModel.onEditIncomeEvent(EditIncomeFormEvent.JobChanged(it)) },
            selectedDate = selectedIncome!!.date,
            onDateSelect = {
                homeViewModel.onEditIncomeEvent(
                    EditIncomeFormEvent.SelectedDateChanged(
                        it
                    )
                )
            },
            baseEarned = selectedIncome!!.baseEarned,
            onBaseEarnedChange = {
                homeViewModel.onEditIncomeEvent(
                    EditIncomeFormEvent.BaseEarnedChanged(
                        it
                    )
                )
            },
            tipsEarned = selectedIncome!!.tipsEarned,
            onTipsEarnedChange = {
                homeViewModel.onEditIncomeEvent(
                    EditIncomeFormEvent.TipsEarnedChanged(
                        it
                    )
                )
            },
            totalHoursWorked = selectedIncome!!.totalHoursWorked,
            onTotalHoursWorkedChange = {
                homeViewModel.onEditIncomeEvent(
                    EditIncomeFormEvent.TotalHoursWorkedChanged(
                        it
                    )
                )
            },
            editIncomeErrorState = incomeEditErrorState,
            onSaveChanges = {
                homeViewModel.onEditIncomeEvent(EditIncomeFormEvent.OnSubmit(onSuccess = {
                    homeViewModel.unselectIncome()
                    selectedTransaction = null
                    coroutineScope.launch {
                        isRefreshing = true
                        homeViewModel.refreshTransactions()
                        isRefreshing = false
                    }
                }))
            },
            onDismissRequest = { homeViewModel.unselectIncome() }
        )
    }

    CustomConfirmDialog(
        showDialog = showConfirmDialog.value,
        description = "Are you sure you want to delete this transaction?",
        onConfirm = {
            selectedTransaction?.let {
                homeViewModel.deleteTransaction(
                    it.uid,
                    it.transactionType
                ) { selectedTransaction = null }
            }
            showConfirmDialog.value = false
        },
        onCancel = {
            showConfirmDialog.value = false
        }
    )

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
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Overview", fontSize = 30.sp)
                    Row(
                        modifier = Modifier.padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .wrapContentSize()
                                .weight(1f),
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
                            modifier = Modifier
                                .wrapContentSize()
                                .weight(1f),
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
                            .border(
                                width = 1.dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(vertical = 10.dp, horizontal = 20.dp),
                        colors = ButtonDefaults.buttonColors(
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
                            .border(
                                width = 1.dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(10.dp)
                            )
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
                        TransactionCard(
                            transaction = transaction,
                            onClick = { selectedTransaction = transaction })
                    }
                }
            }
            if (selectedTransaction != null) {
                TransactionBottomSheet(
                    selectedTransaction!!,
                    finding = finding,
                    deleting = deleting,
                    onDismissRequest = { selectedTransaction = null },
                    onEdit = {
                        if (selectedTransaction!!.transactionType == TransactionType.Income) {
                            homeViewModel.getIncome(selectedTransaction!!.uid)
                        } else {
                            homeViewModel.getExpense(selectedTransaction!!.uid)
                        }
                    },
                    onDelete = {
                        showConfirmDialog.value = true
                    }
                )
            }
        }
    }
}

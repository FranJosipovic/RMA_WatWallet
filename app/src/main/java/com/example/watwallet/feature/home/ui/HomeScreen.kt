package com.example.watwallet.feature.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.watwallet.core.navigation.NavigationItem
import com.example.watwallet.data.repository.GetTransactionDTO
import com.example.watwallet.data.repository.TransactionType
import com.example.watwallet.feature.add.viewmodel.tags
import com.example.watwallet.feature.home.viewmodel.EditExpenseFormEvent
import com.example.watwallet.feature.home.viewmodel.ExpenseEditDTO
import com.example.watwallet.feature.home.viewmodel.HomeViewModel
import com.example.watwallet.ui.components.ConfirmDialog
import com.example.watwallet.ui.components.MoneyInputField
import com.example.watwallet.utils.DateUtils
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavController) {

    val showConfirmDialog = remember { mutableStateOf(false) }

    val homeViewModel: HomeViewModel = koinViewModel()
    val state by homeViewModel.state

    var isRefreshing by remember { mutableStateOf(false) }

    val pullToRefreshState = rememberPullToRefreshState()

    val coroutineScope = rememberCoroutineScope()

    var selectedTransaction by rememberSaveable { mutableStateOf<GetTransactionDTO?>(null) }

    val deleting by homeViewModel.deleting.collectAsState()
    val finding by homeViewModel.finding.collectAsState()
    val editing by homeViewModel.finding.collectAsState()

    val selectedExpense by homeViewModel.selectedExpense.collectAsState()

    if (selectedExpense != null) {
        EditExpenseDialog(
            expense = selectedExpense!!,
            homeViewModel = homeViewModel,
            editing = editing,
            onSuccessEdit = {
                homeViewModel.unselectExpense()
                selectedTransaction = null
                coroutineScope.launch {
                    isRefreshing = true
                    homeViewModel.refreshTransactions()
                    isRefreshing = false
                }
            },
            onDismissRequest = { homeViewModel.unselectExpense() })
    }

    ConfirmDialog(
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
                        val isExpense = transaction.transactionType == TransactionType.Expense
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = Color.Black,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp)
                                .combinedClickable(
                                    onClick = {},
                                    onDoubleClick = { selectedTransaction = transaction },
                                    onLongClick = {
                                        selectedTransaction = transaction
                                    },
                                    onLongClickLabel = "What"
                                )
                        ) {
                            Column {
                                Text(transaction.description)
                                Text(
                                    DateUtils.timestampToLocalDate(transaction.date)
                                        .toString()/*format(DateTimeFormatter.ofPattern("MMM dd")).capitalize(Locale.ROOT)*/,
                                    color = Color.Gray
                                )
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
            if (selectedTransaction != null) {
                ModalBottomSheet(onDismissRequest = { selectedTransaction = null }) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .fillMaxWidth()
                    ) {
                        Column {
                            Text(selectedTransaction!!.description)
                            Text(
                                DateUtils.timestampToLocalDate(selectedTransaction!!.date)
                                    .toString()/*format(DateTimeFormatter.ofPattern("MMM dd")).capitalize(Locale.ROOT)*/,
                                color = Color.Gray
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        homeViewModel.getExpense(selectedTransaction!!.uid)
                                    },
                                    contentPadding = PaddingValues(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    )
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.height(20.dp) // Fix height to prevent jumping
                                    ) {
                                        if (finding) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp,
                                            )
                                        } else {
                                            Text("Edit")
                                        }
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Button(
                                    onClick = {
                                        showConfirmDialog.value = true
                                    },
                                    contentPadding = PaddingValues(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    )
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.height(20.dp) // Fix height to prevent jumping
                                    ) {
                                        if (deleting) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp,
                                            )
                                        } else {
                                            Text("Delete")
                                        }
                                    }
                                }
                            }
                        }
                        Text(
                            text = if (selectedTransaction!!.transactionType == TransactionType.Expense) "-$${selectedTransaction!!.totalAmount}" else "$${selectedTransaction!!.totalAmount}",
                            color = if (selectedTransaction!!.transactionType == TransactionType.Expense) Color.Red else Color.Green,
                            modifier = Modifier.align(Alignment.TopEnd)
                        )

                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseDialog(
    expense: ExpenseEditDTO,
    homeViewModel: HomeViewModel,
    editing:Boolean,
    onSuccessEdit:()->Unit,
    onDismissRequest: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val datePickerState =
        rememberDatePickerState(initialSelectedDateMillis = DateUtils.localDateToMillis(expense.date))
    var openDateDatePicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onDismissRequest() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MoneyInputField(
                    label = "Amount",
                    modifier = Modifier.weight(1f),
                    value = expense.amount,
                    isError = false,
                    onValueChange = {
                        homeViewModel.onEditExpenseEvent(
                            EditExpenseFormEvent.AmountChanged(
                                it
                            )
                        )
                    }
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Date",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openDateDatePicker = true }
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = expense.date.toString(),
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                    }
                }
            }

            // Tag selection
            Column {
                Text("Tag")
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    tags.forEach { tag ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.selectable(
                                selected = (expense.tag == tag),
                                onClick = {
                                    homeViewModel.onEditExpenseEvent(
                                        EditExpenseFormEvent.TagChanged(
                                            tag
                                        )
                                    )
                                }
                            )
                        ) {
                            RadioButton(
                                selected = (expense.tag == tag),
                                onClick = {
                                    homeViewModel.onEditExpenseEvent(
                                        EditExpenseFormEvent.TagChanged(
                                            tag
                                        )
                                    )
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color.Blue)
                            )
                            Text(tag)
                        }
                    }
                }
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = expense.description,
                onValueChange = {
                    homeViewModel.onEditExpenseEvent(
                        EditExpenseFormEvent.DescriptionChanged(
                            it
                        )
                    )
                },
                label = { Text("Expense description") },
                placeholder = { Text("e.g. Taxi") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                ),
                isError = false
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        homeViewModel.onEditExpenseEvent(
                            EditExpenseFormEvent.OnSubmit(onSuccess = {onSuccessEdit()})
                        )
                    },
                    modifier = Modifier.weight(2f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    if (editing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Save Transaction", color = Color.White)
                    }
                }
                OutlinedButton(
                    onClick = {
                        homeViewModel.onEditExpenseEvent(EditExpenseFormEvent.OnCancel)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

package com.example.watwallet.feature.addtransaction.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.watwallet.app.sendTransactionNotification
import com.example.watwallet.feature.addtransaction.viewmodel.AddTransactionViewModel
import com.example.watwallet.ui.components.CustomTabView
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddTransactionScreen(startTabContent:Int = 0, snackbarHostState: SnackbarHostState, onAddJob:()->Unit){

    val context = LocalContext.current

    val addTransactionViewModel:AddTransactionViewModel = koinViewModel()

    val tabs = listOf("Expense", "Income")
    val activeTabIndex = remember { mutableIntStateOf(startTabContent) }

    val tabContents = listOf<@Composable () -> Unit>(
        {
            AddExpenseView(
                snackbarHostState = snackbarHostState,
                addTransactionViewModel = addTransactionViewModel,
                onExpenseAdded = {
                    sendTransactionNotification(context,"Expense Added Successfully")
                }
            )
        },
        {
            AddIncomeView(
                snackbarHostState = snackbarHostState,
                addTransactionViewModel = addTransactionViewModel,
                onIncomeAdded = {
                    sendTransactionNotification(context,"Income Added Successfully")
                },
                onAddJob = onAddJob
            )
        }
    )

    Column(modifier = Modifier
        .padding(10.dp)
        .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Add Transaction",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(18.dp))
        CustomTabView(
            tabs = tabs,
            activeTabIndex = activeTabIndex,
            tabContents = tabContents
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun AddScreenPreview() {
//    WatWalletTheme {
//
//            val tabs = listOf("Expense", "Income")
//            val activeTabIndex = remember { mutableStateOf(0) }
//
//            val tabContents = listOf<@Composable () -> Unit>(
//                {
//                    AddExpenseView(Modifier)
//                },
//                {
//                    AddIncomeView({})
//                }
//            )
//
//            Scaffold(
//                bottomBar = {
//                    BottomAppBar(
//                        containerColor = MaterialTheme.colorScheme.primaryContainer,
//                        contentColor = MaterialTheme.colorScheme.primary,
//                    ) {
//                        Text(
//                            modifier = Modifier
//                                .fillMaxWidth(),
//                            textAlign = TextAlign.Center,
//                            text = "Bottom app bar",
//                        )
//                    }
//                }
//            ) { innerPadding ->
//                Column(modifier = Modifier
//                    .padding(innerPadding)
//                    .padding(10.dp)) {
//                    Text("Add Transaction",fontSize = 30.sp, fontWeight = FontWeight.Bold)
//                    Spacer(modifier = Modifier.height(18.dp))
//                    CustomTabView(
//                        tabs = tabs,
//                        activeTabIndex = activeTabIndex,
//                        tabContents = tabContents
//                    )
//                }
//            }
//    }
//}
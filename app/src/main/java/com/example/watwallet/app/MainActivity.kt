package com.example.watwallet.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.watwallet.R
import com.example.watwallet.core.navigation.NavigationItem
import com.example.watwallet.data.repository.AuthRepository
import com.example.watwallet.ui.theme.WatWalletTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

fun sendTransactionNotification(context: Context, message: String) {
    val notificationId = 1
    val builder = NotificationCompat.Builder(context, "transactions_channel")
        .setSmallIcon(R.drawable.baseline_circle_notifications_24) // Replace with your icon
        .setContentTitle("Transaction Added")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notify(notificationId, builder.build())
    }
}

class MainActivity : ComponentActivity() {

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "transactions_channel", // ID
                "Transactions",         // Name
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Used for transaction notifications"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted
            } else {
                // Permission denied
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        enableEdgeToEdge()

        val authRepository: AuthRepository by inject()

        lifecycleScope.launch {

            val startingRoute = if(authRepository.isAuthenticated()){
                NavigationItem.Main.route
            }else{
                NavigationItem.Auth.route
            }


            setContent {
                WatWalletTheme {
                    MainNavigation(
                        startingRoute = startingRoute
                    )
                }
            }
        }
    }
}

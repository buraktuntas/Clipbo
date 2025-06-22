package com.bt.clipbo.presentation.ui.secure

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureClipboardScreen(
    viewModel: SecureClipboardViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    // Biometric durumunu kontrol et
    LaunchedEffect(Unit) {
        viewModel.checkBiometricAvailability(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("🔒")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Güvenli Pano")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    if (uiState.isAuthenticated) {
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.Default.Lock, contentDescription = "Kilitle")
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1A237E), // Koyu mavi güvenlik rengi
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White,
                    ),
            )
        },
    ) { paddingValues ->

        // Kimlik doğrulama ekranı
        if (!uiState.isAuthenticated) {
            BiometricAuthenticationScreen(
                biometricStatus = uiState.biometricStatus,
                isAuthenticating = uiState.isAuthenticating,
                onAuthenticate = {
                    activity?.let { fragmentActivity ->
                        viewModel.authenticateUser(
                            activity = fragmentActivity,
                            onSuccess = {
                                android.widget.Toast.makeText(context, "✅ Doğrulama başarılı!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                android.widget.Toast.makeText(context, "❌ $error", android.widget.Toast.LENGTH_LONG).show()
                            },
                        )
                    }
                },
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
            )
        } else {
            // Güvenli içerikler ekranı
            SecureContentScreen(
                uiState = uiState,
                onCopyItem = { item ->
                    viewModel.copyToClipboard(item.content)
                    android.widget.Toast.makeText(context, "📋 Güvenli içerik kopyalandı", android.widget.Toast.LENGTH_SHORT).show()
                },
                onDeleteItem = { viewModel.deleteItem(it) },
                onTogglePin = { viewModel.togglePin(it) },
                onToggleSecure = { viewModel.toggleSecureMode(it) },
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
            )
        }
    }
}

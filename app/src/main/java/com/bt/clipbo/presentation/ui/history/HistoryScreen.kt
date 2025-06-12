package com.bt.clipbo.presentation.ui.history

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.presentation.ui.components.ClipboardItemCard
import com.bt.clipbo.presentation.ui.components.SafeClipboardList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var showClearDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var selectedItemForTags by remember { mutableStateOf<ClipboardEntity?>(null) }
    // Her filtre değişiminde list state'ini sıfırla
    LaunchedEffect(selectedFilter) {
        listState.scrollToItem(0)
    }

    // Filtrelenmiş öğeler
    val filteredItems = remember(uiState.clipboardItems, selectedFilter, searchQuery) {
        uiState.clipboardItems.filter { item ->
            val matchesFilter = when (selectedFilter) {
                "Pinned" -> item.isPinned
                "Secure" -> item.isSecure
                "URL" -> item.type == "URL"
                "TEXT" -> item.type == "TEXT"
                "PASSWORD" -> item.type == "PASSWORD"
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true
            else item.content.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📋")
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Clipboard Geçmişi",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${filteredItems.size} öğe",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(Icons.Default.Search, contentDescription = "Ara")
                    }
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Temizle")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // Yukarı kaydır
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        listState.animateScrollToItem(0)
                    }
                },
                icon = { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null) },
                text = { Text("Başa Dön") },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Arama çubuğu
            AnimatedVisibility(
                visible = showSearchBar,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Ara...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Temizle")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gelişmiş istatistik kartları
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ModernStatCard(
                        value = uiState.clipboardItems.size,
                        label = "Toplam",
                        icon = "📊",
                        color = MaterialTheme.colorScheme.primary,
                        isSelected = selectedFilter == "All",
                        onClick = { selectedFilter = "All" }
                    )
                }
                item {
                    ModernStatCard(
                        value = uiState.clipboardItems.count { it.isPinned },
                        label = "Sabitli",
                        icon = "📌",
                        color = Color(0xFFFF9800),
                        isSelected = selectedFilter == "Pinned",
                        onClick = { selectedFilter = "Pinned" }
                    )
                }
                item {
                    ModernStatCard(
                        value = uiState.clipboardItems.count { it.isSecure },
                        label = "Güvenli",
                        icon = "🔒",
                        color = Color(0xFFE91E63),
                        isSelected = selectedFilter == "Secure",
                        onClick = { selectedFilter = "Secure" }
                    )
                }
                item {
                    ModernStatCard(
                        value = uiState.clipboardItems.count { it.type == "URL" },
                        label = "Linkler",
                        icon = "🔗",
                        color = Color(0xFF2196F3),
                        isSelected = selectedFilter == "URL",
                        onClick = { selectedFilter = "URL" }
                    )
                }
                item {
                    ModernStatCard(
                        value = uiState.clipboardItems.count { it.type == "TEXT" },
                        label = "Metinler",
                        icon = "📝",
                        color = Color(0xFF4CAF50),
                        isSelected = selectedFilter == "TEXT",
                        onClick = { selectedFilter = "TEXT" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ana içerik
            // Ana içerik
            if (uiState.isLoading) {
                LoadingAnimation()
            } else if (filteredItems.isEmpty()) {
                EmptyStateCard(selectedFilter, searchQuery.isNotEmpty())
            } else {
                SafeClipboardList(
                    items = filteredItems,
                    selectedFilter = selectedFilter,
                    onCopy = { item ->
                        viewModel.copyToClipboard(item.content)
                        Toast.makeText(context, "📋 Panoya kopyalandı", Toast.LENGTH_SHORT).show()
                    },
                    onDelete = { item -> viewModel.deleteItem(item) },
                    onTogglePin = { item -> viewModel.togglePin(item) },
                    onAssignTags = { item ->
                        selectedItemForTags = item
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Temizleme dialog'u
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Geçmişi Temizle") },
            text = {
                Text(
                    "Sabitlenmeyen tüm öğeler kalıcı olarak silinecek. Bu işlem geri alınamaz.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllUnpinned()
                        showClearDialog = false
                        Toast.makeText(context, "🧹 Geçmiş temizlendi", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Temizle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Etiket atama dialog'u
    selectedItemForTags?.let { item ->
        com.bt.clipbo.presentation.ui.tags.TagAssignmentDialog(
            clipboardItem = item,
            onDismiss = { selectedItemForTags = null },
            onSave = {
                selectedItemForTags = null
                Toast.makeText(context, "Etiketler güncellendi", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernStatCard(
    value: Int,
    label: String,
    icon: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(200), label = ""
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .scale(scale)
            .width(90.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) BorderStroke(2.dp, color) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LoadingAnimation() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Geçmiş yükleniyor...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyStateCard(selectedFilter: String, hasSearchQuery: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (hasSearchQuery) "🔍" else "📝",
                style = MaterialTheme.typography.displayMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (hasSearchQuery) "Arama sonucu bulunamadı"
                else if (selectedFilter == "All") "Henüz kopyalanan öğe yok"
                else "Bu kategoride öğe yok",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (hasSearchQuery) "Farklı kelimeler deneyin"
                else if (selectedFilter == "All") "Herhangi bir metni kopyaladığınızda burada görünecek"
                else "Diğer kategorileri kontrol edin",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
package com.bt.clipbo.presentation.ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bt.clipbo.data.database.ClipboardEntity
import kotlinx.coroutines.delay

@Composable
fun SafeClipboardList(
    items: List<ClipboardEntity>,
    selectedFilter: String,
    onCopy: (ClipboardEntity) -> Unit,
    onDelete: (ClipboardEntity) -> Unit,
    onTogglePin: (ClipboardEntity) -> Unit,
    onAssignTags: (ClipboardEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // Her filtre için ayrı state
    val listStates = remember { mutableMapOf<String, LazyListState>() }

    // Mevcut filtre için state al veya oluştur
    val currentListState = remember(selectedFilter) {
        listStates.getOrPut(selectedFilter) { LazyListState() }
    }

    // Lista değiştiğinde yukarı kaydır
    LaunchedEffect(selectedFilter, items.size) {
        delay(50) // Kısa bir gecikme ile smooth transition
        if (currentListState.firstVisibleItemIndex > 0) {
            currentListState.animateScrollToItem(0)
        }
    }

    // Loading state kontrolü
    var isTransitioning by remember { mutableStateOf(false) }

    LaunchedEffect(selectedFilter) {
        isTransitioning = true
        delay(100) // Transition süresi
        isTransitioning = false
    }

    if (isTransitioning) {
        // Geçiş sırasında basit loading
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
    } else {
        LazyColumn(
            state = currentListState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = items,
                key = { "${selectedFilter}_${it.id}_${it.timestamp}" } // Unique key
            ) { item ->
                ClipboardItemCard(
                    item = item,
                    onCopy = { onCopy(item) },
                    onDelete = { onDelete(item) },
                    onTogglePin = { onTogglePin(item) },
                    onAssignTags = { onAssignTags(item) }
                )
            }

            // Alt boşluk
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
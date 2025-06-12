package com.bt.clipbo.presentation.ui.secure

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bt.clipbo.utils.BiometricStatus
import com.bt.clipbo.utils.Quadruple

@Composable
fun BiometricStatusCard(status: BiometricStatus) {
    val (icon, title, description, color) = when (status) {
        BiometricStatus.AVAILABLE ->
            Quadruple("✅", "Biyometrik Kullanılabilir", "Parmak izi veya yüz tanıma aktif", Color(0xFF4CAF50))

        BiometricStatus.NO_HARDWARE ->
            Quadruple("❌", "Donanım Yok", "Cihazınızda biyometrik donanım bulunmuyor", Color(0xFFF44336))

        BiometricStatus.HARDWARE_UNAVAILABLE ->
            Quadruple("⚠️", "Donanım Kullanılamıyor", "Biyometrik donanım şu an kullanılamıyor", Color(0xFFFF9800))

        BiometricStatus.NONE_ENROLLED ->
            Quadruple("🔧", "Kurulum Gerekli", "Parmak izi veya yüz tanıma ayarlanmamış", Color(0xFF2196F3))

        BiometricStatus.SECURITY_UPDATE_REQUIRED ->
            Quadruple("🔄", "Güncelleme Gerekli", "Güvenlik güncellemesi gerekiyor", Color(0xFFFF9800))

        else ->
            Quadruple("❓", "Bilinmeyen Durum", "Biyometrik durum tespit edilemiyor", Color(0xFF9E9E9E))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

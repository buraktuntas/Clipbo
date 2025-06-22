package com.bt.clipbo.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.bt.clipbo.presentation.ui.main.MainActivity

@Composable
fun ClipboardItemsList(items: List<WidgetClipboardItem>) {
    LazyColumn(
        modifier = GlanceModifier.fillMaxSize(),
    ) {
        items(items.take(5)) { item ->
            ClipboardWidgetItem(item = item)

            if (item != items.last()) {
                Spacer(modifier = GlanceModifier.height(4.dp))
            }
        }

        // "Daha fazla" butonu
        item {
            Spacer(modifier = GlanceModifier.height(4.dp))

            Row(
                modifier =
                    GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionStartActivity<MainActivity>()),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            ) {
                Text(
                    text = "Tümünü Gör →",
                    style =
                        TextStyle(
                            color =
                                ColorProvider(
                                    day = Color(0xFF7B4397),
                                    night = Color(0xFFBB86FC),
                                ),
                            fontSize = 11.sp,
                        ),
                )
            }
        }
    }
}

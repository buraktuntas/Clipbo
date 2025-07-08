package com.bt.clipbo.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.bt.clipbo.presentation.ui.components.getTypeDisplayName
import com.bt.clipbo.presentation.ui.main.MainActivity
import com.bt.clipbo.widget.WidgetUtils.getTypeEmoji

@Composable
fun CompactClipboardItem(item: WidgetClipboardItem) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(
                ColorProvider(
                    day = Color(0xFFFFFFFF),
                    night = Color(0xFF2D2D2D)
                )
            )
            .cornerRadius(8.dp)
            .padding(8.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        // Type emoji
        Text(
            text = getTypeEmoji(item.type),
            style = TextStyle(fontSize = 12.sp)
        )

        Spacer(modifier = GlanceModifier.width(6.dp))

        Column(
            modifier = GlanceModifier.defaultWeight()
        ) {
            Text(
                text = if (item.isSecure) "•".repeat(8) else item.preview.take(25),
                style = TextStyle(
                    fontSize = 10.sp,
                    color = ColorProvider(
                        day = Color(0xFF2D2D2D),
                        night = Color(0xFFFFFFFF)
                    )
                ),
                maxLines = 1
            )

            Text(
                text = formatTimeAgo(item.timestamp),
                style = TextStyle(
                    fontSize = 8.sp,
                    color = ColorProvider(
                        day = Color(0xFF666666),
                        night = Color(0xFF999999)
                    )
                )
            )
        }

        if (item.isPinned) {
            Text(
                text = "📌",
                style = TextStyle(fontSize = 8.sp)
            )
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun DetailedClipboardItem(item: WidgetClipboardItem, context: Context) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(
                ColorProvider(
                    day = Color(0xFFFFFFFF),
                    night = Color(0xFF2D2D2D)
                )
            )
            .cornerRadius(12.dp)
            .padding(10.dp)
            .clickable(
                onClick = actionRunCallback<CopyToClipboardAction>(
                    parameters = actionParametersOf(
                        ActionParameters.Key<String>("content") to item.content
                    )
                )
            ),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        // Type icon with background
        Box(
            modifier = GlanceModifier
                .size(32.dp)
                .background(
                    ColorProvider(
                        day = Color(0xFFF3E5F5),
                        night = Color(0xFF3D3D3D)
                    )
                )
                .cornerRadius(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getTypeEmoji(item.type),
                style = TextStyle(fontSize = 14.sp)
            )
        }

        Spacer(modifier = GlanceModifier.width(10.dp))

        Column(
            modifier = GlanceModifier.defaultWeight()
        ) {
            Text(
                text = if (item.isSecure) "•".repeat(12) else item.preview.take(30),
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(
                        day = Color(0xFF2D2D2D),
                        night = Color(0xFFFFFFFF)
                    )
                ),
                maxLines = 1
            )

            Spacer(modifier = GlanceModifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = getTypeDisplayName(item.type),
                    style = TextStyle(
                        fontSize = 9.sp,
                        color = ColorProvider(Color(0xFF7B4397))
                    )
                )

                Spacer(modifier = GlanceModifier.width(6.dp))

                Text(
                    text = formatTimeAgo(item.timestamp),
                    style = TextStyle(
                        fontSize = 9.sp,
                        color = ColorProvider(
                            day = Color(0xFF666666),
                            night = Color(0xFF999999)
                        )
                    )
                )

                if (item.isPinned) {
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Text(text = "📌", style = TextStyle(fontSize = 8.sp))
                }

                if (item.isSecure) {
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Text(text = "🔒", style = TextStyle(fontSize = 8.sp))
                }
            }
        }
    }
}

@Composable
fun ActionButtonRow(context: Context) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        // Open app button
        Button(
            text = "Tümünü Gör",
            onClick = actionStartActivity<MainActivity>(),
            modifier = GlanceModifier.defaultWeight()
        )

        Spacer(modifier = GlanceModifier.width(6.dp))

        // Quick paste button (son kopyalanan)
        Button(
            text = "Son Öğeyi Yapıştır",
            onClick = actionRunCallback<PasteLastItemAction>(),
            modifier = GlanceModifier.defaultWeight()
        )
    }
}


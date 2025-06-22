package com.bt.clipbo.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
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
import com.bt.clipbo.presentation.ui.components.getTypeDisplayName
import com.bt.clipbo.presentation.ui.main.MainActivity

@Composable
fun ClipboardWidgetItem(item: WidgetClipboardItem) {
    Box(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .background(
                    ColorProvider(
                        day = Color(0xFFFFFFFF),
                        night = Color(0xFF2D2D2D),
                    ),
                )
                .cornerRadius(12.dp)
                .padding(10.dp)
                .clickable(actionStartActivity<MainActivity>()),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Type icon container
            Box(
                modifier =
                    GlanceModifier
                        .size(32.dp)
                        .background(
                            ColorProvider(
                                day = Color(0xFFF3E5F5),
                                night = Color(0xFF3D3D3D),
                            ),
                        )
                        .cornerRadius(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = getTypeDisplayName(item.type),
                    style = TextStyle(fontSize = 14.sp),
                )
            }

            Spacer(modifier = GlanceModifier.width(10.dp))

            // Content
            Column(
                modifier = GlanceModifier.defaultWeight(),
            ) {
                Text(
                    text = if (item.isSecure) "•".repeat(8) else item.preview,
                    style =
                        TextStyle(
                            color =
                                ColorProvider(
                                    day = Color(0xFF2D2D2D),
                                    night = Color(0xFFFFFFFF),
                                ),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    maxLines = 1,
                )

                Spacer(modifier = GlanceModifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatTimeAgo(item.timestamp),
                        style =
                            TextStyle(
                                color =
                                    ColorProvider(
                                        day = Color(0xFF666666),
                                        night = Color(0xFF999999),
                                    ),
                                fontSize = 10.sp,
                            ),
                    )

                    if (item.isPinned) {
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Text(
                            text = "📌",
                            style = TextStyle(fontSize = 10.sp),
                        )
                    }

                    if (item.isSecure) {
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Text(
                            text = "🔒",
                            style = TextStyle(fontSize = 10.sp),
                        )
                    }
                }
            }
        }
    }
}

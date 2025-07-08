package com.bt.clipbo.widget

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

@SuppressLint("RestrictedApi")
@Composable
fun WidgetHeader(serviceRunning: Boolean, itemCount: Int) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = "📋",
            style = TextStyle(fontSize = 14.sp)
        )

        Spacer(modifier = GlanceModifier.width(6.dp))

        Text(
            text = "Clipbo",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(
                    day = Color(0xFF2D2D2D),
                    night = Color(0xFFFFFFFF)
                )
            )
        )

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Status
        Box(
            modifier = GlanceModifier
                .size(6.dp)
                .background(
                    if (serviceRunning) ColorProvider(Color(0xFF4CAF50))
                    else ColorProvider(Color(0xFFE57373))
                )
                .cornerRadius(3.dp)
        ){
        }

        Spacer(modifier = GlanceModifier.width(4.dp))

        Text(
            text = "$itemCount",
            style = TextStyle(
                fontSize = 10.sp,
                color = ColorProvider(Color(0xFF7B4397))
            )
        )
    }
}

package com.bt.clipbo.widget

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.*

class ClipboWidget : GlanceAppWidget() {
    override val sizeMode =
        SizeMode.Responsive(
            setOf(
                // Small widget (2x1)
                DpSize(120.dp, 40.dp),
                // Medium widget (4x2) - Ana hedefimiz
                DpSize(250.dp, 110.dp),
                // Large widget (4x3)
                DpSize(250.dp, 180.dp),
            ),
        )

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        provideContent {
            GlanceTheme {
                ClipboWidgetContent()
            }
        }
    }
}

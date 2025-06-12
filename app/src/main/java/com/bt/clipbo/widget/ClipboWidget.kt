package com.bt.clipbo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.bt.clipbo.presentation.ui.main.MainActivity

class ClipboWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            // Small widget (2x1)
            DpSize(120.dp, 40.dp),
            // Medium widget (4x2) - Ana hedefimiz
            DpSize(250.dp, 110.dp),
            // Large widget (4x3)
            DpSize(250.dp, 180.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                ClipboWidgetContent()
            }
        }
    }
}
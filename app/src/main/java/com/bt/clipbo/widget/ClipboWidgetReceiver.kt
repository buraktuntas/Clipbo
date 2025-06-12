package com.bt.clipbo.widget

import androidx.glance.appwidget.GlanceAppWidget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class ClipboWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ClipboWidget()
}
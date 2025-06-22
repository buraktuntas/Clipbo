package com.bt.clipbo.utils

import com.bt.clipbo.data.database.ClipboardEntity

fun createSampleClipboardItems(): List<ClipboardEntity> {
    val currentTime = System.currentTimeMillis()

    return listOf(
        ClipboardEntity(
            id = 1,
            content = "https://github.com/clipbo/android-app",
            timestamp = currentTime - 300_000, // 5 dakika önce
            type = "URL",
            isPinned = true,
            isSecure = false,
            preview = "https://github.com/clipbo/android-app",
        ),
        ClipboardEntity(
            id = 2,
            content = "john.doe@example.com",
            timestamp = currentTime - 600_000, // 10 dakika önce
            type = "EMAIL",
            isPinned = false,
            isSecure = false,
            preview = "john.doe@example.com",
        ),
        ClipboardEntity(
            id = 3,
            content = "MySecretPassword123!",
            timestamp = currentTime - 900_000, // 15 dakika önce
            type = "PASSWORD",
            isPinned = false,
            isSecure = true,
            preview = "MySecretPassword123!",
        ),
        ClipboardEntity(
            id = 4,
            content = "+90 555 123 45 67",
            timestamp = currentTime - 1200_000, // 20 dakika önce
            type = "PHONE",
            isPinned = false,
            isSecure = false,
            preview = "+90 555 123 45 67",
        ),
        ClipboardEntity(
            id = 5,
            content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            timestamp = currentTime - 1800_000, // 30 dakika önce
            type = "TEXT",
            isPinned = true,
            isSecure = false,
            preview = "Lorem ipsum dolor sit amet, consectetur adipiscing elit...",
        ),
    )
}

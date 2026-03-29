package com.emulnk

import android.app.Application
import android.util.Log
import com.emulnk.core.MemoryService
import com.emulnk.data.ConfigManager
import com.emulnk.data.MemoryRepository
import com.emulnk.model.AppConfig

class EmuLnkApplication : Application() {
    val memoryService: MemoryService by lazy {
        val host = try {
            ConfigManager(this).getAppConfig().emulatorHost
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.w("EmuLnkApplication", "Failed to read emulator host: ${e.message}")
            AppConfig.DEFAULT_HOST
        }
        MemoryService(MemoryRepository(host = host))
    }
}

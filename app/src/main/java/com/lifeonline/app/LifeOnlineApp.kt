package com.lifeonline.app

import android.app.Application
import com.lifeonline.app.data.local.AppDatabase
import com.lifeonline.app.data.repository.LifeOnlineRepository

class LifeOnlineApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: LifeOnlineRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getDatabase(this)
        repository = LifeOnlineRepository(this, database)
    }

    companion object {
        lateinit var instance: LifeOnlineApp
            private set
    }
}

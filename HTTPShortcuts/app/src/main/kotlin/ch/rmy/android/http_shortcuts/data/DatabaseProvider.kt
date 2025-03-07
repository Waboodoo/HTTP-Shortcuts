package ch.rmy.android.http_shortcuts.data

import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseProvider
@Inject
constructor(
    @ApplicationContext
    private val context: Context,
) {
    val db by lazy {
        Room.databaseBuilder(
            context = context,
            klass = Database::class.java,
            name = "main-db",
        ).build()
    }
}

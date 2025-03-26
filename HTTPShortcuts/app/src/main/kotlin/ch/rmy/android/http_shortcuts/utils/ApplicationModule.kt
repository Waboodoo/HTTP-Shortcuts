package ch.rmy.android.http_shortcuts.utils

import android.app.Application
import android.content.Context
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.DatabaseProvider
import ch.rmy.android.scripting.ScriptingEngineFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ActivityComponent::class, ViewModelComponent::class, SingletonComponent::class)
object ApplicationModule {
    @Provides
    fun provideContext(application: Application): Context =
        application

    @Provides
    fun providePlayServicesUtil(context: Context): PlayServicesUtil =
        PlayServicesUtilImpl(context)

    @Provides
    fun provideScriptingEngineFactory(): ScriptingEngineFactory =
        ScriptingEngineFactory

    @Provides
    fun provideDatabase(databaseProvider: DatabaseProvider): Database =
        databaseProvider.db
}

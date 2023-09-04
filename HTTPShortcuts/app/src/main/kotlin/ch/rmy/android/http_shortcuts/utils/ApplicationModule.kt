package ch.rmy.android.http_shortcuts.utils

import android.app.Application
import android.content.Context
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.RealmFactoryImpl
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
    fun provideRealmFactory(context: Context): RealmFactory {
        RealmFactoryImpl.init(context)
        return RealmFactoryImpl.getInstance()
    }

    @Provides
    fun providePlayServicesUtil(context: Context): PlayServicesUtil =
        PlayServicesUtilImpl(context)
}

package ch.rmy.android.http_shortcuts.dagger

import android.content.Context
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.http_shortcuts.Application
import dagger.Module
import dagger.Provides
import ch.rmy.android.http_shortcuts.data.RealmFactory as RealmFactoryImpl

@Module
class ApplicationModule {

    @Provides
    fun provideContext(application: Application): Context =
        application

    @Provides
    fun provideRealmFactory(): RealmFactory =
        RealmFactoryImpl.getInstance()
}

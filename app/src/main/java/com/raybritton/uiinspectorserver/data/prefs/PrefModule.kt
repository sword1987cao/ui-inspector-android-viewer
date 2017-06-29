package com.raybritton.uiinspectorserver.data.prefs

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.raybritton.uiinspectorserver.ui.main.MainMvp
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PrefModule {
    private val PREF_UNIT = "unit.string"
    private val PREF_MARGIN = "margin.bool"
    private val PREF_PADDING = "padding.bool"
    private val PREF_EMPTY = "empty.bool"
    private val PREF_INVISIBLE = "invisible.bool"
    private val PREF_BORDERS = "borders.bool"

    @Provides
    @Singleton
    fun provideRxSharedPreferences(sharedPreferences: SharedPreferences): RxSharedPreferences {
        return RxSharedPreferences.create(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(application: Application): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(application)
    }

    @Provides
    @Singleton
    @ShowMargin
    fun provideShowMarginPreference(rxSharedPreferences: RxSharedPreferences): Preference<Boolean> {
        return rxSharedPreferences.getBoolean(PREF_MARGIN)
    }

    @Provides
    @Singleton
    @ShowPadding
    fun provideShowPaddingPreference(rxSharedPreferences: RxSharedPreferences): Preference<Boolean> {
        return rxSharedPreferences.getBoolean(PREF_PADDING)
    }

    @Provides
    @Singleton
    @DimenUnit
    fun provideUnitPreference(rxSharedPreferences: RxSharedPreferences): Preference<MainMvp.Unit> {
        return rxSharedPreferences.getEnum(PREF_UNIT, MainMvp.Unit.DP, MainMvp.Unit::class.java)
    }

    @Provides
    @Singleton
    @ShowEmptyAttrs
    fun provideShowEmptyAttrsPreference(rxSharedPreferences: RxSharedPreferences): Preference<Boolean> {
        return rxSharedPreferences.getBoolean(PREF_EMPTY)
    }

    @Provides
    @Singleton
    @ShowInvisibleViews
    fun provideShowInvisibleViewsPreference(rxSharedPreferences: RxSharedPreferences): Preference<Boolean> {
        return rxSharedPreferences.getBoolean(PREF_INVISIBLE)
    }

    @Provides
    @Singleton
    @ShowBorders
    fun provideShowBordersViewsPreference(rxSharedPreferences: RxSharedPreferences): Preference<Boolean> {
        return rxSharedPreferences.getBoolean(PREF_BORDERS)
    }
}

package com.raybritton.uiinspectorserver

import com.raybritton.uiinspectorserver.ui.main.MainComponent
import dagger.Module
import dagger.android.AndroidInjectionModule

@Module(includes = arrayOf(AndroidInjectionModule::class, MainComponent.MainModule::class))
class ActivityModule

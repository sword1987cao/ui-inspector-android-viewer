package com.raybritton.uiinspectorserver;

import com.raybritton.uiinspectorserver.data.prefs.PrefModule;

import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ActivityModule.class, AppModule.class, PrefModule.class})
public interface AppComponent {
    void inject(@NotNull ServerApp serverApp);
}

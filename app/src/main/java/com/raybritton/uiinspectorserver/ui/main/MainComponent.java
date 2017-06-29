package com.raybritton.uiinspectorserver.ui.main;

import android.app.Activity;
import android.app.Application;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Subcomponent
public interface MainComponent extends AndroidInjector<MainActivity> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<MainActivity> {
    }

    @Module(subcomponents = MainComponent.class)
    abstract class MainModule {
        @Binds
        @IntoMap
        @ActivityKey(MainActivity.class)
        abstract Factory<? extends Activity> bindActivityInjectorFactory(MainComponent.Builder builder);

        @Binds
        abstract MainMvp.View bindView(MainActivity mainActivity);

        @Binds
        abstract MainMvp.Presenter bindPresenter(MainPresenter mainPresenter);

        @Provides
        @Singleton
        static MainAdapter provideMainAdapter(Application application) {
            return new MainAdapter(application);
        }
    }
}

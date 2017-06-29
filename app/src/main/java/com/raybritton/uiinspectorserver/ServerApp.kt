package com.raybritton.uiinspectorserver

import android.app.Activity
import android.app.Application
import android.os.Build
import com.raybritton.inspector.Inspector
import com.raybritton.inspector.RemoteInspector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasDispatchingActivityInjector
import javax.inject.Inject

class ServerApp : Application(), HasDispatchingActivityInjector {
    private lateinit var inspector: Inspector

    val component: AppComponent = createComponent()

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()

        component.inject(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val remoteInspector = RemoteInspector()
            remoteInspector.autoInspectStart(this)
            inspector = remoteInspector.inspector
        } else {
            inspector = Inspector()
        }
        inspector.setSafeMode(true)
    }

    override fun activityInjector(): DispatchingAndroidInjector<Activity> {
        return dispatchingAndroidInjector
    }

    private fun createComponent(): AppComponent {
        return DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()
    }
}

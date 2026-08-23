package br.com.tamanhocerto.core.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * O SDK precisa de uma `Activity` para exibir o anuncio, mas `RewardGate` nao
 * a recebe: a interface vive em `:core:model`, que e Kotlin puro (ADS-SPEC
 * secao 2).
 *
 * Referencia fraca de proposito: guardar a Activity forte vazaria a tela.
 */
@Singleton
class CurrentActivityHolder @Inject constructor() {

    private var current: WeakReference<Activity>? = null

    val activity: Activity? get() = current?.get()

    fun register(application: Application) {
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityResumed(activity: Activity) {
                    current = WeakReference(activity)
                }

                override fun onActivityPaused(activity: Activity) {
                    if (current?.get() === activity) current = null
                }

                override fun onActivityCreated(activity: Activity, state: Bundle?) = Unit
                override fun onActivityStarted(activity: Activity) = Unit
                override fun onActivityStopped(activity: Activity) = Unit
                override fun onActivitySaveInstanceState(activity: Activity, out: Bundle) = Unit
                override fun onActivityDestroyed(activity: Activity) = Unit
            },
        )
    }
}

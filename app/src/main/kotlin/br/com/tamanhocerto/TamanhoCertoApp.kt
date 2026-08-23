package br.com.tamanhocerto

import android.app.Application
import br.com.tamanhocerto.core.ads.CurrentActivityHolder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Raiz da injecao. Nada de SDK de anuncio e inicializado aqui: a
 * inicializacao acontece dentro de `requestUnlock` (decisao D12).
 */
@HiltAndroidApp
class TamanhoCertoApp : Application() {

    @Inject
    lateinit var currentActivityHolder: CurrentActivityHolder

    override fun onCreate() {
        super.onCreate()
        // So registra o observador de Activity. O SDK de anuncios NAO sobe
        // aqui: ele sobe dentro de requestUnlock (D12).
        currentActivityHolder.register(this)
    }
}

package br.com.tamanhocerto

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Raiz da injecao. Nada de SDK de anuncio e inicializado aqui: a
 * inicializacao acontece dentro de `requestUnlock` (decisao D12).
 */
@HiltAndroidApp
class TamanhoCertoApp : Application()

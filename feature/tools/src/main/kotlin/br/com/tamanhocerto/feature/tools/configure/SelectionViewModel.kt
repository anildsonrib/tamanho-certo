package br.com.tamanhocerto.feature.tools.configure

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * As URIs escolhidas viajam por aqui, e nao por argumento de rota: lista de
 * URI em rota estoura o limite de tamanho e quebra com nome acentuado
 * (UI-SPEC secao 2). Guardadas no `SavedStateHandle` para sobreviver a morte
 * de processo.
 */
@HiltViewModel
class SelectionViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
) : ViewModel() {

    private val _selection = MutableStateFlow(savedState.get<List<String>>(KEY).orEmpty())
    val selection: StateFlow<List<String>> = _selection.asStateFlow()

    val uris: List<Uri> get() = _selection.value.map(Uri::parse)

    fun select(uris: List<Uri>) {
        val values = uris.map(Uri::toString)
        savedState[KEY] = values
        _selection.value = values
    }

    fun clear() = select(emptyList())

    private companion object {
        const val KEY = "selected_uris"
    }
}

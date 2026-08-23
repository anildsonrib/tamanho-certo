package br.com.tamanhocerto.core.files

import android.content.Context
import br.com.tamanhocerto.core.files.EngineDefaults.WORK_DIR_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * `cacheDir/work` e o UNICO lugar em que o app escreve. Nada do usuario vive
 * fora dai (ARCHITECTURE.md secao 6).
 */
@Singleton
class WorkDir @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    val dir: File get() = File(context.cacheDir, WORK_DIR_NAME)

    /** Chamado no inicio de TODA operacao: apaga o que sobrou da anterior. */
    suspend fun prepare(): File = withContext(Dispatchers.IO) {
        clearBlocking()
        dir.apply { mkdirs() }
    }

    suspend fun allocate(name: String): File = withContext(Dispatchers.IO) {
        dir.mkdirs()
        File(dir, name)
    }

    /** Chamado no onDestroy da MainActivity e no prepare() seguinte. */
    suspend fun clear() = withContext(Dispatchers.IO) { clearBlocking() }

    private fun clearBlocking() {
        dir.listFiles()?.forEach { it.deleteRecursively() }
    }
}

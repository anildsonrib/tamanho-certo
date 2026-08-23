package br.com.tamanhocerto.core.files

/** Constantes fechadas da ENGINE-SPEC secao 2. */
object EngineDefaults {
    /** Subpasta de cacheDir. E o unico caminho que o file_paths.xml expoe. */
    const val WORK_DIR_NAME = "work"

    /** Original + saida + folga: exige 3x o tamanho da entrada livre. */
    const val FREE_SPACE_MARGIN = 3.0f

    /** 20 MB, piso absoluto. */
    const val MIN_FREE_SPACE_BYTES = 20L * 1024 * 1024

    /** Quantos arquivos por operacao sem recompensa (D7). */
    const val FREE_BATCH_LIMIT = 1

    /** Teto por lote; acima disso a UI pede para dividir. */
    const val MAX_BATCH_ITEMS = 50

    /** Intervalo minimo entre emissoes de progresso. */
    const val PROGRESS_THROTTLE_MS = 100L

    /** Corte da base do nome, para nao estourar limite de sistema de arquivos. */
    const val MAX_BASE_NAME_LENGTH = 60
}

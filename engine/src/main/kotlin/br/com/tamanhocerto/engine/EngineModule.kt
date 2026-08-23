package br.com.tamanhocerto.engine

import br.com.tamanhocerto.core.files.WorkDir
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    /** O engine so conhece `prepare()`; o resto do WorkDir nao lhe interessa. */
    @Provides
    @Singleton
    fun provideWorkspace(workDir: WorkDir): DefaultOperationEngine.Workspace =
        object : DefaultOperationEngine.Workspace {
            override suspend fun prepare(): File = workDir.prepare()
        }

    @Provides
    @Singleton
    fun provideItemProcessor(processor: PipelineItemProcessor): ItemProcessor = processor

    @Provides
    @Singleton
    fun provideOperationEngine(
        processor: ItemProcessor,
        workspace: DefaultOperationEngine.Workspace,
    ): OperationEngine = DefaultOperationEngine(processor, workspace)
}

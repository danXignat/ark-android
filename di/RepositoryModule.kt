// ...existing code...
import com.danignat.ark.model.TaskModel
import com.danignat.ark.repository.TaskRepository
import dagger.Binds
import dagger.Module
// ...existing code...
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepository): ITaskRepository
}


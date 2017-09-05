package io.github.vladimirmi.photon.features.author

import dagger.Provides
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.data.mappers.AlbumCachingMapper
import io.github.vladimirmi.photon.data.mappers.UserCachingMapper
import io.github.vladimirmi.photon.data.repository.user.UserRepository
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.features.root.RootActivityComponent
import io.github.vladimirmi.photon.features.root.RootPresenter

/**
 * Created by Vladimir Mikhalev 22.06.2017.
 */

class AuthorScreen(val userId: String) : BaseScreen<RootActivityComponent>() {

    override val layoutResId = R.layout.screen_author

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Component {
        return parentComponent.authorComponentBuilder()
                .module(Module())
                .build()
    }

    @dagger.Module
    class Module {

        @Provides
        @DaggerScope(AuthorScreen::class)
        fun provideAuthorModel(userRepository: UserRepository,
                               userMapper: UserCachingMapper,
                               albumsMapper: AlbumCachingMapper): IAuthorModel =
                AuthorModel(userRepository, userMapper, albumsMapper)

        @Provides
        @DaggerScope(AuthorScreen::class)
        fun provideAuthorPresenter(model: IAuthorModel,
                                   rootPresenter: RootPresenter): AuthorPresenter =
                AuthorPresenter(model, rootPresenter)
    }

    @DaggerScope(AuthorScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun module(module: Module): Component.Builder
            fun build(): Component
        }

        fun inject(authorView: AuthorView)
    }

    //endregion

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        if (!super.equals(other)) return false
        other as AuthorScreen
        if (userId != other.userId) return false
        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + userId.hashCode()
        return result
    }
}
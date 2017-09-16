package io.github.vladimirmi.photon.presentation.author

import dagger.Binds
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.domain.interactors.AuthorInteractorImpl
import io.github.vladimirmi.photon.presentation.root.RootActivityComponent

/**
 * Created by Vladimir Mikhalev 22.06.2017.
 */

class AuthorScreen(val userId: String) : BaseScreen<RootActivityComponent>() {

    override val layoutResId = R.layout.screen_author

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Component =
            parentComponent.authorComponentBuilder().build()

    @dagger.Module
    interface Module {
        @Binds
        @DaggerScope(AuthorScreen::class)
        fun authorInteractor(interactor: AuthorInteractorImpl): AuthorInteractor
    }

    @DaggerScope(AuthorScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
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
package io.github.vladimirmi.photon.features.profile

import com.birbit.android.jobqueue.JobManager
import dagger.Provides
import dagger.Subcomponent
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseScreen
import io.github.vladimirmi.photon.data.managers.DataManager
import io.github.vladimirmi.photon.di.DaggerScope
import io.github.vladimirmi.photon.features.root.RootActivityComponent
import io.github.vladimirmi.photon.features.root.RootPresenter

/**
 * Created by Vladimir Mikhalev 15.06.2017.
 */

class ProfileScreen : BaseScreen<RootActivityComponent>() {

    override val layoutResId = R.layout.screen_profile

    //region =============== DI ==============

    override fun createScreenComponent(parentComponent: RootActivityComponent): Component {
        return parentComponent.profileComponentBuilder()
                .module(Module())
                .build()
    }

    @dagger.Module
    class Module {

        @Provides
        @DaggerScope(ProfileScreen::class)
        fun provideProfileModel(dataManager: DataManager, jobManager: JobManager): IProfileModel {
            return ProfileModel(dataManager, jobManager)
        }

        @Provides
        @DaggerScope(ProfileScreen::class)
        fun provideProfilePresenter(model: IProfileModel, rootPresenter: RootPresenter): ProfilePresenter {
            return ProfilePresenter(model, rootPresenter)
        }
    }

    @DaggerScope(ProfileScreen::class)
    @dagger.Subcomponent(modules = arrayOf(Module::class))
    interface Component {
        @Subcomponent.Builder
        interface Builder {
            fun module(module: Module): Component.Builder
            fun build(): Component

        }

        fun inject(profileView: ProfileView)

    }

    //endregion
}


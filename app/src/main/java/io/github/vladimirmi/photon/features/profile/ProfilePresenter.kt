package io.github.vladimirmi.photon.features.profile

import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.root.RootPresenter

class ProfilePresenter(model: IProfileModel, rootPresenter: RootPresenter)
    : BasePresenter<ProfileView, IProfileModel>(model, rootPresenter) {

    override fun initToolbar() {
        rootPresenter.getNewToolbarBuilder().build()
    }

    override fun initView(view: ProfileView) {
        if (!model.isUserAuth()) {
            view.showAuth()
        }
    }

}


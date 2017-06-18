package io.github.vladimirmi.photon.features.profile

import android.view.LayoutInflater
import android.view.MenuItem
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.reactivex.disposables.Disposable
import timber.log.Timber

class ProfilePresenter(model: IProfileModel, rootPresenter: RootPresenter)
    : BasePresenter<ProfileView, IProfileModel>(model, rootPresenter) {

    override fun initToolbar() {
        val actionView = LayoutInflater.from(view.context).inflate(R.layout.view_menu_item, view, false)
        val actions: (MenuItem) -> Unit = {
            when (it.itemId) {
                R.id.edit -> edit()
                R.id.delete -> delete()
            }
        }

        rootPresenter.getNewToolbarBuilder()
                .addAction(MenuItemHolder("Actions",
                        iconResId = R.drawable.ic_action_more,
                        actions = actions,
                        actionView = actionView,
                        popupMenu = R.menu.submenu_profile_screen))
                .build()
    }

    private fun edit() {
        Timber.e("edit")
    }

    private fun delete() {
        Timber.e("delete")
    }


    override fun initView(view: ProfileView) {
        if (!model.isUserAuth()) {
            view.showAuth()
        } else {
            compDisp.add(subscribeOnProfile())
            view.showProfile()
        }
    }

    private fun subscribeOnProfile(): Disposable {
        return model.getUser()
                .subscribe { view.setProfile(it) }
    }

}


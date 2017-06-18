package io.github.vladimirmi.photon.features.profile

import android.view.MenuItem
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BasePresenter
import io.github.vladimirmi.photon.data.models.SignInReq
import io.github.vladimirmi.photon.data.models.SignUpReq
import io.github.vladimirmi.photon.data.network.ApiError
import io.github.vladimirmi.photon.features.root.MenuItemHolder
import io.github.vladimirmi.photon.features.root.RootPresenter
import io.reactivex.disposables.Disposable
import timber.log.Timber

class ProfilePresenter(model: IProfileModel, rootPresenter: RootPresenter)
    : BasePresenter<ProfileView, IProfileModel>(model, rootPresenter) {

    override fun initToolbar() {
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

    fun register(req: SignUpReq) {
        compDisp.add(rootPresenter.register(req)
                .subscribe({}, {
                    // onError
                    if (it is ApiError) view.showMessage(it.errorResId)
                }, {
                    //onComplete
                    view.closeRegistrationDialog()
                    view.showProfile()
                }))
    }

    fun login(req: SignInReq) {
        compDisp.add(rootPresenter.login(req)
                .doOnSubscribe { rootPresenter.showLoading() }
                .doAfterTerminate { rootPresenter.hideLoading() }
                .subscribe({}, {
                    // onError
                    if (it is ApiError) {
                        when (it.statusCode) {
                            404 -> view.showMessage(R.string.message_api_err_auth)
                            else -> view.showMessage(it.errorResId)
                        }
                    }
                }, {
                    //onComplete
                    view.closeLoginDialog()
                    view.showProfile()
                }))
    }

}


package io.github.vladimirmi.photon.ui

import android.graphics.drawable.GradientDrawable
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.Patterns
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.jakewharton.rxbinding2.widget.afterTextChangeEvents
import io.github.vladimirmi.photon.R
import io.github.vladimirmi.photon.core.BaseDialog
import io.github.vladimirmi.photon.di.DaggerService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.regex.Pattern

/**
 * Created by Vladimir Mikhalev 15.06.2017.
 */

open class ValidationDialog(layoutId: Int, viewGroup: ViewGroup)
    : BaseDialog(layoutId, viewGroup) {
    private val colorNormal = ContextCompat.getColor(viewGroup.context, R.color.grey_light)
    private val colorError = ContextCompat.getColor(viewGroup.context, R.color.error)
    private val colorText = ContextCompat.getColor(viewGroup.context, R.color.text_color)

    val LOGIN_PATTERN = Pattern.compile("[a-zA-Z0-9_]{3,20}")
    val EMAIL_PATTERN = Patterns.EMAIL_ADDRESS
    val NAME_PATTERN = Pattern.compile(".{3,20}")
    val PASSWORD_PATTERN = Pattern.compile("[a-zA-Z0-9_]{8,}")
    val DESCRIPTION_PATTERN = Pattern.compile(".{3,400}")

    protected fun getValidObs(field: EditText, pattern: Pattern, errorField: TextView, error: String): Observable<Boolean> {
        return field.afterTextChangeEvents()
                .skipInitialValue()
                .map { pattern.matcher(it.editable().toString()).matches() }
                .doOnNext { matches ->
                    val drawable = field.background as GradientDrawable
                    if (matches) {
                        drawable.setStroke(3, colorNormal)
                        field.setTextColor(colorText)
                        errorField.text = ""
                    } else {
                        drawable.setStroke(3, colorError)
                        field.setTextColor(colorError)
                        errorField.text = error
                    }
                }.startWith(false)
    }

    protected fun getNetObs(errorMsg: String): Observable<Boolean> {
        return DaggerService.appComponent.dataManager().isNetworkAvailable()
                .doOnNext {
                    if (!it) Snackbar.make(viewGroup, errorMsg, Snackbar.LENGTH_LONG).show()
                }
                .observeOn(AndroidSchedulers.mainThread())
    }
}
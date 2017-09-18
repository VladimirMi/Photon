package io.github.vladimirmi.photon.ui.dialogs

import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.util.Patterns
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.jakewharton.rxbinding2.widget.afterTextChangeEvents
import io.github.vladimirmi.photon.R
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.regex.Pattern

/**
 * Created by Vladimir Mikhalev 15.06.2017.
 */

abstract class ValidationDialog(layoutId: Int, viewGroup: ViewGroup)
    : BaseDialog(layoutId, viewGroup) {
    private val colorNormal = ContextCompat.getColor(viewGroup.context, R.color.grey_light)
    private val colorError = ContextCompat.getColor(viewGroup.context, R.color.error)
    private val colorText = ContextCompat.getColor(viewGroup.context, R.color.text_color)

    protected val LOGIN_PATTERN: Pattern = Pattern.compile("[a-zA-Z0-9_]{3,20}")
    protected val EMAIL_PATTERN: Pattern = Patterns.EMAIL_ADDRESS
    protected val NAME_PATTERN: Pattern = Pattern.compile(".{3,20}")
    protected val PASSWORD_PATTERN: Pattern = Pattern.compile("[a-zA-Z0-9_]{8,}")
    protected val DESCRIPTION_PATTERN: Pattern = Pattern.compile(".{3,400}", Pattern.DOTALL)

    protected val compDisp = CompositeDisposable()

    protected fun EditText.validate(pattern: Pattern, errorField: TextView, error: String)
            : Observable<Boolean> {
        val drawable = background as GradientDrawable
        return afterTextChangeEvents()
                .skipInitialValue()
                .map { pattern.matcher(it.editable().toString()).matches() }
                .doOnNext { matches ->
                    if (matches) {
                        drawable.setStroke(3, colorNormal)
                        setTextColor(colorText)
                        errorField.text = ""
                    } else {
                        drawable.setStroke(3, colorError)
                        setTextColor(colorError)
                        errorField.text = error
                    }
                }
    }

    protected fun validateForm(observables: List<Observable<Boolean>>): Observable<Boolean> {
        return Observable.combineLatest(observables, { it.all { it as Boolean } })
                .startWith(false)
    }

    override fun hide() {
        super.hide()
        compDisp.clear()
    }

    override fun show() {
        super.show()
        compDisp.add(listenFields())
    }

    abstract fun listenFields(): Disposable
}

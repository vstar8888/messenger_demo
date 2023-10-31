package ru.demo.messenger.feedback

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import biz.growapp.base.extensions.loadVector
import biz.growapp.base.loading.BaseAppLoadingActivity
import kotlinx.android.synthetic.main.activity_feedback.*
import ru.demo.messenger.R
import ru.demo.messenger.internal.di.Injection
import ru.demo.messenger.utils.DisplayUtils

class FeedbackActivity : BaseAppLoadingActivity(), FeedbackPresenter.View, FeedbackPopupDialog.Callback {

    private lateinit var presenter: FeedbackPresenter
    private var companyTitle: String? = null
    private var email: String? = null
    private var phoneMobile: String? = null
    private lateinit var text: String

    override fun getMainContainerId(): Int = R.id.vgRoot

    companion object {
        private val TAG = FeedbackActivity::class.java.simpleName

        fun createIntent(context: Context): Intent {
            return Intent(context, FeedbackActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        tvSend.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_btn_send.loadVector(this), null, null, null)
        setupListeners()

        switchToMain(false)
        presenter = FeedbackPresenter(
                this,
                Injection.provideFeedback(this),
                Injection.uiScheduler
        )
        presenter.loadLoginData()
    }

    private fun setupListeners() {
        vgSendFeedback.setOnClickListener {
            sendFeedback()
        }

        btnClear.setOnClickListener {
            etPhoneOrEmail.text.clear()
        }

        etPhoneOrEmail.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (text.isNullOrEmpty()) {
                    btnClear.visibility = View.INVISIBLE
                } else {
                    btnClear.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    override fun onLoginDataLoaded(phone: String?) {
        if (!TextUtils.isEmpty(phone)) {
            etPhoneOrEmail.setText(phone)
            etPhoneOrEmail.setSelection(phone!!.length)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_feedback, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_ready -> {
                sendFeedback()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun validateData(): Boolean {
        var result = true
        if (etProblemDescription.text.isEmpty()) {
            tilProblemDescription.hint = null
            tilProblemDescription.error = getString(R.string.feedback_problem_description)
            result = false
        } else {
            text = etProblemDescription.text.toString()
        }
        if (etPhoneOrEmail.text.isEmpty()) {
            tilPhoneOrEmail.hint = null
            tilPhoneOrEmail.error = getString(R.string.feedback_your_phone_or_email)
            result = false
        } else {
            val inputString = etPhoneOrEmail.text.toString()
            if (Patterns.PHONE.matcher(inputString).matches()) {
                phoneMobile = inputString
            } else {
                if (Patterns.EMAIL_ADDRESS.matcher(inputString).matches()) {
                    email = inputString
                } else {
                    tilPhoneOrEmail.hint = null
                    tilPhoneOrEmail.error = getString(R.string.feedback_phone_or_email_error)
                    result = false
                }
            }
        }
        companyTitle = etCompany.text.toString()
        return result
    }

    private fun sendFeedback() {
        if (validateData()) {
            DisplayUtils.hideSoftKeyboard(this)
            switchToLoading(true)
            presenter.sendFeedback(getString(R.string.app_name), companyTitle, email, phoneMobile, text)
        }
    }

    override fun feedbackSent() {
        switchToMain(true)
        FeedbackPopupDialog.newInstance(this).show(supportFragmentManager, TAG)
    }

    override fun onError(throwable: Throwable) {
        switchToError(true, throwable.message)
    }

    override fun onOkClick() {
        finish()
    }

    override fun onRetryButtonClick(v: View?) {
        switchToMain(true)
    }

    override fun onDestroy() {
        presenter.onDestroyView()
        super.onDestroy()
    }
}

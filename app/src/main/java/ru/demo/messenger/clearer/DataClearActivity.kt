package ru.demo.messenger.clearer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import biz.growapp.base.BaseAppActivity
import ru.demo.messenger.R

class DataClearActivity : BaseAppActivity(), DataClearPresenter.View {

    companion object {
        fun createIntent(context: Context) = Intent(context, DataClearActivity::class.java)
    }

    private lateinit var presenter: DataClearPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = DataClearPresenter(this)
        presenter.start()
    }

    override fun showPopup() {
        executeResumeAction {
            AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(R.string.clear_data_clear_text)
                    .setPositiveButton(R.string.clear_data_clear_button) { _, _ ->
                        presenter.clear(this)
                    }
                    .show()
        }
    }

}
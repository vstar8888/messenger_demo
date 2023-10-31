package ru.demo.messenger.feedback


import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_feedback_popup.*
import ru.demo.messenger.R


class FeedbackPopupDialog : AppCompatDialogFragment() {

    companion object {
        fun newInstance(callback: Callback): FeedbackPopupDialog {
            val dialog = FeedbackPopupDialog()
            dialog.callback = callback
            dialog.isCancelable = false
            return dialog
        }
    }

    interface Callback {
        fun onOkClick()
    }

    private var callback: Callback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feedback_popup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnOK.setOnClickListener {
            if (callback != null) {
                callback?.onOkClick()
            }
            dismiss()
        }
    }
}

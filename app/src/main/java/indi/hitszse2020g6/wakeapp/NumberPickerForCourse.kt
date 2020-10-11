package indi.hitszse2020g6.wakeapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.course_week_add_fragment.*


class WeekPickerFragment: DialogFragment(){
    internal lateinit var listener: WeekPickerDialogListener
    interface WeekPickerDialogListener{
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            listener = context as WeekPickerDialogListener
        }catch (e: ClassCastException){
            throw ClassCastException(
                (context.toString() +
                        " must implement NoticeDialogListener")
            )
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            builder.context.theme.applyStyle(R.style.MyAlertDialog, true)

            builder.setView(inflater.inflate(R.layout.course_week_add_fragment, null))
                .setPositiveButton(R.string.dialog_ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onDialogNegativeClick(this)
                    })
                .setNegativeButton(R.string.dialog_cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onDialogNegativeClick(this)
                    })
//            numberPicker1.maxValue = 6



            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
//        return super.onCreateDialog(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val window = dialog!!.window ?: return
        val params = window.attributes
//        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
//        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        params.width = 820
        params.height = 820
        window.attributes = params
    }
}
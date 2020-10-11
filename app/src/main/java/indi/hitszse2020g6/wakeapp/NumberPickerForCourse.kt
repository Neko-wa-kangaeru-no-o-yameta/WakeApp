package indi.hitszse2020g6.wakeapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.course_week_add_fragment.*


class WeekPickerFragment: DialogFragment(){
    var dayOfWeek:Int = -1
    var time :Int = -1
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

        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater;
        builder.context.theme.applyStyle(R.style.MyAlertDialog, true)
        val view = inflater.inflate(R.layout.course_week_add_fragment, null)
        val npDayOfWeek = view.findViewById<NumberPicker>(R.id.numberPicker1)
        val npTime = view.findViewById<NumberPicker>(R.id.numberPicker2)
        npDayOfWeek.minValue = 1
        npDayOfWeek.maxValue = 7
        npTime.maxValue = 6
        npTime.minValue = 1

        builder.setView(view)
            .setPositiveButton(R.string.dialog_ok,
                DialogInterface.OnClickListener { dialog, id ->
                    dayOfWeek = npDayOfWeek.value
                    Log.d("dialog",dayOfWeek.toString())
                    time = npTime.value
                    listener.onDialogPositiveClick (this)
                })
            .setNegativeButton(R.string.dialog_cancel,
                DialogInterface.OnClickListener { dialog, id ->


                    listener.onDialogNegativeClick(this)
                })

        builder.create()

        return builder.create()

    }

    override fun onResume() {
        super.onResume()
        val window = dialog!!.window ?: return
        val params = window.attributes
//        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
//        params.height = ViewGroup.LayoutParams.WRAP_CONTENT

        params.width = 900
        params.height = 900
        window.attributes = params
    }
}
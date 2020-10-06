package indi.hitszse2020g6.wakeapp.eventDetail

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*


class DatePickerFragment: DialogFragment() {
    private lateinit var listener: DatePickerDialog.OnDateSetListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as DatePickerDialog.OnDateSetListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        return DatePickerDialog(activity as Context,listener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(
            Calendar.DAY_OF_MONTH))
    }
}


class TimePickerFragment: DialogFragment(){
    private lateinit var listener: TimePickerDialog.OnTimeSetListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as TimePickerDialog.OnTimeSetListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        return TimePickerDialog(activity, listener, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true)
    }
}
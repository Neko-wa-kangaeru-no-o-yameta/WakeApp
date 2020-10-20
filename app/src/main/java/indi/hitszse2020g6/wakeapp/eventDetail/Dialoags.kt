package indi.hitszse2020g6.wakeapp.eventDetail

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import indi.hitszse2020g6.wakeapp.*
import java.lang.IllegalStateException
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

class RepeatTypeDialog: DialogFragment(){
    interface RepeatTypeListener {
        abstract fun onRepeatTypeSet(doRepeat: Boolean)
    }

    private lateinit var listener: RepeatTypeListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as RepeatTypeListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return super.onCreateDialog(savedInstanceState)
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.RepeatTypeDialogTitle)
                .setItems(R.array.repeat_types,
                DialogInterface.OnClickListener { _, which ->
                    listener.onRepeatTypeSet(which == 1)
                })
            builder.create()
        } ?: throw IllegalStateException("Null activity @ RepeatTypeDialog::onCreateDialog.")
    }
}

class RepeatWeekdayDialog(private var repeatAt: Int): DialogFragment() {
    interface RepeatWeekDayDialogListener {
        abstract fun onRepeatWeekdaySet(repeatAt: Int)
    }

    private lateinit var listener: RepeatWeekDayDialogListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as RepeatWeekDayDialogListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it).apply {
                setTitle(R.string.RepeatTypeDialogTitle)
                setMultiChoiceItems(
                    R.array.repeat_weekday_types,
                    arrayOf(
                        repeatAt and MONDAY,
                        repeatAt and TUESDAY,
                        repeatAt and WEDNESDAY,
                        repeatAt and THURSDAY,
                        repeatAt and FRIDAY,
                        repeatAt and SATURDAY,
                        repeatAt and SUNDAY
                    ).map { i -> i!=0 }.toBooleanArray(),
                    DialogInterface.OnMultiChoiceClickListener { _, which, isChecked ->
                        repeatAt = if (isChecked) {
                            repeatAt or (1 shl which)    // should work
                        } else {
                            repeatAt and (1 shl which).inv()
                        }
                    }
                )
                setPositiveButton(
                    R.string.confirm,
                    DialogInterface.OnClickListener{ _, _ ->
                        listener.onRepeatWeekdaySet(repeatAt)
                    }
                )
                setNegativeButton(
                    R.string.cancel,
                    DialogInterface.OnClickListener { _, _ ->
                        listener.onRepeatWeekdaySet(0)
                    }
                )
            }
            builder.create()
        } ?: throw IllegalStateException("Null activity @ RepeatWeekdayDialog::onCreateDialog.")
    }
}

class ReminderChooseDialog(private var delta: Long, private var pos: Int):DialogFragment() {
    interface ReminderChooseListener {
        abstract fun onReminderChosen(delta: Long, pos: Int)
    }

    private lateinit var listener: ReminderChooseListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("ReminderChooseDialog", "$context")
        listener = context as ReminderChooseListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return super.onCreateDialog(savedInstanceState)
        return activity?.let {
            val builder = AlertDialog.Builder(it).apply {
                val view = layoutInflater.inflate(R.layout.reminder_picker_dialog, null)
                setTitle("输入提醒时间")
                setView(view)
                setPositiveButton(
                    R.string.confirm,
                    DialogInterface.OnClickListener{ _, _ ->
                        val day = view.findViewById<EditText>(R.id.reminder_picker_dialog_day_input).text.toString().toLong()
                        val hour = view.findViewById<EditText>(R.id.reminder_picker_dialog_hour_input).text.toString().toLong()
                        val minute = view.findViewById<EditText>(R.id.reminder_picker_dialog_minute_input).text.toString().toLong()
                        delta = (((day * 24 + hour) * 60) + minute) * 60
                        listener.onReminderChosen(delta, pos)
                    }
                )
                setNegativeButton(
                    R.string.cancel,
                    DialogInterface.OnClickListener { _, _ ->
                        listener.onReminderChosen(delta, pos)
                    }
                )
            }
            builder.create()
        } ?: throw IllegalStateException("Null activity @ ReminderChooseDialog::onCreateDialog.")
    }
}
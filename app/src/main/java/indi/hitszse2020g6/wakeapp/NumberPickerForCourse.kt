package indi.hitszse2020g6.wakeapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment


class TimePickFragment(position: Int): DialogFragment(){
    var dayOfWeek:Int = -1
    var time :Int = -1
    var position : Int = position
    //    internal lateinit var listener: WeekPickerDialogListener
    internal lateinit var listener: TimePickerDialogListener
    interface TimePickerDialogListener{
        fun onDialogPositiveClickForTime(dialog: DialogFragment)
        fun onDialogNegativeClickForTime(dialog: DialogFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            listener = context as TimePickerDialogListener
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
        npDayOfWeek.wrapSelectorWheel = false
        npTime.maxValue = 6
        npTime.minValue = 1
        npTime.wrapSelectorWheel = false
        builder.setView(view)
            .setPositiveButton(R.string.dialog_ok,
                DialogInterface.OnClickListener { dialog, id ->
                    dayOfWeek = npDayOfWeek.value
                    Log.d("dialog",dayOfWeek.toString())
                    time = npTime.value
                    listener.onDialogPositiveClickForTime (this)
                })
            .setNegativeButton(R.string.dialog_cancel,
                DialogInterface.OnClickListener { dialog, id ->


                    listener.onDialogNegativeClickForTime(this)
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

class WeekPickerFragment(position:Int) :DialogFragment(){
    var weekBegin: Int = -1
    var weekEnd : Int = -1
    var position :Int = position
    internal lateinit var listener:WeekPickerDialogListner

    interface WeekPickerDialogListner{
        fun onDialogPositiveClickForWeek(dialog: DialogFragment)
        fun onDialogNegativeClickForWeek(dialog: DialogFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            listener = context as WeekPickerDialogListner
        }catch (e: ClassCastException){
            throw ClassCastException(
                (context.toString() +
                        " must implement NoticeDialogListener")
            )
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d("get in--------------------------------------------------","weekPickFragment")
        val builder = AlertDialog.Builder(activity)
        val infalter = requireActivity().layoutInflater;
        builder.context.theme.applyStyle(R.style.MyAlertDialog,true)
        val view = infalter.inflate(R.layout.week_picker_dailog,null)
        val npWeekBegin = view.findViewById<NumberPicker>(R.id.weekPicker1)
        val npWeekEnd = view.findViewById<NumberPicker>(R.id.weekPicker2)
        npWeekBegin.maxValue = 20
        npWeekBegin.minValue = 1
        npWeekEnd.maxValue = 20
        npWeekEnd.minValue = 1
        builder.setView(view)
            .setPositiveButton(R.string.dialog_ok,
                DialogInterface.OnClickListener { dialog, id ->

                    weekBegin = npWeekBegin.value
                    weekEnd = npWeekEnd.value
                    listener.onDialogPositiveClickForWeek (this)
                })
            .setNegativeButton(R.string.dialog_cancel,
            DialogInterface.OnClickListener { dialog, id ->


                listener.onDialogNegativeClickForWeek(this)
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

//选择界面
class CourseChangeSelectFragment :DialogFragment(){
    var selectItem:Int = -1
    internal lateinit var listener:CourseChangeSelectDailogListner
    interface CourseChangeSelectDailogListner{
        fun onDialogPositiveClickForCourseChangeSelect(dialog: DialogFragment)
        fun onDialogNegativeClickForCourseChangeSelect(dialog: DialogFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            listener = context as CourseChangeSelectDailogListner
        }catch (e: ClassCastException) {
            throw ClassCastException(
                (context.toString() +
                        " must implement CourseChangeSelectFragment")
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val infalter = requireActivity().layoutInflater;
        builder.setTitle(R.string.pick_topic)
            .setSingleChoiceItems(R.array.selectChoice,-1,
                DialogInterface.OnClickListener { dialog, which ->
                    selectItem = which
                }
            )
            .setPositiveButton(R.string.dialog_ok,
                DialogInterface.OnClickListener { dialog, which ->
                    Log.d("selectItem",selectItem.toString())
                    listener.onDialogPositiveClickForCourseChangeSelect(this)
                })
            .setNegativeButton(R.string.dialog_cancel,
                DialogInterface.OnClickListener { dialog, which ->
                    listener.onDialogNegativeClickForCourseChangeSelect(this)
                })
        builder.create()
        return builder.create()
    }
}

package cn.starrah.thu_course_helper.fragment

import android.app.Activity
import android.content.Intent
import cn.starrah.thu_course_helper.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import cn.starrah.thu_course_helper.TableFragment
import cn.starrah.thu_course_helper.activity.ItemEditActivity
import kotlinx.coroutines.launch


class Information : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.information, container, false)
    }

    override fun onStart() {
        super.onStart()

        //显示校历
        var button_calendar: Button = requireActivity().findViewById(R.id.show_calendar)
        button_calendar.setOnClickListener(View.OnClickListener {
            //var intent = Intent(requireActivity(), ItemEditActivity::class.java)
            //intent.putExtra(TableFragment.EXTRA_MESSAGE, -1)
            //requireActivity().startActivity(intent)
        })

        //显示时间节点
        var button_time_point: Button = requireActivity().findViewById(R.id.show_time_point)
        button_time_point.setOnClickListener(View.OnClickListener {
            //var intent = Intent(requireActivity(), ItemEditActivity::class.java)
            //intent.putExtra(TableFragment.EXTRA_MESSAGE, -1)
            //requireActivity().startActivity(intent)
        })

        //显示空教室
        var button_classroom: Button = requireActivity().findViewById(R.id.show_classroom)
        button_classroom.setOnClickListener(View.OnClickListener {
            //var intent = Intent(requireActivity(), ItemEditActivity::class.java)
            //intent.putExtra(TableFragment.EXTRA_MESSAGE, -1)
            //requireActivity().startActivity(intent)
        })

        //显示研讨间
        var button_self_study: Button = requireActivity().findViewById(R.id.show_self_study)
        button_self_study.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                requireActivity(),
                "由于疫情原因，学校研讨间系统暂时关闭，暂时无法查看研讨间信息！",
                Toast.LENGTH_SHORT
            ).show()
        })

        //显示作业
        var button_homework: Button = requireActivity().findViewById(R.id.show_homework)
        button_homework.setOnClickListener(View.OnClickListener {
            //var intent = Intent(requireActivity(), ItemEditActivity::class.java)
            //intent.putExtra(TableFragment.EXTRA_MESSAGE, -1)
            //requireActivity().startActivity(intent)
        })

        //显示校历
        var button_exam: Button = requireActivity().findViewById(R.id.show_exam)
        button_exam.setOnClickListener(View.OnClickListener {
            //var intent = Intent(requireActivity(), ItemEditActivity::class.java)
            //intent.putExtra(TableFragment.EXTRA_MESSAGE, -1)
            //requireActivity().startActivity(intent)
        })
    }
}
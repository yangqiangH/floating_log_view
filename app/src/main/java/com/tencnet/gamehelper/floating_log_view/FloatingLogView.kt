package com.tencnet.gamehelper.floating_log_view

import android.content.Context
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.imuxuan.floatingview.FloatingMagnetView
import com.imuxuan.floatingview.FloatingView
import com.imuxuan.floatingview.MagnetViewListener
import com.tencnet.gamehelper.floating_log_view.databinding.FloatingLogViewBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * @Description:    Window顶层显示的日志浮窗
 * @CreateDate:     2020/5/13 3:32 PM
 * @Version:        1.0
 */

class FloatingLogView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    FloatingMagnetView(context, attrs, defStyleAttr) {

    private var adapter: BaseAdapter? = null

    var magnetListener = object : MagnetViewListener {
        override fun onClick(magnetView: FloatingMagnetView?) {

            binding.open.visibility = View.GONE
            binding.toolContent.visibility = View.VISIBLE

            FloatingView.get().listener(null)
        }

        override fun onRemove(magnetView: FloatingMagnetView?) {
        }

    }

    private val binding: FloatingLogViewBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.floating_log_view, this, true)

    private val logDateSetChangedCallback = object : LogDataSetChangedCallback {

        override fun onChanged() {
            val runnable = Runnable {
                adapter?.notifyDataSetChanged()
            }
            if (Looper.myLooper() == Looper.getMainLooper()) {
                runnable.run()
            } else {
                LogViewManager.handler.post(runnable)
            }
        }
    }

    init {

        binding.shrink.setOnClickListener {
            binding.open.visibility = View.VISIBLE
            binding.toolContent.visibility = View.GONE
            FloatingView.get().listener(magnetListener)
        }

        binding.close.setOnClickListener {
            LogViewManager.destroyView(context)
        }

        LogViewManager.mLogSetChangedListener = (logDateSetChangedCallback)

        binding.lvLogs.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
            }

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    binding.lvLogs.transcriptMode = AbsListView.TRANSCRIPT_MODE_NORMAL
                } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {

                    if (binding.lvLogs.childCount > 0 && binding.lvLogs.getChildAt(binding.lvLogs.childCount - 1).bottom == binding.lvLogs.height) {
                        //开启自动滚动
                        binding.lvLogs.transcriptMode = AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
                        binding.cbAutoScroll.isChecked = true
                    } else {
                        binding.lvLogs.transcriptMode = AbsListView.TRANSCRIPT_MODE_NORMAL
                        binding.cbAutoScroll.isChecked = false
                    }
                }
            }
        })

        binding.lvLogs.transcriptMode = AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL

        adapter = LogAdapter()
        binding.lvLogs.adapter = adapter

        binding.etFilter.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                LogViewManager.keywordFilter = s?.toString()?.trim() ?: ""
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        binding.spinnerFilters.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    binding.etFilter.setText("")
                } else {
                    val buildInFilters = resources.getStringArray(R.array.spinner_filter)
                    val keyword = if (position >= 0 && position < buildInFilters.size) {
                        buildInFilters[position]
                    } else {
                        ""
                    }
                    binding.etFilter.setText(keyword)
                }
            }
        }

        binding.clear.setOnClickListener {
            LogViewManager.clearAll()
        }

        binding.cbAutoScroll.setOnCheckedChangeListener { buttonView, isChecked ->
            if (binding.cbAutoScroll.isChecked) {
                binding.lvLogs.transcriptMode = AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
            } else {
                binding.lvLogs.transcriptMode = AbsListView.TRANSCRIPT_MODE_NORMAL
            }
        }
    }

    fun destroy() {
        LogViewManager.mLogSetChangedListener = null
    }

    inner class LogAdapter : BaseAdapter() {

        private val dateFormat = SimpleDateFormat("yyyy-MM-ss kk:mm:ss", Locale.CHINA)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.floating_log_view_item, parent, false)

            var holder: Holder? = view.tag as Holder?
            if (holder == null) {
                holder = Holder()
                holder.logTitle = view.findViewById(R.id.log_title)
                holder.logContent = view.findViewById(R.id.log_content)
                view.tag = holder
            }

            val log = getItem(position) as LogInfo
            holder.logTitle?.text = "序号:${log.position}    ${dateFormat.format(Date(log.timeMills))}    ${log.tag}"
            holder.logContent?.text = "${log.content}"

            return view
        }

        override fun getItem(position: Int): Any {

            return if (position >= 0 && position < LogViewManager.FILTER_LOGS.size) LogViewManager.FILTER_LOGS[position] else LogInfo()
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return LogViewManager.FILTER_LOGS.size
        }

    }

    inner class Holder {
        var logTitle: TextView? = null
        var logContent: TextView? = null
    }

}


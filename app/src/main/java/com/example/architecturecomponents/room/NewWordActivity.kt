package com.example.architecturecomponents.room


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.architecturecomponents.databinding.ActivityNewWordBinding

/**
 * Crete by dumingwei on 2020-03-12
 * Desc: 添加单词界面
 *
 */
class NewWordActivity : AppCompatActivity() {

    companion object {

        val EXTRA_REPLY = "EXTRA_REPLY"

        fun launchForResult(activity: Activity, requestCode: Int) {
            val intent = Intent(activity, NewWordActivity::class.java)
            activity.startActivityForResult(intent, requestCode)
        }

    }

    private lateinit var binding: ActivityNewWordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewWordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnSave.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(binding.etWord.text)) {
                Toast.makeText(this, "please input word", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val word = binding.etWord.text.toString()
            replyIntent.putExtra(EXTRA_REPLY, word)
            setResult(Activity.RESULT_OK, replyIntent)
            finish()
        }

    }


}

package com.example.architecturecomponents.room


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.architecturecomponents.R
import com.example.architecturecomponents.databinding.FragmentCodeLabRoomExampleBinding
import com.example.architecturecomponents.room.entities.Word

/**
 * Crete by dumingwei on 2020-03-11
 * Desc: CodeLab上的room的例子
 * https://codelabs.developers.google.com/codelabs/android-room-with-a-view-kotlin/index.html?index=..%2F..index#0
 *
 */
class CodeLabRoomExampleFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = CodeLabRoomExampleFragment()
    }

    private val TAG = "CodeLabRoomExampleFragm"

    private val newWordActivityRequestCode = 1
    private lateinit var wordViewModel: WordViewModel

    private lateinit var adapter: WordListAdapter


    private lateinit var binding: FragmentCodeLabRoomExampleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context?.let { ctx ->
            adapter = WordListAdapter(ctx)

            //wordViewModel = ViewModelProvider(this).get(WordViewModel::class.java)
            wordViewModel = ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
            ).get(WordViewModel::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCodeLabRoomExampleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let {
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(it)
        }

        wordViewModel.allWords.observe(this, Observer { words ->
            Log.d(TAG, "onCreate: observe ${words}")
            //数据发生变化的时候，就会回调到这里
            words?.let {

                adapter.words = it
            }
        })

        binding.fab.setOnClickListener {
            val intent = Intent(activity, NewWordActivity::class.java)
            startActivityForResult(intent, newWordActivityRequestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: ")
        if (requestCode == newWordActivityRequestCode && resultCode == Activity.RESULT_OK) {

            data?.getStringExtra(NewWordActivity.EXTRA_REPLY)?.let {
                val word = Word(it)
                wordViewModel.insert(word)
            }
        } else {
            Toast.makeText(
                context,
                R.string.empty_not_saved,
                Toast.LENGTH_LONG
            ).show()
        }
    }

}

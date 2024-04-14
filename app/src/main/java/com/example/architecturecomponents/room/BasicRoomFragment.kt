package com.example.architecturecomponents.room

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.example.architecturecomponents.R
import com.example.architecturecomponents.room.dao.PlaylistDao
import com.example.architecturecomponents.room.dao.WordDao
import com.example.architecturecomponents.room.entities.*
import kotlinx.android.synthetic.main.fragment_baisc_room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.random.Random.Default.nextDouble

/**
 * Crete by dumingwei on 2020-03-12
 * Desc: Basic room use
 */
class BasicRoomFragment : Fragment(), View.OnClickListener, CoroutineScope by MainScope() {

    private var fruitViewModel: FruitViewModel? = null

    lateinit var wordDAO: WordDao

    lateinit var playlistDao: PlaylistDao

    lateinit var allWords: LiveData<List<Word>>

    lateinit var playlist: LiveData<List<Playlist>>

    var index = 0

    var playlistId = 0L

    companion object {
        private const val TAG = "BasicRoomFragment"
        fun newInstance(): BasicRoomFragment {
            val fragment = BasicRoomFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fruitViewModel = ViewModelProvider(this,
                AndroidViewModelFactory(activity!!.application)
        ).get(FruitViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_baisc_room, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeFruits()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wordDAO = WordRoomDatabase.getDatabase(context!!, this).wordDao()
        allWords = wordDAO.getAlphabetizedWords()
        allWords.observe(this, Observer { list ->

            //index = list.size
            Log.d(TAG, "onViewCreated: index = $index")
            list.forEach {
                Log.d(TAG, "onViewCreated: ${it.word},${it.name}")
            }
            // do nothing
        })

        playlistDao = WordRoomDatabase.getDatabase(context!!, this).playlistDao()

        playlist = playlistDao.getList()

        playlist.observe(this, Observer { list ->
            list.forEach {
                playlistId = it.playlistId
                Log.d(TAG, "playList: $it")
            }
        })
        btn_insert.setOnClickListener(this)
        btn_delete.setOnClickListener(this)
        btn_update.setOnClickListener(this)
        btn_query.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_insert -> {
                insertWord()
                //insertFruit()
                //insertAddress()
                insertPlaylist()
            }
            R.id.btn_query -> {
                //query();
                //queryFruit()
                queryWord()
            }

            R.id.btn_update -> {
                //update()
                //updateWord()
                updatePlayList()
            }
            R.id.btn_delete -> {
                //delete()
                //
                // deleteWord()
                deletePlaylist()
            }
            else -> {
            }
        }
    }

    private fun updatePlayList() {
        var tempIndex = playlistId
        launch {
            val nameAndDescription = NameAndDescription(playlistId, "dmw", "小伙子可以${tempIndex}")
            playlistDao.updatePartial(nameAndDescription)
        }
    }

    private fun insertPlaylist() {
        launch {
            val nameAndDescription = NameAndDescription(index.toLong(), "dmw", "小伙子可以")
            playlistDao.insertPartial(nameAndDescription)
            index++
        }
    }

    private fun deletePlaylist() {
        launch {
            val nameAndDescription = NameAndDescription(playlistId, "dmw", "小伙子可以")
            playlistDao.deletePartial(nameAndDescription)
        }
    }

    private fun insertWord() {
        launch {
            try {
                val word = Word("Hi$index", "dmw")
                wordDAO.insert(word)
                index++

            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(TAG, "insertWord: ${e.message}")
            }
        }
    }

    private fun deleteWord() {
        launch {
            val word1 = Word("Hi0", "dmw")
            val word2 = Word("Hi1", "dmw")
            val row = wordDAO.delete(word1, word2)

            Log.d(TAG, "deleteWord: row = $row")

        }
    }

    private fun updateWord() {
        launch {
            val word1 = Word("Hi0", "dmw${System.currentTimeMillis()}")
            val word2 = Word("Hi1", "dmw${System.currentTimeMillis()}")
            val row = wordDAO.update(word1, word2)
            Log.d(TAG, "updateWord: row = $row")
        }
    }

    private fun queryWord() {
        val allWords = wordDAO.getAlphabetizedWords()
        allWords.observe(this, Observer { list ->
            list.forEach {
                Log.d(TAG, "queryWord: ${it.word},${it.name}")
            }
        })
    }

    private fun insertAddress() {
        com.example.architecturecomponents.App.getExecutors().diskIO().execute {
            val address = com.example.architecturecomponents.room.entities.Address(
                "国泰路",
                "杨浦区",
                "上海",
                21610
            )
            val user = com.example.architecturecomponents.room.entities.User(
                "hongmin",
                "du",
                address
            )
            user.job = "android develop"
            val id = com.example.architecturecomponents.App.getDataBase().userDao().insertUser(user)
            Log.d(TAG, "insert: id= $id")
        }
    }

    private fun delete() {
        com.example.architecturecomponents.App.getExecutors().diskIO().execute {
            val address = com.example.architecturecomponents.room.entities.Address(
                "国泰路",
                "徐汇区",
                "上海",
                21110
            )
            val user = com.example.architecturecomponents.room.entities.User(
                "hongmin",
                "du",
                address
            )
            user.id = 4
            val rowAffected = com.example.architecturecomponents.App.getDataBase().userDao().deleteUsers(user)
            Log.d(TAG, "run: rowAffected=$rowAffected")
        }
    }

    /**
     * 在Java中无法使用协程，可以使用线程池来执行数据库操作
     */
    private fun update() {
        com.example.architecturecomponents.App.getExecutors().diskIO().execute {
            val address = com.example.architecturecomponents.room.entities.Address(
                "国泰路",
                "徐汇区",
                "上海",
                21110
            )
            val user = com.example.architecturecomponents.room.entities.User(
                "dumingwei",
                "du",
                address
            )
            user.id = 1
            val rowAffected = com.example.architecturecomponents.App.getDataBase().userDao().updateUsers(user)
            Log.d(TAG, "run: rowAffected=$rowAffected")
        }
    }

    @SuppressLint("CheckResult")
    private fun query() {
        /* App.getExecutors().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<NameTuple> nameTuples = App.getDataBase().userDao().loadFullName();
                for (NameTuple nameTuple : nameTuples) {
                    Log.d(TAG, "run: " + nameTuple.toString());
                }
            }
        });*/

        /*App.getDataBase().userDao().findUserMaybeWithId(1L)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<User>() {
                    @Override
                    public void accept(User user) throws Exception {
                        Log.d(TAG, "accept: " + user.toString());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "accept: " + throwable.getMessage());
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG, "run: complete");
                    }
                });*/

        /*App.getDataBase().userDao().getUserById(1L)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<User>() {
                    @Override
                    public void accept(User user) throws Exception {
                        Log.d(TAG, "accept: " + user.toString());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "accept: " + throwable.getMessage());
                    }
                });*/
    }

    private fun insertFruit() {
        val fruit = com.example.architecturecomponents.room.entities.Fruit(
            "苹果",
            nextDouble(1.0, 10.0)
        )
        fruitViewModel!!.insert(fruit)
    }

    private fun queryFruit() {
        fruitViewModel!!.query(1L).observe(this, Observer { fruit -> Log.d(TAG, "queryFruit: " + fruit?.name) })
    }

    private fun observeFruits() {
        fruitViewModel!!.fruits.observe(this, Observer { fruits ->
            for (fruit in fruits) {
                Log.d(TAG, "onChanged: $fruit")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

}
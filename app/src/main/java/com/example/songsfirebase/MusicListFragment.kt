package com.example.songsfirebase

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.songsfirebase.databinding.FragmentListmusicBinding
import com.example.songsfirebase.recyclerview.MusicListAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList


class MusicListFragment(var fonticon: Typeface, var list: ArrayList<Song>, var list1: ArrayList<Song>) : Fragment(R.layout.fragment_listmusic), MusicListAdapter.OnItemClickListener  {

    private var layoutManager: RecyclerView.LayoutManager?=null
    var adapter: MusicListAdapter? =null
var checkran=false
    var listmusic=ArrayList<Song>()
    var speech: String=""
    var curnt:Int=0
    lateinit var layout: LinearLayout
    var play=false
    var m:Int=0
    var isBound = false
    var mService: MusicService? =null
    lateinit var binding: FragmentListmusicBinding
    var testplay=false


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding= FragmentListmusicBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val intent = Intent(context, MusicService::class.java)
        activity?.bindService(intent, myConnection, Context.BIND_AUTO_CREATE)
        binding.hidden.typeface=fonticon
        binding.speech.typeface=fonticon
        binding.playBtn.typeface=fonticon
        binding.nextBtn.typeface=fonticon
        binding.preBtn.typeface=fonticon
        binding.download.typeface=fonticon
        listmusic.clear()
        listmusic.addAll(list)
        listmusic.addAll(list1)
        recycler(listmusic)


        binding.randome.typeface=fonticon
        binding.randome.setOnClickListener {
            if(checkran){
                checkran=false
            }
            else{
                checkran=true
            }
        }
    binding.seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                      mService!!.media.seekTo(progress)

                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            }
    )






        Thread {
            while (true) {
//                runOnUiThread {
                if (mService != null) {
                    if (mService!!.check.equals("next")) {
                        mService!!.check = ""
                        runOnUiThread {
                            nextplay()
                        }

                    }
                    if (mService!!.check.equals("play")) {
                        testplay = true
                        mService!!.media.start()
                        mService!!.notifi(listmusic[m].title, "")
                    }
                }
                while (testplay) {
                    try {
                        if (mService != null) {
                            if (mService!!.check.equals("next")) {
                                mService!!.check = ""
                                runOnUiThread {
                                    nextplay()
                                }
                            }
                            if (mService!!.check.equals("stop")) {
                                testplay = false
                                mService!!.media.pause()
                                mService!!.notifi(listmusic[m].title, "")
                            }
                            runOnUiThread {
                                binding.seekBar.progress = mService!!.media.currentPosition
                                val min: Int = mService!!.media.currentPosition / 1000 / 60
                                val sec: Int = mService!!.media.currentPosition / 1000 % 60
                                if (sec < 10) {
                                    binding.progress.text = "" + min + ":0" + sec
                                } else {
                                    binding.progress.text = "" + min + ":" + sec
                                }
                            }
                        }





                        if (mService != null) {

                            if (mService!!.media.isLooping == false) {
                                if (mService!!.media.isPlaying) {
                                    play = false
                                }
                                if (mService!!.media.isPlaying == false) {
                                    runOnUiThread {
                                        binding.playBtn.text = "\uF04B"
                                    }
                                    if (play == false) {

                                        if (mService!!.media.duration - mService!!.media.currentPosition < 1000 && mService!!.media.duration > 0) {
                                            play = true
                                            runOnUiThread {
                                                nextplay()
                                            }
                                        }
                                    }
                                } else {
                                    runOnUiThread {
                                        binding.playBtn.text = "\uF04C"
                                    }
                                }
                            }


                        }
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                    }
                }
            }
//            }
        }.start()


     binding.loopBtn.typeface=fonticon
        binding.loopBtn.setOnClickListener {
            if( mService!!.media.isLooping==false){
                mService!!.media.isLooping=true
                binding.loopBtn.setTextColor(Color.BLUE)
            }
            else{
                mService!!.media.isLooping=false
                binding.loopBtn.setTextColor(Color.BLACK)
            }

        }



        binding.hidden.setOnClickListener { view->
            binding.musiclayout.visibility=View.INVISIBLE
            binding.speech.visibility=View.VISIBLE
        }

        binding.speech.setOnClickListener {
            askSpeechInput()
        }

        binding.search.setOnSearchClickListener {
               binding.jcplay.visibility=View.INVISIBLE
           }

        //search text
        binding.search.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener, android.widget.SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        binding.search.clearFocus()
                        listmusic.clear()


                        for(i in 0..list1.size-1){
                            var name = list1[i].title.toLowerCase()
                            var author =list1[i].author.toLowerCase()
                            if (name.contains(query.toString().toLowerCase()) or author.contains(query.toString().toLowerCase())) {
                                listmusic.add(list1[i])
                            }

                            recycler(listmusic)
                            if(listmusic.size==0){
                                var text=  Toast.makeText(activity ,"Không có kết quả tìm kiếm"+listmusic.size,Toast.LENGTH_LONG)

                            }
                        }



                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        return false
                    }

                },
        )
      binding.jcplay.setOnClickListener {
          binding.musiclayout.visibility=View.VISIBLE
          binding.speech.visibility=View.INVISIBLE
      }

        binding.share.typeface=fonticon


        //share mp3
        binding.share.setOnClickListener {
            val shareintent= Intent()
            shareintent.action=Intent.ACTION_SEND
            shareintent.type="text/plain"
            shareintent.putExtra(Intent.EXTRA_TEXT,listmusic[m].uri)
            shareintent.putExtra(Intent.EXTRA_SUBJECT,"Subject here")
            startActivity(Intent.createChooser(shareintent,"Share"))
        }

        //download mp3
    binding.download.setOnClickListener {
        var myDownload:Long=0
        var request = DownloadManager.Request(
                Uri.parse(listmusic[m].uri))
            .setTitle(listmusic[m].title)
            .setDescription("Downloading...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverMetered(true)
    request.allowScanningByMediaScanner()

    var dm =requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    myDownload = dm.enqueue(request)
    var br = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if(id==myDownload){
                Toast.makeText(context, "DOWNLOAD", Toast.LENGTH_LONG).show()
            }
        }
    }
  requireActivity().registerReceiver(br, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
}

     binding.playBtn.setOnClickListener {
         if(mService!!.mediaoff!=null || mService!!.mediaoff.isPlaying)
         {
             mService!!.mediaoff.pause()
         }
            if(mService!!.media.isPlaying==false){
                 testplay=true
                 binding.playBtn.text="\uF04C"
                 mService!!.media.start()
                chanselect(m)
                chanselect(m)
                mService!!.notifi(listmusic[m].title,"")
        }
         else{
                testplay=false
                binding.playBtn.text="\uF04B"
                mService!!.media.pause()
                chanselect(-1)
                chanselect(-1)
         }
              mService!!.notifi(listmusic[m].title,"")
     }



        binding.nextBtn.setOnClickListener {

            nextplay()
        }
        binding.preBtn.setOnClickListener {

            preplay()

        }

    }
//search giong nói
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==300 && resultCode== Activity.RESULT_OK){
            val result:ArrayList<String>?=data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            speech=result?.get(0).toString().toLowerCase()
            listmusic.clear()



                    for(i in 0..list1.size-1){
                        var name = list1[i].title.toLowerCase()
                        var author =list1[i].author.toLowerCase()
                        if (name.contains(speech.toString().toLowerCase()) or author.contains(speech.toString().toLowerCase())) {
                            listmusic.add(list1[i])
                        }

                        recycler(listmusic)
                        if(listmusic.size==0){
                            var text=  Toast.makeText(activity ,"Không có kết quả tìm kiếm"+listmusic.size,Toast.LENGTH_LONG)

                        }
                    }
                }


        }


    private val myConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            val binder = service as MusicService.MyLocalBinder
            mService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }

    fun recycler(listsong: ArrayList<Song>){
        adapter= MusicListAdapter(fonticon,listsong, this, R.layout.musicitem)
        layoutManager= LinearLayoutManager(activity)
        binding.recyclerView.layoutManager=layoutManager
        binding.recyclerView.setAdapter(adapter)
    }

    fun nextplay(){
        play=true
        if(checkran){
            m=(0..listmusic.size-1).random()
        }
        else{
            if(m>=listmusic.size-1){
                m=0
            }
            else{
                m++
            }
        }


  musicPlay(m)

}

    fun preplay(){
        if(m==0){
            m=listmusic.size-1
        }
        else{
            m--
        }

        musicPlay(m)
    }

    override fun onItemClick(position: Int) {
        play=true
        binding.musiclayout.visibility=View.VISIBLE
        binding.speech.visibility=View.INVISIBLE
        m=position
        musicPlay(position)
    }

    override fun playrecl(position: Int) {
        play=true
        m=position
        musicPlay(position)
    }
fun musicPlay(position: Int){

    mService!!.media.pause()
    binding.jcplay.visibility=View.VISIBLE
        Picasso.get().load(listmusic[position].image).into(binding.anhnen1)
        binding.loi2.text=listmusic[position].lyrics
    binding.jcplay.visibility=View.VISIBLE
    chanselect(position)
    binding.seekBar.progress=0
    binding.title.text=listmusic[position].title
    binding.playBtn.text="\uF04B"
mService!!.stop()
    mService!!.media= MediaPlayer()
    Log.d("linkbaihat",""+listmusic[position].uri)
        mService!!.media= MediaPlayer.create(requireContext(), Uri.parse(listmusic[position].uri))
        mService!!.media.start()

    mService!!.notifi(listmusic[position].title,"")
    binding.barmusic.setAudioSessionId(mService!!.media.audioSessionId)
    binding.progress.text="0:00"
    binding.seekBar.max= mService!!.media.duration
    var min =  mService!!.media.duration / 1000 / 60
    var sec =  mService!!.media.duration / 1000 % 60
    binding.dura.text=""+ min + ":"+sec
    mService!!.media.start()
    testplay=true

}

    //search bằng giọng nói
    fun askSpeechInput(){
        if (!SpeechRecognizer.isRecognitionAvailable(activity)){
            Toast.makeText(activity, "speech", Toast.LENGTH_LONG).show()
        } else {
            val i= Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say som thing")
            startActivityForResult(i, 300)

        }
    }

    fun Fragment?.runOnUiThread(action: () -> Unit) {
        this ?: return
        if (!isAdded)
            return
        activity?.runOnUiThread(action)
    }


fun chanselect(index:Int){
    adapter?.notifyItemChanged(adapter!!.getposition())
    curnt=index
    adapter?.selectposition(curnt)
 adapter?.notifyItemChanged(curnt)
}
  override  fun addSong(position: Int){
        var db:DBHelper= DBHelper(requireActivity())
        val song=Song(listmusic[position].title,listmusic[position].uri,listmusic[position].image,listmusic[position].author,listmusic[position].lyrics)
        db.addSong(song)
    }
}





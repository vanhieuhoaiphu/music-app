package com.example.songsfirebase

import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import android.os.*
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.songsfirebase.recyclerview.ListYeuThichAdapter
import com.example.songsfirebase.recyclerview.MusicListAdapter

import com.gauravk.audiovisualizer.visualizer.BarVisualizer
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class Listyeuthich: AppCompatActivity(), ListYeuThichAdapter.OnItemClickListener {
   lateinit var db:DBHelper

    private var layoutManager: RecyclerView.LayoutManager?=null
    var adapter: ListYeuThichAdapter? =null

   lateinit var barmusic:BarVisualizer
          lateinit var dura:TextView
          lateinit var title:TextView
    lateinit var playBtn:Button
     lateinit var nextBtn:Button
    lateinit var preBtn:Button
    lateinit var seekBar:SeekBar
    lateinit var progress:TextView
    lateinit var recycle:RecyclerView
    lateinit var loopBtn:Button
    lateinit var musiclayout:LinearLayout
lateinit var randome:Button
var checkrandome=false

    var listmusic= ArrayList<Song>()

    var curnt:Int=0
    lateinit var layout: LinearLayout
    var play=false
    var m:Int=0
    var isBound = false
    var mService: MusicService? =null

    var testplay=false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.listyeuthich)
        val fonticon = Typeface.createFromAsset(assets, "fontawesome-webfont.ttf")
        val intent = Intent(this, MusicService::class.java)
      bindService(intent, myConnection, Context.BIND_AUTO_CREATE)

       playBtn=findViewById(R.id.playBtn)
        nextBtn=findViewById(R.id.nextBtn)
        preBtn=findViewById(R.id.preBtn)
         seekBar=findViewById(R.id.seekBar)
     progress=findViewById(R.id.progress)
        loopBtn=findViewById(R.id.loopBtn)


        dura=findViewById(R.id.dura)
        title=findViewById(R.id.title)
       recycle=findViewById(R.id.recyclerView)
randome=findViewById(R.id.randome)
randome.typeface=fonticon
        playBtn.typeface=fonticon
       nextBtn.typeface=fonticon
       preBtn.typeface=fonticon
              var back:Button=findViewById(R.id.back)
        back.typeface=fonticon
        back.setOnClickListener {
            onBackPressed()
        }
        db = DBHelper(this)

        val emp: List<Song> = db.viewEmployee()

        listmusic.addAll(emp)
      adapter= ListYeuThichAdapter(fonticon,listmusic,this,R.layout.item_yeuthich)
        var layoutManager= LinearLayoutManager(this)
        recycle.layoutManager=layoutManager
        recycle.adapter=adapter

randome.setOnClickListener {
    if(checkrandome){
        checkrandome=false
    }
    else{
        checkrandome=true
    }

}


      seekBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            mService!!.medialist.seekTo(progress)

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
                while (testplay){
                    try {


                            if(mService!=null){
                                if (mService!!.check.equals("nextlist")){
                                    runOnUiThread {

                                      nextplay()
                                    }
                                    mService!!.check=""
                            }
                                if (mService!!.check.equals("playlist")){

                                    mService!!.check=""

                                        mService!!.notifi(listmusic[m].title,"list")
                                         mService!!.medialist.start()

                                }
                                if (mService!!.check.equals("stoplist")){


                                        mService!!.notifi(listmusic[m].title,"list")
                                        mService!!.medialist.pause()


                                    mService!!.check=""
                                }
                        }


                        if (mService?.medialist != null) {
                            runOnUiThread {
                                var min: Int = mService!!.medialist.currentPosition.toInt() / 1000 / 60
                                var sec: Int = mService!!.medialist.currentPosition.toInt() / 1000 % 60
                                seekBar.progress=mService!!.medialist.currentPosition
                                if (sec < 10) {
                                    progress.text = "" + min + ":0" + sec
                                } else {
                                    progress.text = "" + min + ":" + sec
                                }
                            }
                        }
                        if(mService!=null){

                            if(mService!!.medialist.isLooping==false){
                                if(mService!!.medialist.isPlaying){
                                    play=false
                                }
                                if(mService!!.medialist.isPlaying==false){
                                    runOnUiThread {
                                        playBtn.text = "\uF04B"
                                    }
                                    if(play==false){

                                        if(mService!!.medialist.duration-mService!!.medialist.currentPosition<1000 && mService!!.medialist.duration>0)
                                        {
                                            play=true
                                            runOnUiThread {
                                                nextplay()
                                            }
                                        }

                                    }

                                }
                                else{
                                    runOnUiThread {
                                        playBtn.text = "\uF04C"
                                    }
                                }
                            }


                        }
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                    }
                }
            }
        }.start()


      loopBtn.typeface=fonticon
        loopBtn.setOnClickListener {
            if( mService!!.medialist.isLooping==false){
                mService!!.medialist.isLooping=true
              loopBtn.setTextColor(Color.BLUE)
            }
            else{
                mService!!.medialist.isLooping=false
             loopBtn.setTextColor(Color.BLACK)
            }

        }



        playBtn.setOnClickListener {
            if(mService!!.mediaoff!=null || mService!!.mediaoff.isPlaying)
            {
                mService!!.mediaoff.pause()
            }
            if(mService!!.medialist.isPlaying==false){
                testplay=true
             playBtn.text="\uF04C"
                mService!!.medialist.start()
                chanselect(m)
                chanselect(m)
            }
            else{
                testplay=false
         playBtn.text="\uF04B"
                mService!!.medialist.pause()
                chanselect(-1)
                chanselect(-1)
            }
            mService!!.notifi(listmusic[m].title,"list")

        }



     nextBtn.setOnClickListener {
            play=true
            nextplay()
        }
     preBtn.setOnClickListener {
            play=true
            preplay()
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



    fun nextplay(){
        play=true
        if(checkrandome==true){
            m= Random.nextInt(0,listmusic.size-1)
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
//        Picasso.get().load(list[position].image).into(binding.anhnen1)
        var jc:LinearLayout=findViewById(R.id.jcplay)
        jc.visibility= View.VISIBLE
        play=true
        m=position
        musicPlay(position)
    }


    fun musicPlay(position: Int){

        chanselect(position)
       seekBar.progress=0
     title.text=listmusic[position].title
       playBtn.text="\uF04B"


      mService!!.playmusic(listmusic[position].title,listmusic[position].uri,4)
        mService!!.notifi(listmusic[position].title,"list")

        progress.text="0:00"
        seekBar.max= mService!!.medialist.duration
        var min =  mService!!.medialist.duration / 1000 / 60
        var sec =  mService!!.medialist.duration / 1000 % 60
        dura.text=""+ min + ":"+sec
        testplay=true


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

        var db:DBHelper= DBHelper(this)
        val song=Song(listmusic[position].title,listmusic[position].uri,listmusic[position].image,listmusic[position].author,listmusic[position].lyrics)
        db.deleteData(song)
        val emp: List<Song> = db.viewEmployee()
        listmusic.clear()
        listmusic.addAll(emp)
        val fonticon = Typeface.createFromAsset(assets, "fontawesome-webfont.ttf")
        adapter= ListYeuThichAdapter(fonticon,listmusic,this,R.layout.item_yeuthich)
        var layoutManager= LinearLayoutManager(this)
        recycle.layoutManager=layoutManager
        recycle.setAdapter(adapter)

    }
}



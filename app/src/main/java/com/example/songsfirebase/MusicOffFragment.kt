package com.example.songsfirebase

import android.content.*
import android.graphics.Color
import android.graphics.Typeface

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.LinearLayout

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView



import kotlin.collections.ArrayList

import android.os.*
import android.util.Log

import android.widget.SeekBar

import com.example.songsfirebase.databinding.FragmentMusicoffBinding
import com.example.songsfirebase.recyclerview.MusicListAdapter
import kotlin.random.Random


class MusicOffFragment(var fonticon: Typeface, var list: ArrayList<Song>) : Fragment(R.layout.fragment_musicoff), MusicListAdapter.OnItemClickListener  {

    private var layoutManager: RecyclerView.LayoutManager?=null
    var adapter: MusicListAdapter? =null




   var checkran=false
    var curnt:Int=0
    lateinit var layout: LinearLayout
    var playoff=false
    var m:Int=0
    var isBound = false
    var mService: MusicService? =null
    lateinit var binding: FragmentMusicoffBinding
    var testplay=false


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding= FragmentMusicoffBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val intent = Intent(context, MusicService::class.java)
        activity?.bindService(intent, myConnection, Context.BIND_AUTO_CREATE)

        binding.randome.typeface=fonticon
        binding.playBtn.typeface=fonticon
        binding.nextBtn.typeface=fonticon
        binding.preBtn.typeface=fonticon


        binding.randome.setOnClickListener {
            if(checkran){
                checkran=false
            }
            else{
                checkran=true
            }
        }

        adapter= MusicListAdapter(fonticon,list, this, R.layout.musicitem)
        layoutManager= LinearLayoutManager(activity)
        binding.recyclerViewoff.layoutManager=layoutManager
        binding.recyclerViewoff.setAdapter(adapter)



        binding.seekBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            mService!!.mediaoff.seekTo(progress)

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
                                if (mService!!.check.equals("nextoff")){
                                    mService!!.check=""
                                    runOnUiThread {
                                        nextplay()
                                    }
                                }
                            if(mService!!.check.equals("playoff")){

                                mService!!.mediaoff.start()
                                mService!!.notifi(list[m].title,"off")
                                mService!!.check=""
                            }
                            if(mService!!.check.equals("stopoff")){
                                mService!!.mediaoff.pause()
                                mService!!.notifi(list[m].title,"off")
                                mService!!.check=""

                            }
                            }



                        if (mService?.mediaoff?.currentPosition != null) {
                            runOnUiThread {
                                binding.seekBar.progress = mService!!.mediaoff.currentPosition
                                var min: Int = mService!!.mediaoff.currentPosition.toInt() / 1000 / 60
                                var sec: Int = mService!!.mediaoff.currentPosition.toInt() / 1000 % 60
                                if (sec < 10) {
                                    binding.progress.text = "" + min + ":0" + sec
                                } else {
                                    binding.progress.text = "" + min + ":" + sec
                                }
                            }

                        }
                        if(mService!=null){

                            if(mService!!.mediaoff.isLooping==false){
                                if(mService!!.mediaoff.isPlaying){
                                    playoff=false
                                }
                                if(mService!!.mediaoff.isPlaying==false){
                                    runOnUiThread {
                                        binding.playBtn.text = "\uF04B"
                                    }
                                    if(playoff==false){

                                        if(mService!!.mediaoff.duration-mService!!.mediaoff.currentPosition<1000 && mService!!.mediaoff.duration>0)
                                        {
                                            playoff=true
                                            runOnUiThread {
                                                nextplay()
                                            }

                                        }

                                    }

                                }
                                else{
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
        }.start()


        binding.loopBtn.typeface=fonticon
        binding.loopBtn.setOnClickListener {
            if( mService!!.mediaoff.isLooping==false){
                mService!!.mediaoff.isLooping=true
                binding.loopBtn.setTextColor(Color.BLUE)
            }
            else{
                mService!!.mediaoff.isLooping=false
                binding.loopBtn.setTextColor(Color.BLACK)
            }

        }


        binding.playBtn.setOnClickListener {
            if(mService!!.media!=null || mService!!.media.isPlaying)
            {
                mService!!.media.pause()
            }
            if(mService!!.mediaoff.isPlaying==false){
                testplay=true
                binding.playBtn.text="\uF04C"
                mService!!.mediaoff.start()
                chanselect(m)
                chanselect(m)
            }
            else{
                testplay=false
                binding.playBtn.text="\uF04B"
                mService!!.mediaoff.pause()
                chanselect(-1)
                chanselect(-1)
            }
             mService!!.notifi(list[m].title,"off")
        }



        binding.nextBtn.setOnClickListener {
            playoff=true
            nextplay()
        }
        binding.preBtn.setOnClickListener {
            playoff=true
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
        playoff=true
        if(checkran){
            m=  (0..list.size-1).random()
            Log.d("zxc",""+m)
        }
        else{
            if(m==list.size-1){
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
            m=list.size-1
        }
        else{
            m--
        }
        if(mService!!.mediaoff!=null || mService!!.mediaoff.isPlaying)
        {
            mService!!.mediaoff.pause()
        }
        musicPlay(m)

    }

    override fun onItemClick(position: Int) {

        playoff=true

        m=position
        musicPlay(position)

    }

    override fun playrecl(position: Int) {
        playoff=true
        m=position
        musicPlay(position)

    }
    fun musicPlay(position: Int){
        binding.jcplay.visibility=View.VISIBLE
        chanselect(position)
        playoff=true
        binding.jcplay.visibility=View.VISIBLE
        binding.seekBar.progress=0
        binding.title.text=list[position].title
        binding.playBtn.text="\uF04B"
        m=position
        mService!!.playmusic(list[position].title,list[position].uri,3)
        mService!!.notifi(list[position].title,"off")

        mService!!.mediaoff.isLooping=false
        binding.progress.text="0:00"
        binding.seekBar.max= mService!!.mediaoff.duration
        var min =  mService!!.mediaoff.duration / 1000 / 60
        var sec =  mService!!.mediaoff.duration / 1000 % 60
        binding.dura.text=""+ min + ":"+sec
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
        var db:DBHelper= DBHelper(requireActivity())
        val song=Song(list[position].title,list[position].uri,list[position].image,list[position].author,list[position].lyrics)
        db.addSong(song)
    }
}




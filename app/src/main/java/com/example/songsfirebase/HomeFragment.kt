package com.example.songsfirebase

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.util.Log

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.songsfirebase.databinding.FragmentHomeBinding
import com.example.songsfirebase.fragment.adapter.SlideAdapter
import com.example.songsfirebase.recyclerview.RecyclerAdapter
import com.example.songsfirebase.recyclerview.RecyclerVIew2
import okhttp3.*
import java.io.File
import java.io.IOException
import java.util.*


class HomeFragment(var songList: ArrayList<Song>, var songList1: ArrayList<Song>, var fonticon: Typeface) : Fragment(R.layout.fragment_home), RecyclerAdapter.OnItemClickListener , RecyclerVIew2.OnItemClickListener{
    lateinit var binding:FragmentHomeBinding
    var dem:Int=1
    var imagelist= ArrayList<String>()
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
    var m:Int=0
    var isBound = false
    var mService: MusicService? =null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }
    override  fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val intent = Intent(context, MusicService::class.java)
        activity?.bindService(intent, myConnection, Context.BIND_AUTO_CREATE)
        binding.closeaudio.typeface=fonticon

        //slide
        imagelist.add("https://intphcm.com/data/upload/poster-am-nhac-dien-tu.jpg")
        imagelist.add("https://mtrend.vn/wp-content/uploads/2015/09/hinh-nen-am-nhac-cho-dien-thoai-e1443603648990.jpg")
        imagelist.add("https://mtrend.vn/wp-content/uploads/2015/09/hinh-nen-am-nhac-cho-dien-thoai-e1443603648990.jpg")
        binding.viewPager2.adapter= SlideAdapter(imagelist)
        binding.viewPager2.orientation=ViewPager2.ORIENTATION_HORIZONTAL
        binding.indicator.setViewPager(binding.viewPager2)
        val slide=Handler()
        binding.viewPager2.registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                slide.removeCallbacks(getImage)
                slide.postDelayed(getImage,3000)
            }
        })


        var recyclerViewAdapter = RecyclerAdapter(songList, this, R.layout.gird2)
        binding.bxh.layoutManager = GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL, false)
        binding.bxh.adapter = recyclerViewAdapter

        var recyclerViewAdapter1 = RecyclerVIew2(songList1, this, R.layout.grid)
        binding.album.layoutManager=LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        binding.album.adapter = recyclerViewAdapter1


        //đặt giờ tắt
        binding.time.setOnClickListener {
            binding.choosetime.visibility=View.VISIBLE
            binding.numPickerH.maxValue=23
            binding.numPickerH.minValue=0
            binding.numPickerM.minValue=0
            binding.numPickerM.maxValue=59
            binding.close.typeface=fonticon
            binding.close.setOnClickListener {
                binding.choosetime.visibility=View.INVISIBLE
            }
        binding.oke.setOnClickListener {
                binding.choosetime.visibility=View.INVISIBLE
                mService!!.timenum=(binding.numPickerH.value*60+binding.numPickerM.value)*60
                var intent1 = Intent(context, MusicService::class.java)
                intent1.putExtra("time", ""+ mService!!.timenum)
                if (context != null) {
                    ContextCompat.startForegroundService(requireContext(), intent1)
                }

            }
        }


         Thread{
             while (true){
                 try{
                     runOnUiThread {
                         if (mService != null) {
                             if (mService!!.check.equals("nexthome")) {
                                 if (m == songList.size - 1) {
                                     m = 0
                                 } else {
                                     m++
                                 }
                                 mService!!.check = ""
                                 play(m)
                                 mService!!.notifi(songList[m].title,"home")
                             }
                             if(mService!!.check.equals("playhome")){
                                 mService!!.notifi(songList[m].title,"home")
                                 mService!!.mediahome.start()
                                 mService!!.check = ""

                             }
                             if(mService!!.check.equals("stophome")){
                                 mService!!.notifi(songList[m].title,"home")
                                 mService!!.mediahome.pause()
                                 mService!!.check = ""

                             }
                         }
                     }
                Thread.sleep(1000)
                 }catch (e: InterruptedException){

                 }

             }
         }.start()





    binding.search1.setOnClickListener {
    binding.search1.visibility=View.INVISIBLE
    binding.searchaudio.visibility=View.VISIBLE
    var mr=MediaRecorder()
    var result=""
    var path= Environment.getExternalStorageDirectory().toString()+"/audiorecoder1.mp3"

    if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED)
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE),111)

    binding.start.setOnClickListener {
        binding.start.visibility = View.INVISIBLE

        mr = MediaRecorder()
        mr.setAudioSource(MediaRecorder.AudioSource.MIC)
        mr.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
        mr.setOutputFile(path)
        mr.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        mr.prepare()
        mr.start()
        var dem = 0
        Thread {
            while (dem < 5) {
                dem++
                runOnUiThread {
                    binding.resultsearch.text = "..."
                }


                Thread.sleep(500)
                runOnUiThread {
                    binding.resultsearch.text = "......"
                }
                Thread.sleep(500)
            }
runOnUiThread {
    binding.search.visibility = View.VISIBLE
}

        }.start()
    }

    binding.search.setOnClickListener {
        binding.search.visibility=View.INVISIBLE
        mr.stop()
        val client = OkHttpClient()
        var data: RequestBody
        if(dem>=10){
            data = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file","audiorecoder1.mp3", RequestBody.create(
                            MediaType.parse("audio/mpeg") ,
                            File(path)
                    ))
                    .build()
        }
        else{
            data = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file","audiorecoder1.mp3", RequestBody.create(
                            MediaType.parse("audio/mpeg") ,
                            File(path)
                    ))

                    .addFormDataPart("return", "apple_music,spotify")
                    .addFormDataPart("api_token", "test")

                    .build()
        }

        val request = Request.Builder()
                .url("https://api.audd.io/").post(data).build()
        val response: Response? = null
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {

                    result = response.body()!!.string()
                    runOnUiThread {
                        binding.resultsearch.visibility=View.VISIBLE
                        binding.resultsearch.text = result
                        var s="Artist: "
                        if(result.equals("{\"status\":\"success\",\"result\":null}")){
                            binding.resultsearch.text = "Làm ơn thử lại"
                        }
                        else{

                            var m=result!!.indexOf("\"artist\":\"")+10
                            while (result!!.get(m).toString().equals("\"")==false){
                                s+=result!!.get(m).toString()
                                m++
                            }
                            s+="\n Title: "
                            m=result!!.indexOf("title\":\"")+8
                            while (result!![m].toString().equals("\"")==false){
                                s+=result!![m]
                                m++
                            }
                            s+="\n Album: "
                            m=result!!.indexOf("album\":\"")+8
                            while (result!![m].toString().equals("\"")==false){
                                s+=result!![m]
                                m++
                            }
                            binding.resultsearch.text= s
                        }

                    }
                    val delete= File(path)
                    delete.delete()
                }
            }
        })
        binding.start.visibility=View.VISIBLE
        dem++
    }

        binding.closeaudio.setOnClickListener {
            binding.searchaudio.visibility=View.INVISIBLE
            binding.search1.visibility=View.VISIBLE
        }
}

   binding.next.setOnClickListener {
       var int= Intent(requireContext(),Listyeuthich::class.java)
       startActivity(int)
   }
    }
    val getImage= Runnable {
        binding.viewPager2.currentItem=binding.viewPager2.currentItem+1
    }
    override fun onItemClick(position: Int) {
        m=position
        play(position)
    }

    fun play(position: Int){
      mService!!.playmusic(songList[position].title,songList[position].uri,1)
        mService!!.notifi(songList[position].title,"home")
    }

    override fun onItemClick2(position: Int) {

    }
    fun Fragment?.runOnUiThread(action: () -> Unit) {
        this ?: return
        if (!isAdded)
            return
        activity?.runOnUiThread(action)
    }
}









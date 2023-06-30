package com.example.songsfirebase.recyclerview


import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.songsfirebase.R
import com.example.songsfirebase.Song
import com.squareup.picasso.Picasso
import java.util.*


class ListYeuThichAdapter(var fonticon: Typeface, val list: ArrayList<Song>, private val listener: OnItemClickListener, var layout:Int):RecyclerView.Adapter<ListYeuThichAdapter.ViewHolder>() {
    var selectposition:Int = -1
    fun selectposition( selectposition:Int){
        this.selectposition=selectposition
    }
    fun getposition():Int{
        return selectposition
    }
    inner class ViewHolder(itemview:View):RecyclerView.ViewHolder(itemview), View.OnClickListener {
        val title:TextView
        val image:ImageView
        val author:TextView

        val like:Button
        init {

            title=itemview.findViewById(R.id.titlesong)
            image=itemview.findViewById(R.id.imagesong)
            author=itemview.findViewById(R.id.authorsong)

            like=itemview.findViewById(R.id.like)
            itemview.setOnClickListener(this)

        }

        override fun onClick(v: View?) {
            val position=absoluteAdapterPosition
            if(position!=RecyclerView.NO_POSITION){
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)

        fun addSong(position:Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v=LayoutInflater.from(parent.context).inflate(layout,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text=list[position].title

        holder.like.typeface=fonticon
        if(selectposition==position){
            holder.like.visibility=View.INVISIBLE
        }
        else{
            holder.like.visibility=View.VISIBLE
        }
        Picasso.get().load(list[position].image).into(holder.image)
        holder.author.text=list[position].author

        holder.like.setOnClickListener {
            listener.addSong(position)
            holder.like.setTextColor(android.graphics.Color.RED)
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }


}
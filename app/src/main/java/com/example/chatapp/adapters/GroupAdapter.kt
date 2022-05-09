package com.example.chatapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ItemGroupBinding
import com.example.chatapp.models.Group

class GroupAdapter(val list: List<Group>,val listener:OnClickListener) : RecyclerView.Adapter<GroupAdapter.GroupVH>(){

    inner class GroupVH(val itemBinding:ItemGroupBinding):RecyclerView.ViewHolder(itemBinding.root){
        fun onBind(group: Group){
            itemBinding.nameTv.text = group.name
            itemBinding.nameTv.setOnClickListener {
                listener.OnClick(group)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupVH {
        return GroupVH(ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: GroupVH, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list.size

    interface OnClickListener{
        fun OnClick(group: Group)
    }
}
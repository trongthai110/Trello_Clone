package ndtt.trelloclone.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_card.view.*
import ndtt.trelloclone.R
import ndtt.trelloclone.activities.TaskListActivity
import ndtt.trelloclone.models.Card
import ndtt.trelloclone.models.SelectedMembers

open class CardListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Card>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            if (model.labelColor.isNotEmpty()) {
                holder.itemView.viewLabelColor.visibility = View.VISIBLE
                holder.itemView.viewLabelColor
                    .setBackgroundColor(Color.parseColor(model.labelColor))
            } else {
                holder.itemView.viewLabelColor.visibility = View.GONE
            }
            holder.itemView.tvCardName.text = model.name

            if ((context as TaskListActivity)
                    .assignedMemberDetailList.size > 0) {
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

                for (i in context.assignedMemberDetailList.indices) {
                    for (j in model.assignedTo) {
                        if (context.assignedMemberDetailList[i].id == j) {
                            val selectedMembers = SelectedMembers(
                                context.assignedMemberDetailList[i].id,
                                context.assignedMemberDetailList[i].image
                            )
                            selectedMembersList.add(selectedMembers)
                        }
                    }
                }

                if (selectedMembersList.size > 0) {
                    if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy) {
                        holder.itemView.rvCardSelectedMembersList.visibility = View.GONE
                    } else {
                        holder.itemView.rvCardSelectedMembersList.visibility = View.VISIBLE

                        holder.itemView.rvCardSelectedMembersList.layoutManager =
                            GridLayoutManager(context,4)
                        val adapter = CardMemberListItemsAdapter(context, selectedMembersList, false)
                        holder.itemView.rvCardSelectedMembersList.adapter = adapter
                        adapter.setOnClickListener(
                            object: CardMemberListItemsAdapter.OnClickListener {
                                override fun onClick() {
                                    if (onClickListener != null) {
                                        onClickListener!!.onClick(position)
                                    }
                                }
                        })
                    }
                } else {
                    holder.itemView.rvCardSelectedMembersList.visibility = View.GONE
                }
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
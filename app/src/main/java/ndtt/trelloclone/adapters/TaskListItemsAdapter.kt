package ndtt.trelloclone.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_task.view.*
import ndtt.trelloclone.R
import ndtt.trelloclone.activities.TaskListActivity
import ndtt.trelloclone.models.Task
import java.util.Collections

open class TaskListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Task>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var positionDraggedFrom = -1
    private var positionDraggedTo = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_task, parent, false)
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins((15.toDp().toPx()),0,(40.toDp()).toPx(), 0)

        view.layoutParams = layoutParams
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder,
                                  @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]
        if (holder is MyViewHolder) {
            if (position == list.size - 1) {
                holder.itemView.tvAddTaskList.visibility = View.VISIBLE
                holder.itemView.llTaskTtem.visibility = View.GONE
            } else {
                holder.itemView.tvAddTaskList.visibility = View.GONE
                holder.itemView.llTaskTtem.visibility = View.VISIBLE
            }

            holder.itemView.tvTaskListTitle.text = model.title
            holder.itemView.tvAddTaskList.setOnClickListener {
                holder.itemView.tvAddTaskList.visibility = View.GONE
                holder.itemView.cvAddTaskListName.visibility = View.VISIBLE
            }

            holder.itemView.ibCloseListName.setOnClickListener {
                holder.itemView.tvAddTaskList.visibility = View.VISIBLE
                holder.itemView.cvAddTaskListName.visibility = View.GONE
            }

            holder.itemView.ibDoneListName.setOnClickListener {
                val listName = holder.itemView.etTaskListName.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.createTaskList(listName)
                    }
                } else {
                    Toast.makeText(
                        context,"Please Enter List Name",
                        Toast.LENGTH_SHORT).show()
                }
            }

            holder.itemView.ibEditListName.setOnClickListener {
                holder.itemView.etEditTaskListName.setText(model.title)
                holder.itemView.llTitleView.visibility = View.GONE
                holder.itemView.cvEditTaskListName.visibility = View.VISIBLE
            }

            holder.itemView.ibCloseEditableView.setOnClickListener {
                holder.itemView.llTitleView.visibility = View.VISIBLE
                holder.itemView.cvEditTaskListName.visibility = View.GONE
            }

            holder.itemView.ibDoneEditListName.setOnClickListener {
                val listName = holder.itemView.etEditTaskListName.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.updateTaskList(position, listName, model)
                    }
                } else {
                    Toast.makeText(
                        context,"Please Enter a List Name",
                        Toast.LENGTH_SHORT).show()
                }
            }

            holder.itemView.ibDeleteList.setOnClickListener {
                alertDialogForDeleteList(position, model.title)
            }

            holder.itemView.tvAddCard.setOnClickListener {
                holder.itemView.tvAddCard.visibility = View.GONE
                holder.itemView.cvAddCard.visibility = View.VISIBLE
            }

            holder.itemView.ibCloseCardName.setOnClickListener {
                holder.itemView.tvAddCard.visibility = View.VISIBLE
                holder.itemView.cvAddCard.visibility = View.GONE
            }

            holder.itemView.ibDoneCardName.setOnClickListener {
                val cardName = holder.itemView.etCardName.text.toString()

                if (cardName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.addCardToTaskList(position, cardName)
                    }
                } else {
                    Toast.makeText(
                        context,"Please Enter a Card Name",
                        Toast.LENGTH_SHORT).show()
                }
            }

            holder.itemView.rvCardList.layoutManager =
                LinearLayoutManager(context)

            holder.itemView.rvCardList.setHasFixedSize(true)

            val adapter = CardListItemsAdapter(context, model.cards)
            holder.itemView.rvCardList.adapter = adapter

            adapter.setOnClickListener(
                object: CardListItemsAdapter.OnClickListener {
                    override fun onClick(cardPosition: Int) {

                        if (context is TaskListActivity) {
                            context.cardDetails(position, cardPosition)
                        }
                    }
                }
            )

            val dividerItemDecoration = DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL)
            holder.itemView.rvCardList.addItemDecoration(dividerItemDecoration)

            val helper = ItemTouchHelper(
                object: ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
                ) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        dragged: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        val draggedPosition = dragged.adapterPosition
                        val targetPosition = target.adapterPosition

                        if (positionDraggedFrom == -1) {
                            positionDraggedFrom == draggedPosition
                        }
                        positionDraggedTo = targetPosition
                        Collections.swap(list[position].cards,
                            draggedPosition, targetPosition)

                        adapter.notifyItemMoved(draggedPosition, targetPosition)
                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                    }

                    override fun clearView(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder
                    ) {
                        super.clearView(recyclerView, viewHolder)

                        if (positionDraggedFrom != -1 && positionDraggedTo != 1
                            && positionDraggedFrom != positionDraggedTo) {
                            (context as TaskListActivity).updateCardsInTaskList(
                                position,
                                list[position].cards
                            )
                        }
                        positionDraggedFrom = -1
                        positionDraggedTo = -1
                    }

                }
            )
            helper.attachToRecyclerView(holder.itemView.rvCardList)
        }
    }

    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle("Alert")
        //set message for alert dialog
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed

            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }

        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun Int.toDp(): Int =
        (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

}
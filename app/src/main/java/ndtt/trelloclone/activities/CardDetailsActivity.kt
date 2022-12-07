package ndtt.trelloclone.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_card_details.*
import kotlinx.android.synthetic.main.activity_members.*
import ndtt.trelloclone.R
import ndtt.trelloclone.adapters.CardMemberListItemsAdapter
import ndtt.trelloclone.dialogs.LabelColorListDialog
import ndtt.trelloclone.dialogs.MembersListDialog
import ndtt.trelloclone.firebase.FirestoreClass
import ndtt.trelloclone.models.*
import ndtt.trelloclone.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {

    private lateinit var boardDetails: Board
    private var taskListPosition = - 1
    private var cardPosition = - 1
    private var selectedColor = ""
    private lateinit var membersDetailList: ArrayList<User>
    private var selectedDueDateMilliSeconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntentData()
        setUpActionBar()

        etNameCardDetails.setText(boardDetails
            .taskList[taskListPosition]
            .cards[cardPosition].name)
        etNameCardDetails.setSelection(etNameCardDetails.text.toString().length)

        selectedColor = boardDetails.taskList[taskListPosition].cards[cardPosition].labelColor

        if (selectedColor.isNotEmpty()) {
            setColor()
        }

        btnUpdateCardDetails.setOnClickListener {
            if ( etNameCardDetails.text.toString().isNotEmpty())
                updateCardDetails()
            else {
                Toast.makeText(this,
                "Enter a card name.", Toast.LENGTH_SHORT).show()
            }
        }

        tvSelectLabelColor.setOnClickListener {
            labelColorsListDialog()
        }

        tvSelectMembers.setOnClickListener {
            membersListDialog()
        }

        setupSelectedMembersList()

        selectedDueDateMilliSeconds = boardDetails
            .taskList[taskListPosition]
            .cards[cardPosition].dueDate

        if (selectedDueDateMilliSeconds > 0 ) {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(selectedDueDateMilliSeconds))
            tvSelectDueDate.text = selectedDate
        }

        tvSelectDueDate.setOnClickListener {
            showDataPicker()
        }
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbarCardDetailsActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = boardDetails
                .taskList[taskListPosition]
                .cards[cardPosition].name
        }

        toolbarCardDetailsActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun colorsList(): ArrayList<String> {
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#e71d36")
        colorsList.add("#f35b04")
        colorsList.add("#f7b801")
        colorsList.add("#72b01d")
        colorsList.add("#00a6fb")
        colorsList.add("#6f6866")
        colorsList.add("#6a4c93")
        return colorsList
    }

    private fun setColor() {
        tvSelectLabelColor.text = ""
        tvSelectLabelColor.setBackgroundColor(
            Color.parseColor(selectedColor))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.actionDeleteCard -> {
                alertDialogForDeleteCard(boardDetails
                    .taskList[taskListPosition].cards[cardPosition].name)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            boardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }

        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            taskListPosition = intent.getIntExtra(
                Constants.TASK_LIST_ITEM_POSITION, - 1)
        }

        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            cardPosition = intent.getIntExtra(
                Constants.CARD_LIST_ITEM_POSITION, - 1)
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            membersDetailList = intent.getParcelableArrayListExtra(
                Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle(resources.getString(R.string.alert))
        //set message for alert dialog
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_card,
                cardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
            deleteCard()
        }
        //performing negative action
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }

    private fun membersListDialog() {
        var cardAssignedMembersList = boardDetails
            .taskList[taskListPosition]
            .cards[cardPosition].assignedTo

        if (cardAssignedMembersList.size > 0) {
            for (i in membersDetailList.indices) {
                for (j in cardAssignedMembersList) {
                    if (membersDetailList[i].id == j) {
                        membersDetailList[i].selected = true
                    }
                }
            }
        } else {
            for (i in membersDetailList.indices) {
                membersDetailList[i].selected = false
            }
        }

        val listDialog = object: MembersListDialog(
            this,
            membersDetailList,
            resources.getString(R.string.str_select_member)
        ) {
            override fun onItemSelected(user: User, action: String) {
               if (action == Constants.SELECT) {
                   if (!boardDetails
                           .taskList[taskListPosition]
                           .cards[cardPosition].assignedTo.contains(user.id)) {
                       boardDetails
                           .taskList[taskListPosition]
                           .cards[cardPosition].assignedTo.add(user.id)
                   }
               }
               else {
                   boardDetails
                       .taskList[taskListPosition]
                       .cards[cardPosition].assignedTo.remove(user.id)

                   for (i in membersDetailList.indices) {
                       if (membersDetailList[i].id == user.id) {
                           membersDetailList[i].selected = false
                       }
                   }
               }

                setupSelectedMembersList()
            }

        }
        listDialog.show()
    }

    private fun updateCardDetails() {
        val card = Card(
            etNameCardDetails.text.toString(),
            boardDetails.taskList[taskListPosition]
                .cards[cardPosition].createdBy,
            boardDetails.taskList[taskListPosition]
                .cards[cardPosition].assignedTo,
            selectedColor,
            selectedDueDateMilliSeconds
        )

        val taskList: ArrayList<Task> = boardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        boardDetails.taskList[taskListPosition].cards[cardPosition] = card

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity, boardDetails)
    }

    private fun deleteCard() {
        val cardList: ArrayList<Card> = boardDetails
            .taskList[taskListPosition].cards
        cardList.removeAt(cardPosition)

        val taskList: ArrayList<Task> = boardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        taskList[taskListPosition].cards = cardList

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity, boardDetails)
    }

    private fun labelColorsListDialog() {
        val colorsList: ArrayList<String> = colorsList()

        val listDialog = object: LabelColorListDialog(
            this,
            colorsList,
            resources.getString(R.string.str_select_label_color), selectedColor) {
            override fun onItemSelected(color: String) {
                selectedColor = color
                setColor()
            }
        }

        listDialog.show()
    }

    private fun setupSelectedMembersList() {
        val cardAssignedMemberList = boardDetails
            .taskList[taskListPosition].cards[cardPosition].assignedTo

        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for (i in membersDetailList.indices) {
            for (j in cardAssignedMemberList) {
                if (membersDetailList[i].id == j) {
                    val selectedMember = SelectedMembers(
                        membersDetailList[i].id,
                        membersDetailList[i].image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0) {
            selectedMembersList.add(SelectedMembers("", ""))
            tvSelectMembers.visibility = View.GONE
            rvSelectedMembersList.visibility = View.VISIBLE
            rvSelectedMembersList.layoutManager = GridLayoutManager(
                this, 6
            )
            val adapter = CardMemberListItemsAdapter(
                this, selectedMembersList, true)
            rvSelectedMembersList.adapter = adapter
            adapter.setOnClickListener(
                object: CardMemberListItemsAdapter.OnClickListener {
                    override fun onClick() {
                        membersListDialog()
                    }
                }
            )
        } else {
            tvSelectMembers.visibility = View.VISIBLE
            rvSelectedMembersList.visibility = View.GONE
        }
    }
    private fun showDataPicker() {
        /**
         * This Gets a calendar using the default time zone and locale.
         * The calender returned is based on the current time
         * in the default time zone with the default.
         */
        val c = Calendar.getInstance()
        val year =
            c.get(Calendar.YEAR) // Returns the value of the given calendar field. This indicates YEAR
        val month = c.get(Calendar.MONTH) // This indicates the Month
        val day = c.get(Calendar.DAY_OF_MONTH) // This indicates the Day

        /**
         * Creates a new date picker dialog for the specified date using the parent
         * context's default date picker dialog theme.
         */
        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                /*
                  The listener used to indicate the user has finished selecting a date.
                 Here the selected date is set into format i.e : day/Month/Year
                  And the month is counted in java is 0 to 11 so we need to add +1 so it can be as selected.

                 Here the selected date is set into format i.e : day/Month/Year
                  And the month is counted in java is 0 to 11 so we need to add +1 so it can be as selected.*/

                // Here we have appended 0 if the selected day is smaller than 10 to make it double digit value.
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                // Here we have appended 0 if the selected month is smaller than 10 to make it double digit value.
                val sMonthOfYear =
                    if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                // Selected date it set to the TextView to make it visible to user.
                tvSelectDueDate.text = selectedDate

                /**
                 * Here we have taken an instance of Date Formatter as it will format our
                 * selected date in the format which we pass it as an parameter and Locale.
                 * Here I have passed the format as dd/MM/yyyy.
                 */

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                // The formatter will parse the selected date in to Date object
                // so we can simply get date in to milliseconds.
                val theDate = sdf.parse(selectedDate)

                /** Here we have get the time in milliSeconds from Date object
                 */
                selectedDueDateMilliSeconds = theDate!!.time
            },
            year,
            month,
            day
        )
        dpd.show() // It is used to show the datePicker Dialog.
    }

}
package com.hungama.music.ui.main.viewmodel

import android.app.Application
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hungama.music.data.model.Contact
import com.hungama.music.utils.CommonUtils.setLog
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch

class ContactsViewModel(val mApplication: Application) : AndroidViewModel(mApplication) {

    private val _contactsLiveData = MutableLiveData<ArrayList<Contact>>()
    val contactsLiveData:LiveData<ArrayList<Contact>> = _contactsLiveData

    fun fetchContacts() {
        viewModelScope.launch {
            val contactsListAsync = async { getPhoneContacts() }
            val contactNumbersAsync = async { getContactNumbers() }
            //val contactEmailAsync = async { getContactEmails() }

            val contacts = contactsListAsync.await()
            val contactNumbers = contactNumbersAsync.await()
            //val contactEmails = contactEmailAsync.await()
            contacts.forEach {
                contactNumbers[it.id]?.let { numbers ->
                    it.number = numbers
                }
                /*contactEmails[it.id]?.let { emails ->
                    it.emails = emails
                }*/
                /*val testHashMap = HashMap<String, String>()
                testHashMap["name"] = it.name
                testHashMap["number"] = it.numbers.toString()
                it.contactData = testHashMap*/
            }
            _contactsLiveData.postValue(contacts)
        }
    }

    private suspend fun getPhoneContacts(): ArrayList<Contact> {
        val contactsList = ArrayList<Contact>()
        val contactsCursor = mApplication.contentResolver?.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        if (contactsCursor != null && contactsCursor.count > 0) {
            val idIndex = contactsCursor.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            while (contactsCursor.moveToNext()) {
                val id = contactsCursor.getString(idIndex)
                val name = contactsCursor.getString(nameIndex)
                if (name != null) {
                    contactsList.add(Contact(id, name))
                }
            }
            contactsCursor.close()
        }
        return contactsList
    }

    private suspend fun getContactNumbers(): HashMap<String, ArrayList<String>> {
        val contactsNumberMap = HashMap<String, ArrayList<String>>()
        val phoneCursor: Cursor? = mApplication.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        if (phoneCursor != null && phoneCursor.count > 0) {
            val contactIdIndex = phoneCursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (phoneCursor.moveToNext()) {
                val contactId = phoneCursor.getString(contactIdIndex)
                val number: String = phoneCursor.getString(numberIndex)
                //check if the map contains key or not, if not then create a new array list with number
                if (contactsNumberMap.containsKey(contactId)) {
                    contactsNumberMap[contactId]?.add(number)
                } else {
                    contactsNumberMap[contactId] = arrayListOf(number)
                }
            }
            //contact contains all the number of a particular contact
            phoneCursor.close()
        }
        setLog("ContactData2", contactsNumberMap.toString())
        return contactsNumberMap
    }

}
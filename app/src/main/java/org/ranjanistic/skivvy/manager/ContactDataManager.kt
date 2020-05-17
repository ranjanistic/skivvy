package org.ranjanistic.skivvy.manager

import android.os.Environment
import org.json.JSONArray
import org.json.JSONObject
import org.ranjanistic.skivvy.ContactModel
import org.ranjanistic.skivvy.R
import org.ranjanistic.skivvy.Skivvy
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset

/**
 * Device contacts manager class, to hold the details of contacts
 * on the device for direct supply to Skivvy on demand.
 * @author Priyanshu Ranjan
 */

class ContactDataManager(val skivvy: Skivvy) {

    class Codes {
        val contacts = "contacts"
        val photo = "pid"
        val prefix = "prefix"
        val fname = "fname"
        val midname = "middle"
        val lname = "lname"
        val suffix = "suffix"
        val phoenticFirst = "phoneticF"
        val phoneticMid = "phoneticM"
        val phoneticLast = "phoneticL"
        val nicknameset = "nicknames"
        val nickNameType = "nickType"
        val nickName = "nickname"
        val nicknameFreq = "nickFreq"
        val workTitle = "workTitle"
        val company = "company"
        val addressSet = "addresses"
        val line1 = "line1"
        val line2 = "line2"
        val city = "city"
        val state = "state"
        val country = "country"
        val addressFreq = "addressFreq"
        val phoneSet = "phones"
        val phoneType = "phoneType"
        val phoneNumber = "number"
        val phoneFreq = "phoneFreq"
        val emailSet = "emails"
        val emailType = "emailType"
        val emailId = "email"
        val emailFreq = "emailFreq"
        val dateSet = "dates"
        val dateType = "dateType"
        val date = "date"
        val relationType = "relationtype"
        val relation = "relation"
        val customSet = "customs"
        val customType = "cType"
        val customValue = "cValue"
    }

    private val code = Codes()
    private var totalContacts: Int = 0
    private lateinit var contactID: Array<String?>
    private lateinit var photoID: Array<String?>
    private lateinit var displayName: Array<String?>
    private lateinit var nickName: Array<Array<String?>?>
    private lateinit var phoneList: Array<Array<String?>?>
    private lateinit var emailList: Array<Array<String?>?>

    var userIDs: ArrayList<String> = ArrayList()
    var names: ArrayList<String> = ArrayList()
    var photoIDs: ArrayList<String> = ArrayList()

    var nicknames: ArrayList<ArrayList<String>> = ArrayList()

    data class ContactData(val size: Int) {
        var iDs: Array<String?> = arrayOfNulls(size)
        var photoIDs: Array<String?> = arrayOfNulls(size)
        var names: Array<String?> = arrayOfNulls(size)
        var nickNames: Array<Array<String?>?> = arrayOfNulls(size)
        var phones: Array<Array<String?>?> = arrayOfNulls(size)
        var emails: Array<Array<String?>?> = arrayOfNulls(size)
    }

    fun setTotalContacts(size: Int) {
        this.totalContacts = size
    }

    fun getTotalContacts(): Int {
        return this.totalContacts
    }

    fun setContactDataInitials(
        IDs: Array<String?>,
        pIDs: Array<String?>,
        displayNames: Array<String?>,
        nicknames: Array<Array<String?>?>,
        phones: Array<Array<String?>?>,
        emails: Array<Array<String?>?>
    ) {
        this.contactID = IDs
        this.photoID = pIDs
        this.displayName = displayNames
        this.nickName = nicknames
        this.phoneList = phones
        this.emailList = emails
    }

    fun setContactSoloData(index: Int, ID: String?, photoUri: String?, contactName: String?) {
        this.contactID[index] = ID
        this.photoID[index] = photoUri
        this.displayName[index] = contactName
    }

    fun setContactDetails(contactData: ContactData) {
        this.contactID = contactData.iDs
        this.photoID = contactData.photoIDs
        this.displayName = contactData.names
        this.nickName = contactData.nickNames
        this.phoneList = contactData.phones
        this.emailList = contactData.emails
    }

    fun setContactNicknameInitials(contactIndex: Int, nicknames: Array<String?>) {
        this.nickName[contactIndex] = nicknames
    }

    fun setContactPhonesInitials(contactIndex: Int, phones: Array<String?>) {
        this.phoneList[contactIndex] = phones
    }

    fun setContactEmailsInitials(contactIndex: Int, emails: Array<String?>) {
        this.emailList[contactIndex] = emails
    }

    fun setContactNicknameData(contactIndex: Int, nicknameIndex: Int, nickName: String?) {
        this.nickName[contactIndex]?.set(nicknameIndex, nickName)
    }

    fun setContactPhoneData(contactIndex: Int, phoneIndex: Int, phone: String?) {
        this.phoneList[contactIndex]?.set(phoneIndex, phone)
    }

    fun setContactEmailData(contactIndex: Int, emailIndex: Int, email: String?) {
        this.emailList[contactIndex]?.set(emailIndex, email)
    }

    fun getContactIDs(): Array<String?> {
        return this.contactID
    }

    fun getContactPhotoUris(): Array<String?> {
        return this.photoID
    }

    fun getContactNames(): Array<String?> {
        return this.displayName
    }

    fun getContactNicknames(): Array<Array<String?>?> {
        return this.nickName
    }

    fun getContactEmails(): Array<Array<String?>?> {
        return this.emailList
    }

    fun getContactPhones(): Array<Array<String?>?> {
        return this.phoneList
    }

    val contact: ArrayList<ContactModel> = ArrayList()

    fun saveContactsToJson() {
        val output = FileOutputStream(
            Environment.getExternalStorageDirectory().toString() + "/.Skivvy/" + skivvy.getString(R.string.contact_file_name), true
        )
        output.close()
    }

    //TODO:  contact management
}
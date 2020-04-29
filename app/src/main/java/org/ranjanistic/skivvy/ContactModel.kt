package org.ranjanistic.skivvy

import java.util.jar.Attributes

class ContactModel {
    var contactID:String = String()
    var photoID:String = String()
    var displayName:String = String()
    var nickName:Array<String?>? = null
    var phoneList:Array<String?>? = null
    var emailList:Array<String?>? = null

    var photo:String = String()

    class Name{
        var prefix:String = String()
        var first:String = String()
        var middle:String = String()
        var last:String = String()
        var suffix:String = String()
        class Phonetic{
            var surname:String = String()
            var middle:String = String()
            var first:String = String()
        }
        private val phonetic = Phonetic()
        fun getPhoneticSet():Phonetic{
            return this.phonetic
        }
        class Nickname{
            var type:String = String()
            var nickname:String = String()
            var frequency:Int = 0
        }
        private val nicknameSet:ArrayList<Nickname> = ArrayList()
        fun getNicknameSet(index: Int):Nickname{
            return this.nicknameSet[index]
        }
    }
    private val name:Name = Name()
    fun getNameSet():Name{
        return this.name
    }

    class Work{
        var title:String = String()
        var company:String = String()
    }
    private val work = Work()
    fun getWorkSet():Work{
        return this.work
    }

    class Address{
        var line1:String = String()
        var line2:String = String()
        var city:String = String()
        var state:String = String()
        var country:String = String()
        var frequency:Int = 0
    }
    private val addressSet:ArrayList<Address> = ArrayList()
    fun getAddressSet(index: Int):Address{
        return this.addressSet[index]
    }
    class Phone{
        var type:String = String()
        var number:String = String()
        var frequency:Int = 0
    }
    private val phoneSet:ArrayList<Phone> = ArrayList()
    fun getPhoneSet(index:Int):Phone{
        return this.phoneSet[index]
    }
    class Email{
        var type:String = String()
        var emailID:String = String()
        var frequency:Int = 0
    }
    private val emailSet:ArrayList<Email> = ArrayList()
    fun getEmailSet(index:Int):Email{
        return this.emailSet[index]
    }
    class Date{
        var type:String = String()
        var date:String = String()
    }
    private val dateSet:ArrayList<Date> = ArrayList()
    fun getDateSet(index:Int):Date{
        return this.dateSet[index]
    }
    class Relation{
        var type:String = String()
        var relation:String = String()
    }
    private val relation = Relation()
    fun getRelationSet():Relation{
        return this.relation
    }
    private var website:String = String()
    private var label:String = String()
    private var note:String = String()
    class CustomField{
        var type:String = String()
        var value:String = String()
    }
    private val customField:ArrayList<CustomField> = ArrayList()
    fun getCustomFields(index:Int):CustomField{
        return this.customField[index]
    }
}
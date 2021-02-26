package com.xx.xx.util

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import com.xx.xx.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/***
 * 对日历、事件、参加者、提醒等执行查询、插入、更新和删除操作。
 * 1.需要有读写日历权限
 * 2.如果没有日历账户需要先创建账户
 * 3.创建账户后，再基于账户(id)实现日历事件增删改查、提醒功能
 */
object SystemCalendarUtils {
    val INSTANCE_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Instances.EVENT_ID, // 0
        CalendarContract.Instances.BEGIN, // 1
        CalendarContract.Instances.TITLE // 2
    )
    private val EVENT_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Calendars._ID,                     // 0
        CalendarContract.Calendars.ACCOUNT_NAME,            // 1
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
        CalendarContract.Calendars.OWNER_ACCOUNT            // 3
    )
    private const val PROJECTION_ID_INDEX: Int = 0
    private const val PROJECTION_BEGIN_INDEX: Int = 1
    private const val PROJECTION_TITLE_INDEX: Int = 2
    private const val PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
    private const val PROJECTION_DISPLAY_NAME_INDEX: Int = 2
    private const val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3

    private const val APP_DISPLAY_NAME = "名称"
    private const val APP_ACCOUNT_NAME = "calendar@xx.com"

    //注意看源码注释，改其他值可能导致重新打开app或重启设备清空事件和账户
    private const val APP_ACCOUNT_TYPE = CalendarContract.ACCOUNT_TYPE_LOCAL

    //查询所有存在的账户
    @SuppressLint("Recycle")
    suspend fun queryAccount(context: Context) =
        withContext<ArrayList<AccountModel>>(Dispatchers.IO) {
            val uri: Uri = CalendarContract.Calendars.CONTENT_URI
            val cur: Cursor? = context.contentResolver.query(
                uri,
                EVENT_PROJECTION,
                null,
                null,
                null
            )

            val arrayList = arrayListOf<AccountModel>()
            while (cur?.moveToNext() == true) {
                val calID: Long = cur.getLong(PROJECTION_ID_INDEX)
                val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
                val accountName: String = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
                val ownerName: String = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
                arrayList.add(AccountModel(calID, displayName, accountName, ownerName))
            }
            return@withContext arrayList
        }

    //查询是否存在此APP账户
    //返回小于等于0表示无账户，反之返回账户id
    suspend fun checkHasAccount(context: Context) = withContext<Long>(Dispatchers.IO) {
        val list = queryAccount(context)
        for (i in list) {
            if (i.accountName == APP_ACCOUNT_NAME && i.displayName == APP_DISPLAY_NAME) {
                return@withContext i.calID
            }
        }
        return@withContext -1
    }

    //创建账户
    suspend fun createAccount(context: Context) = withContext<Long>(Dispatchers.IO) {
        if (checkHasAccount(context) <= 0) {//如果不存在账户才尝试创建
            val timeZone: TimeZone = TimeZone.getDefault()
            val values: ContentValues = ContentValues().apply {
                put(CalendarContract.Calendars.NAME, APP_DISPLAY_NAME)
                put(CalendarContract.Calendars.ACCOUNT_NAME, APP_ACCOUNT_NAME)
                put(CalendarContract.Calendars.ACCOUNT_TYPE, APP_ACCOUNT_TYPE)
                put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, APP_DISPLAY_NAME)
                put(CalendarContract.Calendars.VISIBLE, 1)
                put(CalendarContract.Calendars.CALENDAR_COLOR, Color.GREEN)
                put(
                    CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                    CalendarContract.Calendars.CAL_ACCESS_OWNER
                )
                put(CalendarContract.Calendars.SYNC_EVENTS, 1)
                put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.id)
                put(CalendarContract.Calendars.OWNER_ACCOUNT, APP_ACCOUNT_NAME)
                put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0)
            }
            var uri: Uri? = CalendarContract.Calendars.CONTENT_URI
            uri = uri?.buildUpon()
                ?.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                ?.appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, APP_ACCOUNT_NAME)
                ?.appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, APP_ACCOUNT_TYPE)
                ?.build()
            uri?.let {
                val resultUri = context.contentResolver.insert(uri, values)
                return@withContext resultUri?.lastPathSegment?.toLong() ?: -1
            }
        }
        return@withContext -1
    }

    //添加事件并添加提醒-传入添加时的事件id后续删改需要传入此id
    //eventID1需要传入唯一id
    suspend fun addEvent(
        context: Context,
        eventID: Long,
        startMillis: Long,
        endMillis: Long,
        title: String,
        desc: String
    ) = withContext<Long>(Dispatchers.IO) {
        val accountId = checkHasAccount(context)
        if (accountId > 0) {//如果存在账户
            val values = ContentValues().apply {
                put(CalendarContract.Events._ID, eventID)
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, desc)
                put(CalendarContract.Events.CALENDAR_ID, accountId)
                val timeZoneId = TimeZone.getDefault().id//格式："Asia/Shanghai"
                put(CalendarContract.Events.EVENT_TIMEZONE, timeZoneId)
            }
            //添加事件
            val uri: Uri? =
                context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            //得到事件ID 您需经常使用事件 ID 来执行其他日历操作 — 例如，向事件添加参加者或提醒。
            //这里的eventID等于put(CalendarContract.Events._ID, 1234)设置的id
            val eventID1 = uri?.lastPathSegment?.toLong() ?: -1
            if (eventID1 > 0) {
                val remindersValues = ContentValues().apply {
                    //设置提前几分钟提醒时，如果是准时提醒的话需要设置为0
                    put(CalendarContract.Reminders.MINUTES, 15)
                    put(CalendarContract.Reminders.EVENT_ID, eventID1)
                    //设置提醒的次数
                    put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                }
                val remindersUri: Uri? = context.contentResolver.insert(
                    CalendarContract.Reminders.CONTENT_URI,
                    remindersValues
                )
                return@withContext remindersUri?.lastPathSegment?.toLong() ?: -1
            }
        }
        return@withContext -1
    }

    //删除事件-传入添加时的事件id
    @SuppressLint("Recycle")
    suspend fun delEvent(
        context: Context,
        eventID: Long
    ) = withContext(Dispatchers.IO) {
        val deleteUri: Uri =
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID)
        val rows: Int = context.contentResolver.delete(deleteUri, null, null)
        Log.d("调试", "Rows deleted: $rows")
    }

    //更新事件-传入添加时的事件id
    @SuppressLint("Recycle")
    suspend fun updateEvent(
        context: Context,
        eventID: Long,
        startMillis: Long,
        endMillis: Long,
        title: String,
        desc: String
    ) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, desc)
        }
        val updateUri: Uri =
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID)
        val rows: Int = context.contentResolver.update(updateUri, values, null, null)
        Log.d("调试", "Rows updated: $rows")
    }

    //查询所有事件列表(仅当前账户)
    @SuppressLint("Recycle")
    suspend fun queryEvents(context: Context) = withContext<ArrayList<EventModel>>(Dispatchers.IO) {
        val cur: Cursor? = context.contentResolver.query(
            Uri.parse("content://${CalendarContract.AUTHORITY}/events"),
            null,
            null,
            null,
            null
        )
        val arrayList = arrayListOf<EventModel>()
        while (cur?.moveToNext() == true) {
            val id: Long = cur.getString(cur.getColumnIndex("_id")).toLong()
            val title: String = cur.getString(cur.getColumnIndex("title"))
            val description: String = cur.getString(cur.getColumnIndex("description"))
            val dtstart: Long = cur.getString(cur.getColumnIndex("dtstart")).toLong()
            val dtend: Long = cur.getString(cur.getColumnIndex("dtend")).toLong()
            arrayList.add(EventModel(id,title,description, dtstart, dtend))
        }
        return@withContext arrayList
    }

    //查询是否存在某个事件-传入事件id
    suspend fun queryExistEvent(context: Context,eventID: Long,) = withContext<Boolean>(Dispatchers.IO){
        val list = queryEvents(context)
        for (i in list){
            if (i.eventID==eventID){
                return@withContext true
            }
        }
        return@withContext false
    }

    data class AccountModel(
        val calID: Long,
        val displayName: String,
        val accountName: String,
        val owner: String
    )

    data class EventModel(
        val eventID: Long,
        val title: String,
        val description: String,
        val dtstart: Long,
        val dtend: Long,
    )
}

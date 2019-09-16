package com.example.todayzero.util

import android.os.AsyncTask
import android.util.Log
import com.example.todayzero.data.Notice
import com.example.todayzero.data.Store
import com.example.todayzero.data.source.DataFilterType
import com.example.todayzero.data.source.NoticeRepository
import com.example.todayzero.data.source.DataSource

class NetworkTask(
    val dataType: DataFilterType,
    var url: String,
    val callback: DataSource.ApiListener
) : AsyncTask<String, Void, String>() {
    lateinit var storeList: ArrayList<Store>
    lateinit var noticeList: ArrayList<Notice>
    var limitNum: Int?=null

    constructor(dataList: ArrayList<*>, dataType: DataFilterType, url: String, callback: DataSource.ApiListener, limitNum:Int)
            : this(dataType, url, callback) {
        if (dataType == DataFilterType.STORE) storeList = dataList as ArrayList<Store>
        else noticeList= dataList as ArrayList<Notice>
        this.limitNum=limitNum
    }

    override fun doInBackground(vararg params: String?): String {
        return RequestHttpURLConnection().request(url)
    }

    override fun onPostExecute(result: String?) {

        super.onPostExecute(result)
        if (result == "ERROR") {
            callback.onFailure(dataType)
        } else {
            when (dataType) {
                DataFilterType.STORE -> {
                }
                DataFilterType.STORE_NUM -> {

                }
                DataFilterType.NOTICE -> {
                    var idx1 = result!!.indexOf("<tbody>")
                    var idx2 = result!!.indexOf("</tbody>")

                    var rawText = result.slice(IntRange(idx1, idx2))

                    for (i in 0 until limitNum!!) {
                        var endIdx = rawText.indexOf("a>")
                        if (endIdx == -1)
                            break
                        rawText = rawText.substring(endIdx + 2)
                    }

                    while (true) {
                        var startIdx = rawText.indexOf("<a")
                        if (startIdx == -1)
                            break
                        var endIdx = rawText.indexOf("a>")
                        var title = rawText.slice(IntRange(startIdx, endIdx))
                        rawText = rawText.substring(endIdx + 2)//a>까지 같이 짤리는데 없애려고 +2함
                        //타이틀 제목 가져오기
                        startIdx = title.indexOf(";\">")
                        endIdx = title.indexOf("</")
                        val titleText = title.slice(IntRange(startIdx + 3, endIdx - 1)).replace("&#039;","'").replace("&#034;","\"") //자름문자 빼주기

                        //타이틀 번호 가져오기 (디테일 url로 넘겨짐)
                        startIdx = title.indexOf("goDetail('")
                        endIdx = title.indexOf("');")
                        val titleNum = title.slice(IntRange(startIdx + 10, endIdx - 1)) //자름문자 빼주기

                        startIdx=rawText.indexOf("<td>")
                        rawText=rawText.substring(startIdx+4)
                        startIdx=rawText.indexOf("</td>")
                        rawText=rawText.substring(startIdx+4)
                        startIdx=rawText.indexOf("<td>")
                        endIdx=rawText.indexOf("</td>")
                        val date=rawText.slice(IntRange(startIdx+4, endIdx-1))
                        noticeList.add(Notice(titleText,"",date,titleNum))

                    }
                    //날짜 파싱해야함!!
                }
                DataFilterType.NOTICE_NUM -> {
                    var idx1 = result!!.indexOf("<tbody>")
                    var idx2 = result!!.indexOf("</tbody>")

                    var rawText = result.slice(IntRange(idx1, idx2 + 8))
                    while (true) {
                        var startIdx = rawText.indexOf("<a")
                        if (startIdx == -1)
                            break
                        var endIdx = rawText.indexOf("a>")
                        var title = rawText.slice(IntRange(startIdx, endIdx))
                        rawText = rawText.substring(endIdx + 2)//a>까지 같이 짤리는데 없애려고 +2함

                        //타이틀 제목 가져오기
                        startIdx = title.indexOf(";\">")
                        endIdx = title.indexOf("</")
                        val titleText = title.slice(IntRange(startIdx + 3, endIdx - 1)).replace("&#039;","'").replace("&#034;","\"") //자름문자 빼주기

                        //타이틀 번호 가져오기 (디테일 url로 넘겨짐)
                        startIdx = title.indexOf("goDetail('")
                        endIdx = title.indexOf("');")
                        val titleNum = title.slice(IntRange(startIdx + 10, endIdx - 1)) //자름문자 빼주기

                        //날짜 가져오기
                        startIdx=rawText.indexOf("<td>")
                        rawText=rawText.substring(startIdx+4)
                        startIdx=rawText.indexOf("</td>")
                        rawText=rawText.substring(startIdx+4)
                        startIdx=rawText.indexOf("<td>")
                        endIdx=rawText.indexOf("</td>")
                        val date=rawText.slice(IntRange(startIdx+4, endIdx-1))
                        noticeList.add(Notice(titleText,"",date,titleNum))
                    }
                    (callback as NoticeRepository.NoticeNumApiListener).onDataLoaded(noticeList.size)
                }
                DataFilterType.NOTICE_DETAIL->{
                    var idx1 = result!!.indexOf("<tbody>")
                    var idx2 = result!!.indexOf("</tbody>")

                    var detailText = result!!.slice(IntRange(idx1, idx2))

                    idx1 = detailText.indexOf("<td>")
                    idx2 = detailText.indexOf("</td>")
                    detailText = detailText.slice(IntRange(idx1 + 4, idx2 - 1))

                    detailText = detailText.replace("&nbsp;", " ")
                    detailText = detailText.replace("▶", "▶\n")
                    detailText = detailText.replace("<br/>", "\n")
                    detailText=detailText.replace("&lt;","<")
                    detailText=detailText.replace("&gt;",">")
                    detailText=detailText.replace("                                                                                ","\n")
                    detailText = detailText.replace("                                                                                ", "\n")
                    Log.i("test","!"+detailText[0]+"!"+detailText[0].toInt())
                    noticeList[limitNum!!].content=detailText

            }

            }
            callback.onDataLoaded(dataType)
        }

    }


}
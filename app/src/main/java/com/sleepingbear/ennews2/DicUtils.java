package com.sleepingbear.ennews2;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class DicUtils {
    public static String getString(String str) {
        if (str == null)
            return "";
        else
            return str.trim();
    }

    public static String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return year + "" + (month + 1 > 9 ? "" : "0") + (month + 1) + "" + (day > 9 ? "" : "0") + day;
    }

    public static String getAddDay(String date, int addDay) {
        String mDate = date.replaceAll("[.-/]", "");

        int year = Integer.parseInt(mDate.substring(0, 4));
        int month = Integer.parseInt(mDate.substring(4, 6)) - 1;
        int day = Integer.parseInt(mDate.substring(6, 8));

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day + addDay);

        return c.get(Calendar.YEAR) + "" + (c.get(Calendar.MONTH) + 1 > 9 ? "" : "0") + (c.get(Calendar.MONTH) + 1) + "" + (c.get(Calendar.DAY_OF_MONTH) > 9 ? "" : "0") + c.get(Calendar.DAY_OF_MONTH);
    }

    public static String getDelimiterDate(String date, String delimiter) {
        if (getString(date).length() < 8) {
            return "";
        } else {
            return date.substring(0, 4) + delimiter + date.substring(4, 6) + delimiter + date.substring(6, 8);
        }
    }

    public static String getYear(String date) {
        if (date == null) {
            return "";
        } else {
            String mDate = date.replaceAll("[.-/]", "");
            return mDate.substring(0, 4);
        }
    }

    public static String getMonth(String date) {
        if (date == null) {
            return "";
        } else {
            String mDate = date.replaceAll("[.-/]", "");
            return mDate.substring(4, 6);
        }
    }

    public static String getDay(String date) {
        if (date == null) {
            return "";
        } else {
            String mDate = date.replaceAll("[.-/]", "");
            return mDate.substring(6, 8);
        }
    }

    public static void dicSqlLog(String str) {
        if (BuildConfig.DEBUG) {
            Log.d(CommConstants.tag + " ====>", str);
        }
    }

    public static void dicLog(String str) {
        if (BuildConfig.DEBUG) {
            Calendar cal = Calendar.getInstance();
            String time = cal.get(Calendar.HOUR_OF_DAY) + "시 " + cal.get(Calendar.MINUTE) + "분 " + cal.get(Calendar.SECOND) + "초";

            Log.d(CommConstants.tag + " ====>", time + " : " + str);
        }
    }

    public static String lpadding(String str, int length, String fillStr) {
        String rtn = "";

        for (int i = 0; i < length - str.length(); i++) {
            rtn += fillStr;
        }
        return rtn + (str == null ? "" : str);
    }

    public static String[] sentenceSplit(String sentence) {
        ArrayList<String> al = new ArrayList<String>();

        if ( sentence != null ) {
            String tmpSentence = sentence + " ";

            int startPos = 0;
            for (int i = 0; i < tmpSentence.length(); i++) {
                if (CommConstants.sentenceSplitStr.indexOf(tmpSentence.substring(i, i + 1)) > -1) {
                    if (i == 0) {
                        al.add(tmpSentence.substring(i, i + 1));
                        startPos = i + 1;
                    } else {
                        if (i != startPos) {
                            al.add(tmpSentence.substring(startPos, i));
                        }
                        al.add(tmpSentence.substring(i, i + 1));
                        startPos = i + 1;
                    }
                }
            }
        }

        String[] stringArr = new String[al.size()];
        stringArr = al.toArray(stringArr);

        return stringArr;
    }

    public static String getSentenceWord(String[] sentence, int kind, int position) {
        String rtn = "";
        if ( kind == 1 ) {
            rtn = sentence[position];
        } else if ( kind == 2 ) {
            if ( position + 2 <= sentence.length - 1 ) {
                if ( " ".equals(sentence[position + 1]) ) {
                    rtn = sentence[position] + sentence[position + 1] + sentence[position + 2];
                }
            }
        } else if ( kind == 3 ) {
            if ( position + 4 <= sentence.length - 1 ) {
                if ( " ".equals(sentence[position + 1]) && " ".equals(sentence[position + 3]) ) {
                    rtn = sentence[position] + sentence[position + 1] + sentence[position + 2] + sentence[position + 3] + sentence[position + 4];
                }
            }
        }

        //dicLog(rtn);
        return rtn;
    }

    public static String getOneSpelling(String spelling) {
        String rtn = "";
        String[] str = spelling.split(",");
        if ( str.length == 1 ) {
            rtn = spelling;
        } else {
            rtn = str[0] + "(" + str[1] + ")";
        }

        return rtn;
    }

    public static void readInfoFromFile(Context ctx, SQLiteDatabase db, String fileName) {
        dicLog(DicUtils.class.toString() + " : " + "readInfoFromFile start, " + fileName);

        //데이타 복구
        FileInputStream fis = null;
        try {
            //데이타 초기화
            DicDb.initVocabulary(db);
            DicDb.initDicClickWord(db);
            DicDb.initMyNovel(db);

            if ( "".equals(fileName) ) {
                fis = ctx.openFileInput(CommConstants.infoFileName);
            } else {
                fis = new FileInputStream(new File(fileName));
            }

            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader buffreader = new BufferedReader(isr);

            //출력...
            String readString = buffreader.readLine();
            while (readString != null) {
                dicLog(readString);

                String[] row = readString.split("[/^]");
                if ( row[0].equals(CommConstants.tag_code_ins) ) {
                    DicDb.insCode(db, row[1], row[2], row[3]);
                } else if ( row[0].equals(CommConstants.tag_voc_ins) ) {
                    DicDb.insDicVoc(db, row[1], row[2], row[3], row[4]);
                } else if ( row[0].equals(CommConstants.tag_click_word_ins) ) {
                    DicDb.insDicClickWord(db, row[1], row[2]);
                //} else if ( row[0].equals(CommConstants.tag_news_ins) ) {
                    //DicDb.insNewsBackup(db, row[1], row[2], row[3], row[4], row[5], row[6]);
                }

                readString = buffreader.readLine();
            }

            isr.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        dicLog(DicUtils.class.toString() + " : " + "readInfoFromFile end");
    }

    /**
     * 데이타 기록
     * @param ctx
     * @param db
     */
    public static void writeInfoToFile(Context ctx, SQLiteDatabase db, String fileName) {
        System.out.println("writeNewInfoToFile start");

        try {
            FileOutputStream fos = null;

            if ( "".equals(fileName) ) {
                fos = ctx.openFileOutput(CommConstants.infoFileName, Context.MODE_PRIVATE);
            } else {
                File saveFile = new File(fileName);
                try {
                    saveFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
                fos = new FileOutputStream(saveFile);
            }

            Cursor cursor = db.rawQuery(DicQuery.getWriteData(), null);
            while (cursor.moveToNext()) {
                String writeData = cursor.getString(cursor.getColumnIndexOrThrow("WRITE_DATA"));
                DicUtils.dicLog(writeData);
                if ( writeData != null ) {
                    fos.write((writeData.getBytes()));
                    fos.write("\n".getBytes());
                }
            }
            cursor.close();

            fos.close();
        } catch (Exception e) {
            DicUtils.dicLog("File 에러=" + e.toString());
        }

        System.out.println("writeNewInfoToFile end");
    }

    public static boolean isHangule(String pStr) {
        boolean isHangule = false;
        String str = (pStr == null ? "" : pStr);
        try {
            isHangule = str.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
        }

        return isHangule;
    }

    public static Document getDocument(String url) throws Exception {
        Document doc = null;
        //while (true) {
        //    try {
                doc = Jsoup.connect(url).timeout(60000).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US;   rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();
        //        break;
        //    } catch (Exception e) {
        //        System.out.println(e.getMessage());
        //    }
        //}

        return doc;
    }

    public static Element findElementSelect(Document doc, String tag, String attr, String value) throws Exception {
        Elements es = doc.select(tag);
        for (Element es_r : es) {
            if (value.equals(es_r.attr(attr))) {
                return es_r;
            }
        }

        return null;
    }

    public static Element findElementForTag(Element e, String tag, int findIdx) throws Exception {
        if (e == null) {
            return null;
        }

        int idx = 0;
        for (int i = 0; i < e.children().size(); i++) {
            if (tag.equals(e.child(i).tagName())) {
                if (idx == findIdx) {
                    return e.child(i);
                } else {
                    idx++;
                }
            }
        }

        return null;
    }

    public static Element findElementForTagAttr(Element e, String tag, String attr, String value) throws Exception {
        if (e == null) {
            return null;
        }

        for (int i = 0; i < e.children().size(); i++) {
            if (tag.equals(e.child(i).tagName()) && value.equals(e.child(i).attr(attr))) {
                return e.child(i);
            }
        }

        return null;
    }

    public static String getAttrForTagIdx(Element e, String tag, int findIdx, String attr) throws Exception {
        if (e == null) {
            return null;
        }

        int idx = 0;
        for (int i = 0; i < e.children().size(); i++) {
            if (tag.equals(e.child(i).tagName())) {
                if (idx == findIdx) {
                    return e.child(i).attr(attr);
                } else {
                    idx++;
                }
            }
        }

        return "";
    }

    public static String getElementText(Element e) throws Exception {
        if (e == null) {
            return "";
        } else {
            return e.text();
        }
    }

    public static String getElementHtml(Element e) throws Exception {
        if (e == null) {
            return "";
        } else {
            return e.html();
        }
    }

    public static String getUrlParamValue(String url, String param) throws Exception {
        String rtn = "";

        if (url.indexOf("?") < 0) {
            return "";
        }
        String[] split_url = url.split("[?]");
        String[] split_param = split_url[1].split("[&]");
        for (int i = 0; i < split_param.length; i++) {
            String[] split_row = split_param[i].split("[=]");
            if (param.equals(split_row[0])) {
                rtn = split_row[1];
            }
        }

        return rtn;
    }

    public static Boolean isNetWork(AppCompatActivity context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService (Context.CONNECTIVITY_SERVICE);
        boolean isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        boolean isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        boolean isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();
        boolean isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

        return (isWifiAvailable && isWifiConnect) || (isMobileAvailable && isMobileConnect);
    }

    public static String getBtnString(String word){
        String rtn = "";

        if ( word.length() == 1 ) {
            rtn = "  " + word + "  ";
        } else if ( word.length() == 2 ) {
            rtn = "  " + word + " ";
        } else if ( word.length() == 3 ) {
            rtn = " " + word + " ";
        } else if ( word.length() == 4 ) {
            rtn = " " + word;
        } else {
            rtn = " " + word + " ";
        }

        return rtn;
    }

    public static void setDbChange(Context mContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CommConstants.flag_dbChange, "Y");
        editor.commit();

        dicLog(DicUtils.class.toString() + " setDbChange : " + "Y");
    }

    public static String getDbChange(Context mContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getString(CommConstants.flag_dbChange, "N");
    }

    public static void clearDbChange(Context mContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CommConstants.flag_dbChange, "N");
        editor.commit();
    }

    public static String getPreferencesValue(Context context, String preference) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String rtn = sharedPref.getString( preference, "" );
        if ( "".equals( rtn ) ) {
            if ( preference.equals(CommConstants.preferences_font) ) {
                rtn = "17";
            } else if ( preference.equals(CommConstants.preferences_wordView) ) {
                rtn = "0";
            } else if ( preference.equals(CommConstants.preferences_webViewFont) ) {
                rtn = "3";
            } else {
                rtn = "";
            }
        }

        DicUtils.dicLog(rtn);

        return rtn;
    }

    public static ArrayList gatherCategory(SQLiteDatabase db, String url, String codeGroup) {
        ArrayList wordAl = new ArrayList();
        try {
            int cnt = 1;
            boolean isBreak = false;
            while (true) {
                Document doc = getDocument(url + "&page=" + cnt);
                Element table_e = findElementSelect(doc, "table", "class", "tbl_wordbook");
                Element tbody_e = findElementForTag(table_e, "tbody", 0);
                for (int m = 0; m < tbody_e.children().size(); m++) {
                    HashMap row = new HashMap();

                    Element category = findElementForTag(tbody_e.child(m), "td", 1);

                    String categoryId = getUrlParamValue(category.child(0).attr("href"), "id").replace("\n", "");
                    String categoryName = category.text();
                    String wordCnt = findElementForTag(tbody_e.child(m), "td", 3).text();
                    String bookmarkCnt = findElementForTag(tbody_e.child(m), "td", 4).text();
                    String updDate = findElementForTag(tbody_e.child(m), "td", 5).text();
                    dicLog(codeGroup + " : " + categoryName + " : " + categoryId + " : " + categoryName + " : " + wordCnt + " : " + bookmarkCnt + " : " + updDate) ;
                    Cursor cursor = db.rawQuery(DicQuery.getDaumCategory(categoryId), null);
                    if (cursor.moveToNext()) {
                        if ( categoryId.equals(cursor.getString(cursor.getColumnIndexOrThrow("CATEGORY_ID"))) && updDate.equals(cursor.getString(cursor.getColumnIndexOrThrow("UPD_DATE"))) ) {
                            isBreak = true;
                            break;
                        } else {
                            //수정
                            DicDb.updDaumCategoryInfo(db, categoryId, categoryName, updDate, bookmarkCnt);
                        }
                    } else {
                        //입력
                        DicDb.insDaumCategoryInfo(db, codeGroup, categoryId, categoryName, updDate, wordCnt, bookmarkCnt);
                    }
                }

                if ( isBreak ) {
                    break;
                }

                HashMap pageHm = new HashMap();
                Element div_paging = findElementSelect(doc, "div", "class", "paging_comm paging_type1");
                for (int is = 0; is < div_paging.children().size(); is++) {
                    if ("a".equals(div_paging.child(is).tagName())) {
                        HashMap row = new HashMap();

                        String page = getUrlParamValue(div_paging.child(is).attr("href"), "page");
                        pageHm.put(page, page);
                    }
                }
                // 페이지 정보중에 다음 페이지가 없으면 종료...
                if (!pageHm.containsKey(Integer.toString(cnt + 1))) {
                    break;
                } else {
                    dicLog("cnt : " + cnt);
                    cnt++;
                }
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return wordAl;
    }

    public static ArrayList gatherCategoryWord(String url) {
        ArrayList wordAl = new ArrayList();
        try {
            int cnt = 1;
            while (true) {
                Document doc = getDocument(url + "&page=" + cnt);
                Element div_e = findElementSelect(doc, "div", "class", "list_word on");
                for (int is = 0; is < div_e.children().size(); is++) {
                    if ("div".equals(div_e.child(is).tagName())) {
                        HashMap row = new HashMap();

                        Element wordDiv = findElementForTagAttr(div_e.child(is), "div", "class", "txt_word");

                        row.put("WORD", wordDiv.child(0).child(0).text());
                        wordAl.add(row);
                    }
                }
                HashMap pageHm = new HashMap();
                Element div_paging = findElementSelect(doc, "div", "class", "paging_comm paging_type1");
                for (int is = 0; is < div_paging.children().size(); is++) {
                    if ("a".equals(div_paging.child(is).tagName())) {
                        HashMap row = new HashMap();

                        String page = getUrlParamValue(div_paging.child(is).attr("href"), "page");
                        pageHm.put(page, page);
                    }
                }
                // 페이지 정보중에 다음 페이지가 없으면 종료...
                if (!pageHm.containsKey(Integer.toString(cnt + 1))) {
                    break;
                } else {
                    cnt++;
                }
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return wordAl;
    }

    public static void getNovelList0(SQLiteDatabase db, String url, String kind) {
        try {
            Document doc = getDocument(url);
            Elements es = doc.select("li a");

            DicDb.delNovel(db, kind);

            for (int m = 0; m < es.size(); m++) {
                DicDb.insNovel(db, kind, es.get(m).text(), es.get(m).attr("href"));
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }
    }

    public static void getNovelList1(SQLiteDatabase db, String url, String kind) {
        try {
            Document doc = getDocument(url);
            Elements es = doc.select("ul.titlelist li");

            DicDb.delNovel(db, kind);

            for (int m = 0; m < es.size(); m++) {
                DicDb.insNovel(db, kind, es.get(m).text(), es.get(m).child(0).attr("href"));
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }
    }

    public static void getNovelList2(SQLiteDatabase db, String url, String kind) {
        dicLog("getNovelList2 : " + url);
        try {
            Document doc = getDocument(url);
            Elements es = doc.select("li.menu-li-bottom p.paginate-bar");
            String pageStr = es.get(0).text().trim().replaceAll("Page ","").replaceAll("of ","").split(" ")[1];
            int page = Integer.parseInt(pageStr);

            ArrayList al = new ArrayList();
            for ( int i = 1; i <= page; i++ ) {
                String pageUrl = url;
                if ( i > 1 ) {
                    doc = getDocument(url + "&page=" + i);
                }
                Elements es2 = doc.select("li.list-li");
                for ( int m = 0; m < es2.size(); m++ ) {
                    //dicLog(i + " page " + m + " td");

                    Elements esA = es2.get(m).select("a.list-link");
                    Elements esImg = es2.get(m).select("img");
                    if ( esA.size() > 0 ) {
                        HashMap hm = new HashMap();
                        hm.put("url", esA.get(0).attr("href"));
                        hm.put("title", esImg.get(0).attr("alt"));
                        al.add(hm);
                    }
                }
                es2 = doc.select("ul#s-list-ul li");
                for ( int m = 0; m < es2.size(); m++ ) {
                    //dicLog(i + " page " + m + " td");

                    Elements esA = es2.get(m).select("a");
                    if ( esA.size() > 0 ) {
                        HashMap hm = new HashMap();
                        hm.put("url", esA.get(0).attr("href"));
                        hm.put("title", es2.get(m).text().replaceAll("[:]", ""));
                        al.add(hm);
                    }
                }
            }

            DicDb.delNovel(db, kind);

            for (int i = 0; i < al.size(); i++) {
                DicDb.insNovel(db, kind, (String)((HashMap)al.get(i)).get("title"), (String)((HashMap)al.get(i)).get("url"));
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }
    }

    public static int getNovelPartCount0(String url) {
        int partSize = 0;
        try {
            Document doc = getDocument(url);
            Elements es = doc.select("li a");
            partSize = es.size();
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return partSize;
    }

    public static int getNovelPartCount1(String url) {
        int partSize = 0;
        try {
            Document doc = getDocument(url);
            Elements es = doc.select("ul.chapter-list li");
            partSize = es.size();
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return partSize;
    }

    public static String getNovelContent0(String url) {
        String rtn = "";
        try {
            Document doc = getDocument(url);
            Elements contents = doc.select("td font");
            rtn = contents.get(1).html().replaceAll("<br /> <br />", "\n").replaceAll("&quot;","\"").replaceAll("<br />","");
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return rtn;
    }

    public static String getNovelContent1(String url) {
        String rtn = "";
        try {
            Document doc = getDocument(url);
            Elements contents = doc.select("td.chapter-text span.chapter-heading");
            if ( contents.size() > 0 ) {
                rtn += contents.get(0).text() + "\n\n\n";
            }

            contents = doc.select("td.chapter-text p");
            for ( int i = 0; i < contents.size(); i++ ) {
                rtn += contents.get(i).text() + "\n\n";
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return rtn;
    }

    public static String getNovelContent2(String url) {
        StringBuffer rtn = new StringBuffer();
        try {
            Document doc = getDocument(url);
            Elements esA = doc.select("ul#book-ul a");
            for ( int i = 0; i < esA.size(); i++ ) {
                if ( esA.get(i).attr("href").indexOf(".txt") >= 0 ) {
                    InputStream inputStream = new URL("http://www.loyalbooks.com" + esA.get(i).attr("href")).openStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while((line = rd.readLine()) != null) {
                        rtn.append(line);
                        rtn.append('\n');
                    }
                    rd.close();
                }
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return rtn.toString();
    }

    public static File getFIle(String folderName, String fileName) {
        File appDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + folderName);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        File saveFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + folderName + "/" + fileName);

        return saveFile;
    }

    public static String getHtmlString(String title, String contents, int fontSize) {
        StringBuffer sb = new StringBuffer();
        sb.append("<!doctype html>");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("</head>");
        sb.append("<script src='https://code.jquery.com/jquery-1.11.3.js'></script>");
        sb.append("<script>");
        sb.append("$( document ).ready(function() {");
        sb.append("    $('#news_title,#news_contents').html(function(index, oldHtml) {");
        sb.append("        return oldHtml.replace(/<[^>]*>/g, '').replace(/(<br>)/g, '\\n').replace(/\\b(\\w+?)\\b/g,'<span class=\"word\">$1</span>').replace(/\\n/g, '<br>')");
        sb.append("    });");
        sb.append("    $('.word').click(function(event) {");
        sb.append("        window.android.setWord(event.target.innerHTML)");
        sb.append("    });");
        sb.append("});");
        sb.append("</script>");

        sb.append("<body>");
        sb.append("<h3 id='news_title'>");
        sb.append(title);
        sb.append("</h3>");
        sb.append("<font size='" + fontSize + "' face='돋움'><div id='news_contents'>");
        sb.append(contents);
        sb.append("</div></font></body>");
        sb.append("</html>");

        return sb.toString();
    }

    public static String getMyNovelContent(String path) {
        String content = "";
        try {
            FileInputStream fis = new FileInputStream(new File(path));
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String temp = "";
            while( (temp = br.readLine()) != null) {
                content += temp + "\n";
            }

            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

        return content;
    }

    public static String getFilePageContent(String path, int pageSize, int page) {
        //dicLog("getFilePageContent : " + pageSize + " : " + page);
        String content = "";
        try {
            FileInputStream fis = new FileInputStream(new File(path));
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String temp = "";
            int getContentSize = 0;
            while( (temp = br.readLine()) != null) {
                getContentSize += temp.length();
                if ( getContentSize > ( page - 1 ) * pageSize && getContentSize < page * pageSize ) {
                    content += temp + "\n";
                } else if ( getContentSize > page * pageSize ) {
                    break;
                }
            }

            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

        //dicLog("content length : " + content.length());
        return content;
    }

    public static int getFilePageCount(String path, int pageSize) {
        int getContentSize = 0;
        try {
            FileInputStream fis = new FileInputStream(new File(path));
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String temp = "";
            while( (temp = br.readLine()) != null) {
                getContentSize += temp.length();
            }

            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

        int pageCount = (int)Math.ceil(getContentSize / pageSize);
        if ( getContentSize - pageCount * pageSize > 0 ) {
            pageCount++;
        }
        //dicLog("content page : " + getContentSize + " : " + pageSize + " : " + pageCount);
        return pageCount;
    }

    public static void setMainNews(Context mContext, String news) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CommConstants.flag_mainNews, news);
        editor.commit();
    }

    public static String getMainNews(Context mContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getString(CommConstants.flag_mainNews, "N01");
    }

    public static String[] getNews(String kind) {
        String[] news = new String[11];
        int idx = 0;

        if ( "N".equals(kind) ) {
            news[idx++] = "Korea Joongang Daily";
            news[idx++] = "The Korea Herald";
            news[idx++] = "The Korea Times";
            news[idx++] = "The Chosunilbo";
            news[idx++] = "ABC News";
            news[idx++] = "BBC News";
            news[idx++] = "CNN";
            news[idx++] = "LosAngeles Times";
            news[idx++] = "Reuters";
            news[idx++] = "The New Work Times";
            news[idx++] = "Washingtone Post";
        } else if ( "C".equals(kind) ) {
            news[idx++] = CommConstants.news_KoreaJoongangDaily;
            news[idx++] = CommConstants.news_TheKoreaHerald;
            news[idx++] = CommConstants.news_TheKoreaTimes;
            news[idx++] = CommConstants.news_TheChosunilbo;
            news[idx++] = CommConstants.news_abcNews;
            news[idx++] = CommConstants.news_bbcNews;
            news[idx++] = CommConstants.news_cnn;
            news[idx++] = CommConstants.news_losangeles;
            news[idx++] = CommConstants.news_reuters;
            news[idx++] = CommConstants.news_newWorkTimes;
            news[idx++] = CommConstants.news_washingtonePost;
        } else if ( "U".equals(kind) ) {
            news[idx++] = "http://koreajoongangdaily.joins.com";
            news[idx++] = "http://www.koreaherald.com";
            news[idx++] = "http://www.koreatimes.co.kr";
            news[idx++] = "http://english.chosun.com";
            news[idx++] = "http://abcnews.go.com/";
            news[idx++] = "http://www.bbc.com/";
            news[idx++] = "http://edition.cnn.com/";
            news[idx++] = "http://www.latimes.com/";
            news[idx++] = "http://www.reuters.com/";
            news[idx++] = "http://mobile.nytimes.com";
            news[idx++] = "https://www.washingtonpost.com";
        } else if ( "W".equals(kind) ) {
            news[idx++] = "E002";
            news[idx++] = "E003";
            news[idx++] = "E004";
            news[idx++] = "E001";
            news[idx++] = "E005";
            news[idx++] = "E006";
            news[idx++] = "E007";
            news[idx++] = "E008";
            news[idx++] = "E010";
            news[idx++] = "E009";
            news[idx++] = "E011";
        }

        return news;
    }

    public static String[] getNewsCategory(String newsCode, String kind) {
        String[] category = new String[1];
        int idx = 0;
        ArrayList al = new ArrayList();

        if ( newsCode.equals(CommConstants.news_KoreaJoongangDaily) ) {
            al.add(idx++, getNewsInfo("National - Politics","030101","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=030101"));
            al.add(idx++, getNewsInfo("National - Social affairs","030201","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=030201"));
            al.add(idx++, getNewsInfo("National - Education","030301","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=030301"));
            al.add(idx++, getNewsInfo("National - People","030401","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=030401"));
            al.add(idx++, getNewsInfo("National - Special Series","030501","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=030501"));

            al.add(idx++, getNewsInfo("Business - Economy","050101","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=050101"));
            al.add(idx++, getNewsInfo("Business - Finance","050201","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=050201"));
            al.add(idx++, getNewsInfo("Business - Industry","050301","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=050301"));
            al.add(idx++, getNewsInfo("Business - Stock Market","050401","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=050401"));
            al.add(idx++, getNewsInfo("Business - Speical Series","050601","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=050601"));

            al.add(idx++, getNewsInfo("Opinion - Editorials","010101","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=010101"));
            al.add(idx++, getNewsInfo("Opinion - Columns","010201","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=010201"));
            al.add(idx++, getNewsInfo("Opinion - Fountain","010301","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=010301"));
            al.add(idx++, getNewsInfo("Opinion - Letters","010501","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=010501"));

            al.add(idx++, getNewsInfo("Culture - Features","020101","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=020101"));
            al.add(idx++, getNewsInfo("Culture - Arts","020201","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=020201"));
            al.add(idx++, getNewsInfo("Culture - Entertainment","020301","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=020301"));
            al.add(idx++, getNewsInfo("Culture - Style & Travel","020401","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=020401"));
            al.add(idx++, getNewsInfo("Culture - Movie","020901","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=020901"));
            al.add(idx++, getNewsInfo("Culture - Korean Heritage","020801","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=020801"));
            al.add(idx++, getNewsInfo("Culture - Ticket","020601","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=020601"));
            al.add(idx++, getNewsInfo("Culture - Music & Performance","021001","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=021001"));

            al.add(idx++, getNewsInfo("Sports - Domestic","070101","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=070101"));
            al.add(idx++, getNewsInfo("Sports - International","070201","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=070201"));
            al.add(idx++, getNewsInfo("Sports - Special Series","070301","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=070301"));

            al.add(idx++, getNewsInfo("Foreign Community - Activities","040101","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=040101"));
            al.add(idx++, getNewsInfo("Foreign Community - Special Series","040401","http://koreajoongangdaily.joins.com/news/list/List.aspx?gCat=040401"));
        } else if ( newsCode.equals(CommConstants.news_TheChosunilbo)) {
            al.add(idx++, getNewsInfo("National","11","http://english.chosun.com/svc/list_in/list.html?catid=11"));
            al.add(idx++, getNewsInfo("Politics","12","http://english.chosun.com/svc/list_in/list.html?catid=12"));
            al.add(idx++, getNewsInfo("North Korea","F","http://english.chosun.com/svc/list_in/list.html?catid=F"));
            al.add(idx++, getNewsInfo("Business","21","http://english.chosun.com/svc/list_in/list.html?catid=21"));
            al.add(idx++, getNewsInfo("Sci-Tech","22","http://english.chosun.com/svc/list_in/list.html?catid=22"));
            al.add(idx++, getNewsInfo("Sports","3","http://english.chosun.com/svc/list_in/list.html?catid=3"));
            al.add(idx++, getNewsInfo("Entertainment","45","http://english.chosun.com/svc/list_in/list.html?catid=45"));
            al.add(idx++, getNewsInfo("Health","G1","http://english.chosun.com/svc/list_in/list.html?catid=G1"));
            al.add(idx++, getNewsInfo("Lifestyle","G2","http://english.chosun.com/svc/list_in/list.html?catid=G2"));
        } else if ( newsCode.equals(CommConstants.news_TheKoreaHerald)) {
            al.add(idx++, getNewsInfo("National - Politics","020101000000","http://www.koreaherald.com/list.php?ct=020101000000"));
            al.add(idx++, getNewsInfo("National - Social Affairs","020102000000","http://www.koreaherald.com/list.php?ct=020102000000"));
            al.add(idx++, getNewsInfo("National - Foreign Affairs","020103000000","http://www.koreaherald.com/list.php?ct=020103000000"));
            al.add(idx++, getNewsInfo("National - Defense","020106000000","http://www.koreaherald.com/list.php?ct=020106000000"));
            al.add(idx++, getNewsInfo("National - North Korea","020104000000","http://www.koreaherald.com/list.php?ct=020104000000"));
            al.add(idx++, getNewsInfo("National - Sharing","020108000000","http://www.koreaherald.com/list.php?ct=020108000000"));
            al.add(idx++, getNewsInfo("National - Science","020107000000","http://www.koreaherald.com/list.php?ct=020107000000"));
            al.add(idx++, getNewsInfo("National - Diplomatic Circuit","020103010000","http://www.koreaherald.com/list.php?ct=020103010000"));
            al.add(idx++, getNewsInfo("National - Education","020109000000","http://www.koreaherald.com/list.php?ct=020109000000"));
            al.add(idx++, getNewsInfo("National - Environment","020110000000","http://www.koreaherald.com/list.php?ct=020110000000"));

            al.add(idx++, getNewsInfo("Business - Economy","020201000000","http://www.koreaherald.com/list.php?ct=020201000000"));
            al.add(idx++, getNewsInfo("Business - Finance","020202000000","http://www.koreaherald.com/list.php?ct=020202000000"));
            al.add(idx++, getNewsInfo("Business - Industry","020203000000","http://www.koreaherald.com/list.php?ct=020203000000"));
            al.add(idx++, getNewsInfo("Business - Technology","020206000000","http://www.koreaherald.com/list.php?ct=020206000000"));
            al.add(idx++, getNewsInfo("Business - Automode","020205000000","http://www.koreaherald.com/list.php?ct=020205000000"));
            al.add(idx++, getNewsInfo("Business - Management","020207000000","http://www.koreaherald.com/list.php?ct=020207000000"));

            al.add(idx++, getNewsInfo("Life & Style - Culture","020307000000","http://www.koreaherald.com/list.php?ct=020307000000"));
            al.add(idx++, getNewsInfo("Life & Style - Travel","020301000000","http://www.koreaherald.com/list.php?ct=020301000000"));
            al.add(idx++, getNewsInfo("Life & Style - Fashion","020302000000","http://www.koreaherald.com/list.php?ct=020302000000"));
            al.add(idx++, getNewsInfo("Life & Style - Food & Beverage","020303000000","http://www.koreaherald.com/list.php?ct=020303000000"));
            al.add(idx++, getNewsInfo("Life & Style - Books","020304000000","http://www.koreaherald.com/list.php?ct=020304000000"));
            al.add(idx++, getNewsInfo("Life & Style - People","020305000000","http://www.koreaherald.com/list.php?ct=020305000000"));
            al.add(idx++, getNewsInfo("Life & Style - Expat Living","020306000000","http://www.koreaherald.com/list.php?ct=020306000000"));
            al.add(idx++, getNewsInfo("Life & Style - Design","020308000000","http://www.koreaherald.com/list.php?ct=020308000000"));
            al.add(idx++, getNewsInfo("Life & Style - Health","020309000000","http://www.koreaherald.com/list.php?ct=020309000000"));

            al.add(idx++, getNewsInfo("Enterainment - Film","020401000000","http://www.koreaherald.com/list.php?ct=020401000000"));
            al.add(idx++, getNewsInfo("Enterainment - Television","020402000000","http://www.koreaherald.com/list.php?ct=020402000000"));
            al.add(idx++, getNewsInfo("Enterainment - Music","020403000000","http://www.koreaherald.com/list.php?ct=020403000000"));
            al.add(idx++, getNewsInfo("Enterainment - Arts","020404000000","http://www.koreaherald.com/list.php?ct=020404000000"));
            al.add(idx++, getNewsInfo("Enterainment - Hallyu","020405000000","http://www.koreaherald.com/list.php?ct=020405000000"));

            al.add(idx++, getNewsInfo("Sports - Soccer","020501000000","http://www.koreaherald.com/list.php?ct=020501000000"));
            al.add(idx++, getNewsInfo("Sports - Baseball","020502000000","http://www.koreaherald.com/list.php?ct=020502000000"));
            al.add(idx++, getNewsInfo("Sports - Golf","020503000000","http://www.koreaherald.com/list.php?ct=020503000000"));
            al.add(idx++, getNewsInfo("Sports - More Sports","020504000000","http://www.koreaherald.com/list.php?ct=020504000000"));

            al.add(idx++, getNewsInfo("World - World News","021201000000","http://www.koreaherald.com/list.php?ct=021201000000"));
            al.add(idx++, getNewsInfo("World - World Business","021202000000","http://www.koreaherald.com/list.php?ct=021202000000"));
            al.add(idx++, getNewsInfo("World - Asia News Network","021204000000","http://www.koreaherald.com/list.php?ct=021204000000"));

            al.add(idx++, getNewsInfo("Opinion - Editorial","020601000000","http://www.koreaherald.com/list.php?ct=020601000000"));
            al.add(idx++, getNewsInfo("Opinion - Viewpoints","020603000000","http://www.koreaherald.com/list.php?ct=020603000000"));
            al.add(idx++, getNewsInfo("Opinion - Voice","020604000000","http://www.koreaherald.com/list.php?ct=020604000000"));
        } else if ( newsCode.equals(CommConstants.news_TheKoreaTimes)) {
            al.add(idx++, getNewsInfo("North Korea","103","http://www.koreatimes.co.kr/www/sublist_103.html"));

            al.add(idx++, getNewsInfo("Entertainment - Music","682","http://www.koreatimes.co.kr/www/sublist_682.html"));
            al.add(idx++, getNewsInfo("Entertainment - Dramas & TV shows","688","http://www.koreatimes.co.kr/www/sublist_688.html"));
            al.add(idx++, getNewsInfo("Entertainment - Movies","689","http://www.koreatimes.co.kr/www/sublist_689.html"));
            al.add(idx++, getNewsInfo("Entertainment - Performances","690","http://www.koreatimes.co.kr/www/sublist_690.html"));
            al.add(idx++, getNewsInfo("Entertainment - Exhibitions","691","http://www.koreatimes.co.kr/www/sublist_691.html"));

            al.add(idx++, getNewsInfo("Opinion - Editorial","202","http://www.koreatimes.co.kr/www/sublist_202.html"));
            al.add(idx++, getNewsInfo("Opinion - Reporter`s Notebook","264","http://www.koreatimes.co.kr/www/sublist_264.html"));
            al.add(idx++, getNewsInfo("Opinion - Guest Column","197","http://www.koreatimes.co.kr/www/sublist_197.html"));
            al.add(idx++, getNewsInfo("Opinion - Thoughts of the Times","162","http://www.koreatimes.co.kr/www/sublist_162.html"));
            al.add(idx++, getNewsInfo("Opinion - Letter to the Editor","161","http://www.koreatimes.co.kr/www/sublist_161.html"));
            al.add(idx++, getNewsInfo("Opinion - Times Forum","198","http://www.koreatimes.co.kr/www/sublist_198.html"));

            al.add(idx++, getNewsInfo("Economy - Policies","367","http://www.koreatimes.co.kr/www/sublist_367.html"));
            al.add(idx++, getNewsInfo("Economy - Finance","488","http://www.koreatimes.co.kr/www/sublist_488.html"));

            al.add(idx++, getNewsInfo("Biz & Tech - Automotive","419","http://www.koreatimes.co.kr/www/sublist_419.html"));
            al.add(idx++, getNewsInfo("Biz & Tech - IT","133","http://www.koreatimes.co.kr/www/sublist_133.html"));
            al.add(idx++, getNewsInfo("Biz & Tech - Heavy industries","693","http://www.koreatimes.co.kr/www/sublist_693.html"));
            al.add(idx++, getNewsInfo("Biz & Tech - Light industries","694","http://www.koreatimes.co.kr/www/sublist_694.html"));
            al.add(idx++, getNewsInfo("Biz & Tech - Science","325","http://www.koreatimes.co.kr/www/sublist_325.html"));
            al.add(idx++, getNewsInfo("Biz & Tech - Game","134","http://www.koreatimes.co.kr/www/sublist_134.html"));

            al.add(idx++, getNewsInfo("National - Politics","356","http://www.koreatimes.co.kr/www/sublist_356.html"));
            al.add(idx++, getNewsInfo("National - Foreign Affairs","120","http://www.koreatimes.co.kr/www/sublist_120.html"));
            al.add(idx++, getNewsInfo("National - Embassy News","176","http://www.koreatimes.co.kr/www/sublist_176.html"));
            al.add(idx++, getNewsInfo("National - Defense Affairs","205","http://www.koreatimes.co.kr/www/sublist_205.html"));
            al.add(idx++, getNewsInfo("National - Foreign Communities","177","http://www.koreatimes.co.kr/www/sublist_177.html"));
            al.add(idx++, getNewsInfo("National - Investigations","251","http://www.koreatimes.co.kr/www/sublist_251.html"));
            al.add(idx++, getNewsInfo("National - Diseases & welfare","119","http://www.koreatimes.co.kr/www/sublist_119.html"));
            al.add(idx++, getNewsInfo("National - Labor & environment","371","http://www.koreatimes.co.kr/www/sublist_371.html"));
            al.add(idx++, getNewsInfo("National - Education","181","http://www.koreatimes.co.kr/www/sublist_181.html"));
            al.add(idx++, getNewsInfo("National - Seoul & provinces","281","http://www.koreatimes.co.kr/www/sublist_281.html"));
            al.add(idx++, getNewsInfo("National - Obituaries","121","http://www.koreatimes.co.kr/www/sublist_121.html"));

            al.add(idx++, getNewsInfo("Culture - Books","142","http://www.koreatimes.co.kr/www/sublist_142.html"));
            al.add(idx++, getNewsInfo("Culture - Religions","293","http://www.koreatimes.co.kr/www/sublist_293.html"));
            al.add(idx++, getNewsInfo("Culture - Healthcare","641","http://www.koreatimes.co.kr/www/sublist_641.html"));
            al.add(idx++, getNewsInfo("Culture - Food","201","http://www.koreatimes.co.kr/www/sublist_201.html"));
            al.add(idx++, getNewsInfo("Culture - Fortune Telling","148","http://www.koreatimes.co.kr/www/sublist_148.html"));
            al.add(idx++, getNewsInfo("Culture - Hotel & Travel","141","http://www.koreatimes.co.kr/www/sublist_141.html"));
            al.add(idx++, getNewsInfo("Culture - Fashion","199","http://www.koreatimes.co.kr/www/sublist_199.html"));
            al.add(idx++, getNewsInfo("Culture - Korean traditions","317","http://www.koreatimes.co.kr/www/sublist_317.html"));
            al.add(idx++, getNewsInfo("Culture - Trend","703","http://www.koreatimes.co.kr/www/sublist_703.html"));

            al.add(idx++, getNewsInfo("Sports - Football","661","http://www.koreatimes.co.kr/www/sublist_661.html"));
            al.add(idx++, getNewsInfo("Sports - Baseball","662","http://www.koreatimes.co.kr/www/sublist_662.html"));
            al.add(idx++, getNewsInfo("Sports - Golf","159","http://www.koreatimes.co.kr/www/sublist_159.html"));
            al.add(idx++, getNewsInfo("Sports - Other Sports","663","http://www.koreatimes.co.kr/www/sublist_663.html"));

            al.add(idx++, getNewsInfo("World - SCMP","672","http://www.koreatimes.co.kr/www/sublist_672.html"));
            al.add(idx++, getNewsInfo("World - Asia Pacific","683","http://www.koreatimes.co.kr/www/sublist_683.html"));
            al.add(idx++, getNewsInfo("World - Americas","684","http://www.koreatimes.co.kr/www/sublist_684.html"));
            al.add(idx++, getNewsInfo("World - Europe","685","http://www.koreatimes.co.kr/www/sublist_685.html"));

            al.add(idx++, getNewsInfo("Columnists - Park Moo-jong","636","http://www.koreatimes.co.kr/www/sublist_636.html"));
            al.add(idx++, getNewsInfo("Columnists - Choi Sung-jin","673","http://www.koreatimes.co.kr/www/sublist_673.html"));
            al.add(idx++, getNewsInfo("Columnists - Tong Kim","167","http://www.koreatimes.co.kr/www/sublist_167.html"));
            al.add(idx++, getNewsInfo("Columnists - Lee Seong-hyon","674","http://www.koreatimes.co.kr/www/sublist_674.html"));
            al.add(idx++, getNewsInfo("Columnists - Andrew Salmon","351","http://www.koreatimes.co.kr/www/sublist_351.html"));
            al.add(idx++, getNewsInfo("Columnists - John Burton","396","http://www.koreatimes.co.kr/www/sublist_396.html"));
            al.add(idx++, getNewsInfo("Columnists - Jason Lim","352","http://www.koreatimes.co.kr/www/sublist_352.html"));
            al.add(idx++, getNewsInfo("Columnists - Donald Kirk","353","http://www.koreatimes.co.kr/www/sublist_353.html"));
            al.add(idx++, getNewsInfo("Columnists - Kim Ji-myung","355","http://www.koreatimes.co.kr/www/sublist_355.html"));
            al.add(idx++, getNewsInfo("Columnists - Andrei Lankov","304","http://www.koreatimes.co.kr/www/sublist_304.html"));
            al.add(idx++, getNewsInfo("Columnists - Michael Breen","170","http://www.koreatimes.co.kr/www/sublist_170.html"));
            al.add(idx++, getNewsInfo("Columnists - Frank Ching","171","http://www.koreatimes.co.kr/www/sublist_171.html"));
            al.add(idx++, getNewsInfo("Columnists - Hyon O'Brien","256","http://www.koreatimes.co.kr/www/sublist_256.html"));
            al.add(idx++, getNewsInfo("Columnists - Younghoy Kim Kimaro","614","http://www.koreatimes.co.kr/www/sublist_614.html"));
            al.add(idx++, getNewsInfo("Columnists - Michael McManus","620","http://www.koreatimes.co.kr/www/sublist_620.html"));
            al.add(idx++, getNewsInfo("Columnists - Deauwand Myers","621","http://www.koreatimes.co.kr/www/sublist_621.html"));
            al.add(idx++, getNewsInfo("Columnists - Bernard Rowan","625","http://www.koreatimes.co.kr/www/sublist_625.html"));
            al.add(idx++, getNewsInfo("Columnists - Casey Lartigue, Jr.","626","http://www.koreatimes.co.kr/www/sublist_626.html"));
            al.add(idx++, getNewsInfo("Columnists - Stephen Costello","637","http://www.koreatimes.co.kr/www/sublist_637.html"));
            al.add(idx++, getNewsInfo("Columnists - Semoon Chang","652","http://www.koreatimes.co.kr/www/sublist_652.html"));
            al.add(idx++, getNewsInfo("Columnists - Korean Historical Sense","633","http://www.koreatimes.co.kr/www/sublist_633.html"));
        } else if ( newsCode.equals(CommConstants.news_reuters)) {
            int cIdx = 1;

            al.add(idx++, getNewsInfo("Business Home",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/finance"));
            al.add(idx++, getNewsInfo("Business - Legal",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/legal"));
            al.add(idx++, getNewsInfo("Business - Deal",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/finance/deals"));
            al.add(idx++, getNewsInfo("Business - Aerospace & Defense",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/subjects/aerospace-and-defense"));
            al.add(idx++, getNewsInfo("Business - DATA DIVE",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/data-dive"));
            al.add(idx++, getNewsInfo("Business - Finance",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/subjects/banks"));
            al.add(idx++, getNewsInfo("Business - Autos",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/subjects/autos"));
            al.add(idx++, getNewsInfo("Business - Adventures",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/news/subjects/ADventures"));

            al.add(idx++, getNewsInfo("Markets Home",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/finance/markets"));
            al.add(idx++, getNewsInfo("Markets - U.S.",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/finance/markets/us"));
            al.add(idx++, getNewsInfo("Markets - European",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/finance/markets/europe"));
            al.add(idx++, getNewsInfo("Markets - Asian",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/finance/markets/asia"));
            al.add(idx++, getNewsInfo("Markets - Commodities",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/finance/commodities"));
            al.add(idx++, getNewsInfo("Markets - Earnings",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/finance/EarningsUS"));
            al.add(idx++, getNewsInfo("Markets - Bonds",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/finance/bonds"));

            al.add(idx++, getNewsInfo("World Home",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/news/world"));
            al.add(idx++, getNewsInfo("World - U.S.",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/news/us"));
            al.add(idx++, getNewsInfo("World - Special Reports",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/subjects/specialReports"));
            al.add(idx++, getNewsInfo("World - Mexico",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/places/mexico"));
            al.add(idx++, getNewsInfo("World - Brazil",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/places/brazil"));
            al.add(idx++, getNewsInfo("World - Africa",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/places/africa"));
            al.add(idx++, getNewsInfo("World - Russia",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/places/russia"));
            al.add(idx++, getNewsInfo("World - Euro Zone",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/subjects/euro-zone"));
            al.add(idx++, getNewsInfo("World - Middle East and North Africa",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/subjects/middle-east"));
            al.add(idx++, getNewsInfo("World - China",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/places/china"));
            al.add(idx++, getNewsInfo("World - Japan",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/places/japan"));
            al.add(idx++, getNewsInfo("World - India",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/places/india"));

            al.add(idx++, getNewsInfo("Politics Home",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/politics"));
            al.add(idx++, getNewsInfo("Politics - Supreme Court",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/subjects/supreme-court"));

            al.add(idx++, getNewsInfo("Technology Home",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/news/technology"));
            al.add(idx++, getNewsInfo("Technology - Science",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/news/science"));
            al.add(idx++, getNewsInfo("Technology - Media",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/news/media"));
            al.add(idx++, getNewsInfo("Technology - Energy And Environment",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/energy-environment"));
            al.add(idx++, getNewsInfo("Technology - Innovation & Intellectual Property",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/innovation"));

            al.add(idx++, getNewsInfo("Commentary",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/commentary"));

            al.add(idx++, getNewsInfo("Breakingviews",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/breakingviews"));

            al.add(idx++, getNewsInfo("Money Home",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/finance/personal-finance"));
            al.add(idx++, getNewsInfo("Money - Retirement",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/finance/personal-finance/retirement"));

            al.add(idx++, getNewsInfo("Lifestyle Home",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/news/lifestyle"));
            al.add(idx++, getNewsInfo("Lifestyle - Health",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/news/health"));
            al.add(idx++, getNewsInfo("Lifestyle - Sports",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/news/sports"));
            al.add(idx++, getNewsInfo("Lifestyle - Arts",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/news/entertainment/arts"));
            al.add(idx++, getNewsInfo("Lifestyle - Entertainment",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/news/entertainment"));
            al.add(idx++, getNewsInfo("Lifestyle - Oddly Enough",CommConstants.news_reuters + "_" + cIdx++,"http://www.reuters.com/news/oddlyEnough"));
        } else if ( newsCode.equals(CommConstants.news_abcNews)) {
            int cIdx = 1;

            al.add(idx++, getNewsInfo("U.S.",CommConstants.news_abcNews + "_" + cIdx++,"http://abcnews.go.com/US"));
            al.add(idx++, getNewsInfo("International",CommConstants.news_abcNews + "_" + cIdx++,"http://abcnews.go.com/International"));
            al.add(idx++, getNewsInfo("Politics",CommConstants.news_abcNews + "_" + cIdx++,"http://abcnews.go.com/Politics"));
            al.add(idx++, getNewsInfo("Lifestyle",CommConstants.news_abcNews + "_" + cIdx++,"http://abcnews.go.com/Lifestyle"));
            al.add(idx++, getNewsInfo("Entertainment",CommConstants.news_abcNews + "_" + cIdx++,"http://abcnews.go.com/Entertainment"));
            al.add(idx++, getNewsInfo("Health",CommConstants.news_abcNews + "_" + cIdx++,"http://abcnews.go.com/Health"));
            al.add(idx++, getNewsInfo("Technology",CommConstants.news_abcNews + "_" + cIdx++,"http://abcnews.go.com/Technology"));
        } else if ( newsCode.equals(CommConstants.news_bbcNews)) {
            int cIdx = 1;

            al.add(idx++, getNewsInfo("World Home",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/world/rss.xml"));
            al.add(idx++, getNewsInfo("Africa",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/world/africa/rss.xml"));
            al.add(idx++, getNewsInfo("Australia",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/world/australia/rss.xml"));
            al.add(idx++, getNewsInfo("Europe",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/world/europe/rss.xml"));
            al.add(idx++, getNewsInfo("Latin America",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/world/latin_america/rss.xml"));
            al.add(idx++, getNewsInfo("Middle East",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/world/middle_east/rss.xml"));
            al.add(idx++, getNewsInfo("US & Canada",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml"));

            al.add(idx++, getNewsInfo("Asia",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/world/asia/rss.xml"));
            al.add(idx++, getNewsInfo("China",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/world/asia/china/rss.xml"));
            al.add(idx++, getNewsInfo("India",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/world/asia/india/rss.xml"));

            al.add(idx++, getNewsInfo("Uk",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/uk/rss.xml"));
            al.add(idx++, getNewsInfo("England",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/england/rss.xml"));
            al.add(idx++, getNewsInfo("N.Ireland",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/northern_ireland/rss.xml"));
            al.add(idx++, getNewsInfo("Scotland",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/scotland/rss.xml"));
            al.add(idx++, getNewsInfo("Edinburgh, Fife & East",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/scotland/edinburgh_east_and_fife/rss.xml"));
            al.add(idx++, getNewsInfo("Glasgow & West",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/scotland/glasgow_and_west/rss.xml"));
            al.add(idx++, getNewsInfo("Highlands & Islands",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/scotland/highlands_and_islands/rss.xml"));
            al.add(idx++, getNewsInfo("NE, Orkney & Shetland",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/scotland/north_east_orkney_and_shetland/rss.xml"));
            al.add(idx++, getNewsInfo("South",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/scotland/south_scotland/rss.xml"));
            al.add(idx++, getNewsInfo("Tayside & Central",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/scotland/tayside_and_central/rss.xml"));

            al.add(idx++, getNewsInfo("Wales",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/wales/rss.xml"));
            al.add(idx++, getNewsInfo("Wales Politics",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/wales/wales_politics/rss.xml"));
            al.add(idx++, getNewsInfo("Wales North West",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/wales/north_west_wales/rss.xml"));
            al.add(idx++, getNewsInfo("Wales North East",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/wales/north_east_wales/rss.xml"));
            al.add(idx++, getNewsInfo("Wales Mid",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/wales/mid_wales/rss.xml"));
            al.add(idx++, getNewsInfo("Wales South West",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/wales/south_west_wales/rss.xml"));
            al.add(idx++, getNewsInfo("Wales South East",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/wales/south_east_wales/rss.xml"));
            al.add(idx++, getNewsInfo("Politics",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/politics/rss.xml"));
            al.add(idx++, getNewsInfo("Brexit",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/politics/uk_leaves_the_eu/rss.xml"));

            al.add(idx++, getNewsInfo("Business",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/business/rss.xml"));
            al.add(idx++, getNewsInfo("Companies",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/business/companies/rss.xml"));
            al.add(idx++, getNewsInfo("Technology",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/technology/rss.xml"));
            al.add(idx++, getNewsInfo("Magazine",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/magazine/rss.xml"));
            al.add(idx++, getNewsInfo("Entertainment & Arts",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/entertainment_and_arts/rss.xml"));
            al.add(idx++, getNewsInfo("Health",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/health/rss.xml"));
            al.add(idx++, getNewsInfo("Politics",CommConstants.news_bbcNews + "_" + cIdx++,"http://feeds.bbci.co.uk/news/politics/rss.xml"));
        } else if ( newsCode.equals(CommConstants.news_cnn)) {
            int cIdx = 1;

            al.add(idx++, getNewsInfo("Regions - U.S.",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/us"));
            al.add(idx++, getNewsInfo("Regions - Africa",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/africa"));
            al.add(idx++, getNewsInfo("Regions - Americas",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/americas"));
            al.add(idx++, getNewsInfo("Regions - Asia",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/asia"));
            al.add(idx++, getNewsInfo("Regions - China",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/china"));
            al.add(idx++, getNewsInfo("Regions - Europe",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/europe"));
            al.add(idx++, getNewsInfo("Regions - Middle East",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/middle-east"));
            al.add(idx++, getNewsInfo("Regions - Opinion",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/opinions"));

            al.add(idx++, getNewsInfo("U.S. Politics - 45",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/specials/politics/president-donald-trump-45"));
            al.add(idx++, getNewsInfo("U.S. Politics - Congress",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/specials/politics/congress-capitol-hill"));
            al.add(idx++, getNewsInfo("U.S. Politics - Security",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/specials/politics/us-security"));
            al.add(idx++, getNewsInfo("U.S. Politics - The Nine",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/specials/politics/supreme-court-nine"));
            al.add(idx++, getNewsInfo("U.S. Politics - Trumpmerica",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/specials/politics/trumpmerica"));
            al.add(idx++, getNewsInfo("U.S. Politics - State",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/specials/politics/state-cnn-politics-magazine"));

            al.add(idx++, getNewsInfo("Entertainment - Stars",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/entertainment/celebrities"));
            al.add(idx++, getNewsInfo("Entertainment - Screen",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/entertainment/movies"));
            al.add(idx++, getNewsInfo("Entertainment - Binge",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/entertainment/tv-shows"));
            al.add(idx++, getNewsInfo("Entertainment - Culture",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/entertainment/culture"));

            al.add(idx++, getNewsInfo("Sport - Football",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/sport/football"));
            al.add(idx++, getNewsInfo("Sport - Golf",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/sport/golf"));
            al.add(idx++, getNewsInfo("Sport - Tennis",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/sport/tennis"));
            al.add(idx++, getNewsInfo("Sport - Motorsport",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/sport/motorsport"));
            al.add(idx++, getNewsInfo("Sport - Horseracing",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/sport/horse-racing"));
            al.add(idx++, getNewsInfo("Sport - Equestrian",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/sport/equestrian"));
            al.add(idx++, getNewsInfo("Sport - Sailing",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/sport/sailing"));
            al.add(idx++, getNewsInfo("Sport - Skiing",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/sport/skiing"));

            al.add(idx++, getNewsInfo("Style - Fashion",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/style/fashion"));
            al.add(idx++, getNewsInfo("Style - Design",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/style/design"));
            al.add(idx++, getNewsInfo("Style - Architecture",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/style/architecture"));
            al.add(idx++, getNewsInfo("Style - Arts",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/style/arts"));
            al.add(idx++, getNewsInfo("Style - Autos",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/style/autos"));
            al.add(idx++, getNewsInfo("Style - Luxury",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/style/luxury"));

            al.add(idx++, getNewsInfo("Health - Diet + Fitness",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/specials/health/diet-fitness"));
            al.add(idx++, getNewsInfo("Health - Living Well",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/specials/health/living-well"));
            al.add(idx++, getNewsInfo("Health - Parenting + Family",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/specials/living/cnn-parents"));
            al.add(idx++, getNewsInfo("Health - Vital Signs",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/specials/health/vital-signs"));

            al.add(idx++, getNewsInfo("Features - Freedom Project",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/specials/world/freedom-project"));
            al.add(idx++, getNewsInfo("Features - Impact Your World",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/specials/impact-your-world"));
            al.add(idx++, getNewsInfo("Features - Inside Africa",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/specials/africa/inside-africa"));
            al.add(idx++, getNewsInfo("Features - 2 degrees",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/specials/opinions/two-degrees"));
            al.add(idx++, getNewsInfo("Features - CNN Heroes",CommConstants.news_losangeles + "_" + cIdx++,"http://edition.cnn.com/specials/cnn-heroes"));
        } else if ( newsCode.equals(CommConstants.news_losangeles)) {
            int cIdx = 1;

            al.add(idx++, getNewsInfo("California & Local",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/local/"));
            al.add(idx++, getNewsInfo("L.A. Now",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/local/lanow/"));
            al.add(idx++, getNewsInfo("California",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/local/california/"));
            al.add(idx++, getNewsInfo("Orange County",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/local/orangecounty/"));
            al.add(idx++, getNewsInfo("Education",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/local/education/la-essential-education-updates-southern-california-2017-htmlstory.html"));
            al.add(idx++, getNewsInfo("Crime",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/local/crime/"));

            al.add(idx++, getNewsInfo("Arts & Culture",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/entertainment/arts/"));
            al.add(idx++, getNewsInfo("Company Town",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/entertainment/envelope/cotown/"));
            al.add(idx++, getNewsInfo("Gossip",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/entertainment/gossip/"));
            al.add(idx++, getNewsInfo("Hero Complex",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/entertainment/herocomplex/"));
            al.add(idx++, getNewsInfo("Movies",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/entertainment/movies/"));
            al.add(idx++, getNewsInfo("Music",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/entertainment/music/"));
            al.add(idx++, getNewsInfo("Television",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/entertainment/tv/"));
            al.add(idx++, getNewsInfo("The Envelope",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/entertainment/envelope/"));

            al.add(idx++, getNewsInfo("Sports - Sports Now",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/sports/sportsnow/"));
            al.add(idx++, getNewsInfo("Sports - Dodgers",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/sports/dodgers/"));
            al.add(idx++, getNewsInfo("Sports - Angels",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/sports/angels/"));
            al.add(idx++, getNewsInfo("Sports - Lakers",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/sports/lakers/"));
            al.add(idx++, getNewsInfo("Sports - Clippers",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/sports/clippers/"));
            al.add(idx++, getNewsInfo("Sports - Rams",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/sports/rams/"));
            al.add(idx++, getNewsInfo("Sports - Chargers",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/sports/chargers/"));
            al.add(idx++, getNewsInfo("Sports - UCLA",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/sports/ucla/"));
            al.add(idx++, getNewsInfo("Sports - USC",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/sports/usc/"));
            al.add(idx++, getNewsInfo("Sports - High School",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/sports/highschool/"));
            al.add(idx++, getNewsInfo("Sports - Kings/Ducks",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/sports/hockey/"));
            al.add(idx++, getNewsInfo("Sports - NHL / Ducks",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/sports/ducks/"));

            al.add(idx++, getNewsInfo("Business",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/entertainment/envelope/cotown/"));

            al.add(idx++, getNewsInfo("Technology",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/business/technology/"));
            al.add(idx++, getNewsInfo("State Politics",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/politics/"));
            al.add(idx++, getNewsInfo("L.A. City Hall",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/local/cityhall/"));

            al.add(idx++, getNewsInfo("World - Africa",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/world/africa/"));
            al.add(idx++, getNewsInfo("World - Americas",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/world/mexico-americas/"));
            al.add(idx++, getNewsInfo("World - Asia",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/world/asia/"));
            al.add(idx++, getNewsInfo("World - Europe",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/world/europe/"));
            al.add(idx++, getNewsInfo("World - Middle East",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/world/middleeast/"));
            al.add(idx++, getNewsInfo("World - Development",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/world/global-development/"));

            al.add(idx++, getNewsInfo("Opinion",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/opinion/"));
            al.add(idx++, getNewsInfo("Opinion L.A.",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/opinion/opinion-la/"));
            al.add(idx++, getNewsInfo("David Horsey",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/opinion/topoftheticket/"));
            al.add(idx++, getNewsInfo("Editorials",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/opinion/editorials/"));
            al.add(idx++, getNewsInfo("Op-Ed",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/opinion/op-ed/"));
            al.add(idx++, getNewsInfo("Readers React",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/opinion/readersreact/"));
            al.add(idx++, getNewsInfo("Readers Rep",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/local/readers-rep/"));

            al.add(idx++, getNewsInfo("Nation - Environment",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/nation/la-na-energy-environment-sg-storygallery.html"));
            al.add(idx++, getNewsInfo("Nation - Development",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/world/global-development/"));
            al.add(idx++, getNewsInfo("Nation - Immigration",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/nation/immigration/"));

            al.add(idx++, getNewsInfo("Obituaries - California Lives",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/local/obituaries/archives/"));

            al.add(idx++, getNewsInfo("Travel - Deals & News",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/travel/deals/"));
            al.add(idx++, getNewsInfo("Travel - Calif. & West",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/travel/california/"));
            al.add(idx++, getNewsInfo("Travel - Europe",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/travel/europe/"));
            al.add(idx++, getNewsInfo("Travel - Hawaii",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/travel/hawaii/"));
            al.add(idx++, getNewsInfo("Travel - Las Vegas",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/travel/lasvegas/"));
            al.add(idx++, getNewsInfo("Travel - Cruises",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/travel/cruises/"));
            al.add(idx++, getNewsInfo("Travel - Mexico",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/travel/mexico/"));
            al.add(idx++, getNewsInfo("Travel - Asia",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/travel/asia/"));
            al.add(idx++, getNewsInfo("Travel - Theme Parks",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/travel/themeparks/"));

            al.add(idx++, getNewsInfo("Books",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/books/"));
            al.add(idx++, getNewsInfo("Fashion",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/fashion/"));
            al.add(idx++, getNewsInfo("Health",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/health/"));
            al.add(idx++, getNewsInfo("Home & Garden",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/home/"));
            al.add(idx++, getNewsInfo("L.A. Affairs",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/style/laaffairs/"));

            al.add(idx++, getNewsInfo("Science Now",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/science/sciencenow/"));

            al.add(idx++, getNewsInfo("Autos",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/business/autos/"));
            al.add(idx++, getNewsInfo("Autos - Reviews",CommConstants.news_losangeles + "_" + cIdx++,"http://www.latimes.com/business/autos/reviews/"));
        } else if ( newsCode.equals(CommConstants.news_newWorkTimes)) {
            int cIdx = 1;

            al.add(idx++, getNewsInfo("News - World",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/world/index.html"));
            al.add(idx++, getNewsInfo("News - U.S.",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/national/index.html"));
            al.add(idx++, getNewsInfo("News - Politics",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/politics/index.html"));
            al.add(idx++, getNewsInfo("News - N.Y.",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/nyregion/index.html"));
            al.add(idx++, getNewsInfo("News - Business",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/business/index.html"));
            al.add(idx++, getNewsInfo("News - Tech",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/technology/index.html"));
            al.add(idx++, getNewsInfo("News - Science",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/section/science"));
            al.add(idx++, getNewsInfo("News - Health",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/health/index.html"));
            al.add(idx++, getNewsInfo("News - Sports",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/sports/index.html"));
            al.add(idx++, getNewsInfo("News - Education",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/education/index.html"));
            al.add(idx++, getNewsInfo("News - Obituaries",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/obituaries/index.html"));
            al.add(idx++, getNewsInfo("News - Today's Paper",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/todayspaper/index.html"));
            al.add(idx++, getNewsInfo("News - Corrections",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/corrections/index.html"));

            al.add(idx++, getNewsInfo("Today's Opinion",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/opinion/index.html?module=SiteIndex&region=Footer&pgtype=sectionfront"));

            al.add(idx++, getNewsInfo("Arts - Today's Arts",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/arts/index.html"));
            al.add(idx++, getNewsInfo("Arts - Art Design",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/arts/design/index.html"));
            al.add(idx++, getNewsInfo("Arts - Books",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/books/index.html"));
            al.add(idx++, getNewsInfo("Arts - Dance",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/arts/dance/index.html"));
            al.add(idx++, getNewsInfo("Arts - Movies",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/movies/index.html"));
            al.add(idx++, getNewsInfo("Arts - Music",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/arts/music/index.html"));
            al.add(idx++, getNewsInfo("Arts - N.Y.C. Events Guide",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/events/"));
            al.add(idx++, getNewsInfo("Arts - Television",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/arts/television/index.html"));
            al.add(idx++, getNewsInfo("Arts - Theater",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/theater/index.html"));

            al.add(idx++, getNewsInfo("Living - Automobiles",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/automobiles/index.html"));
            al.add(idx++, getNewsInfo("Living - Crossword",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/crosswords/"));
            al.add(idx++, getNewsInfo("Living - Food",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/dining/index.html"));
            al.add(idx++, getNewsInfo("Living - Education",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/education/index.html"));
            al.add(idx++, getNewsInfo("Living - Fashion  Style",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/fashion/index.html"));
            al.add(idx++, getNewsInfo("Living - Health",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/health/index.html"));
            al.add(idx++, getNewsInfo("Living - Jobs",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/section/jobs"));
            al.add(idx++, getNewsInfo("Living - Magazine",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/magazine/index.html"));
            al.add(idx++, getNewsInfo("Living - N.Y.C. Events Guide",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/events/"));
            al.add(idx++, getNewsInfo("Living - Real Estate",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/section/realestate"));
            al.add(idx++, getNewsInfo("Living - T Magazine",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/section/t-magazine"));
            al.add(idx++, getNewsInfo("Living - Travel",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/section/travel"));
            al.add(idx++, getNewsInfo("Living - Weddings  Celebrations",CommConstants.news_newWorkTimes + "_" + cIdx++,"https://www.nytimes.com/pages/fashion/weddings/index.html"));
        } else if ( newsCode.equals(CommConstants.news_washingtonePost)) {
            int cIdx = 1;

            al.add(idx++, getNewsInfo("Politics - PowerPost ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/news/powerpost/"));
            al.add(idx++, getNewsInfo("Politics - The Fix ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/blogs/the-fix/"));
            al.add(idx++, getNewsInfo("Politics - White House ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/politics/white-house/"));
            al.add(idx++, getNewsInfo("Politics - Courts and Law ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/politics/courts-law/"));
            al.add(idx++, getNewsInfo("Politics - Polling ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/politics/polling/"));
            al.add(idx++, getNewsInfo("Politics - Monkey Cage ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/blogs/monkey-cage/"));
            al.add(idx++, getNewsInfo("Politics - Fact Checker ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/blogs/fact-checker/"));
            al.add(idx++, getNewsInfo("Politics - Post Politics Blog ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/blogs/post-politics/"));

            al.add(idx++, getNewsInfo("Opinions - The Post's View ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/opinions/the-posts-view/"));
            al.add(idx++, getNewsInfo("Opinions - Toles Cartoons ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/people/tom-toles"));
            al.add(idx++, getNewsInfo("Opinions - Telnaes Animations ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/people/ann-telnaes"));
            al.add(idx++, getNewsInfo("Opinions - Local Opinions ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/opinions/local-opinions/"));
            al.add(idx++, getNewsInfo("Opinions - Global Opinions ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/global-opinions/"));
            al.add(idx++, getNewsInfo("Opinions - Letters to the Editor ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/opinions/letters-to-the-editor/"));
            al.add(idx++, getNewsInfo("Opinions - Act Four ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/act-four/"));
            al.add(idx++, getNewsInfo("Opinions - All Opinions Are Local ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/blogs/all-opinions-are-local/"));
            al.add(idx++, getNewsInfo("Opinions - Book Party ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/book-party/"));
            al.add(idx++, getNewsInfo("Opinions - Compost ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/blogs/compost/"));
            al.add(idx++, getNewsInfo("Opinions - Erik Wemple ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/blogs/erik-wemple/"));
            al.add(idx++, getNewsInfo("Opinions - In Theory ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/news/in-theory/"));
            al.add(idx++, getNewsInfo("Opinions - The Plum Line ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/blogs/plum-line/"));
            al.add(idx++, getNewsInfo("Opinions - PostPartisan ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/blogs/post-partisan/"));
            al.add(idx++, getNewsInfo("Opinions - Rampage ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/rampage/"));
            al.add(idx++, getNewsInfo("Opinions - Right Turn ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/blogs/right-turn/"));
            al.add(idx++, getNewsInfo("Opinions - The Watch ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/the-watch/"));
            al.add(idx++, getNewsInfo("Opinions - Volokh Conspiracy ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/volokh-conspiracy/"));
            al.add(idx++, getNewsInfo("Opinions - DemocracyPost",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/democracy-post/"));

            al.add(idx++, getNewsInfo("Sports - Redskins ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/sports/redskins/"));
            al.add(idx++, getNewsInfo("Sports - NFL ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/sports/nfl/"));
            al.add(idx++, getNewsInfo("Sports - MLB ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/sports/mlb/"));
            al.add(idx++, getNewsInfo("Sports - NBA ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/sports/nba/"));
            al.add(idx++, getNewsInfo("Sports - NHL ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/sports/nhl/"));
            al.add(idx++, getNewsInfo("Sports - AllMetSports ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/allmetsports/"));
            al.add(idx++, getNewsInfo("Sports - Soccer ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/sports/soccer/"));
            al.add(idx++, getNewsInfo("Sports - Boxing/MMA ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/sports/boxing-mma/"));
            al.add(idx++, getNewsInfo("Sports - College Sports ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/sports/colleges/"));
            al.add(idx++, getNewsInfo("Sports - College Football ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/sports/colleges/football/"));
            al.add(idx++, getNewsInfo("Sports - College Basketball ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/sports/colleges/basketball/"));
            al.add(idx++, getNewsInfo("Sports - D.C. Sports Bog ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/blogs/dc-sports-bog/"));
            al.add(idx++, getNewsInfo("Sports - Early Lead ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/blogs/early-lead/"));
            al.add(idx++, getNewsInfo("Sports - Fancy Stats ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/fancy-stats/"));
            al.add(idx++, getNewsInfo("Sports - Golf ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/sports/golf/"));
            al.add(idx++, getNewsInfo("Sports - Tennis ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/sports/tennis/"));
            al.add(idx++, getNewsInfo("Sports - Fantasy Sports",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/sports/fantasy-sports/"));

            al.add(idx++, getNewsInfo("Local - D.C. ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/local/dc/"));
            al.add(idx++, getNewsInfo("Local - Maryland ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/local/maryland/"));
            al.add(idx++, getNewsInfo("Local - Virginia ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/local/virginia/"));
            al.add(idx++, getNewsInfo("Local - Public Safety ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/local/public-safety/"));
            al.add(idx++, getNewsInfo("Local - Education ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/local/education/"));
            al.add(idx++, getNewsInfo("Local - Obituaries ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/local/obituaries/"));
            al.add(idx++, getNewsInfo("Local - Transportation ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/local/traffic-commuting/"));
            al.add(idx++, getNewsInfo("Local - Weather ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/local/weather/"));
            al.add(idx++, getNewsInfo("Local - Retropolis",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/retropolis/"));

            al.add(idx++, getNewsInfo("National - Acts of Faith ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/acts-of-faith/"));
            al.add(idx++, getNewsInfo("National - Health and Science ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/national/health-science/"));
            al.add(idx++, getNewsInfo("National - National Security ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/world/national-security/"));
            al.add(idx++, getNewsInfo("National - Investigations ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/national/investigations/"));
            al.add(idx++, getNewsInfo("National - Morning Mix ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/morning-mix/"));
            al.add(idx++, getNewsInfo("National - Post Nation ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/post-nation/"));
            al.add(idx++, getNewsInfo("National - True Crime ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/true-crime/"));
            al.add(idx++, getNewsInfo("National - Obituaries",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/local/obituaries/"));

            al.add(idx++, getNewsInfo("World - Africa ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/world/africa/"));
            al.add(idx++, getNewsInfo("World - The Americas ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/world/americas/"));
            al.add(idx++, getNewsInfo("World - Asia and Pacific ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/world/asia-pacific/"));
            al.add(idx++, getNewsInfo("World - Europe ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/world/europe/"));
            al.add(idx++, getNewsInfo("World - Middle East ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/world/middle-east/"));
            al.add(idx++, getNewsInfo("World - National Security ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/world/national-security/"));
            al.add(idx++, getNewsInfo("World - WorldViews ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/blogs/worldviews/"));
            al.add(idx++, getNewsInfo("World - Checkpoint",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/checkpoint/"));

            al.add(idx++, getNewsInfo("Business - Wonkblog ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/news/wonk/"));
            al.add(idx++, getNewsInfo("Business - On Leadership ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/business/on-leadership/"));
            al.add(idx++, getNewsInfo("Business - Personal Finance ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/business/get-there/"));
            al.add(idx++, getNewsInfo("Business - Digger ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/digger/"));
            al.add(idx++, getNewsInfo("Business - Energy and Environment ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/news/energy-environment/"));
            al.add(idx++, getNewsInfo("Business - On Small Business ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/business/on-small-business/"));
            al.add(idx++, getNewsInfo("Business - Capital Business",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/business/capital-business/"));

            al.add(idx++, getNewsInfo("Tech - Innovations ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/innovations/"));
            al.add(idx++, getNewsInfo("Tech - The Switch ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/blogs/the-switch/"));

            al.add(idx++, getNewsInfo("Lifestyle - Arts and Entertainment ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/news/arts-and-entertainment/"));
            al.add(idx++, getNewsInfo("Lifestyle - Advice ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/lifestyle/advice/"));
            al.add(idx++, getNewsInfo("Lifestyle - Carolyn Hax ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/pb/people/carolyn-hax/"));
            al.add(idx++, getNewsInfo("Lifestyle - Food ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/food/"));
            al.add(idx++, getNewsInfo("Lifestyle - Travel ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/lifestyle/travel/"));
            al.add(idx++, getNewsInfo("Lifestyle - Wellness ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/lifestyle/wellness/"));
            al.add(idx++, getNewsInfo("Lifestyle - Magazine ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/lifestyle/magazine/"));
            al.add(idx++, getNewsInfo("Lifestyle - Home and Garden ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/lifestyle/home-garden/"));
            al.add(idx++, getNewsInfo("Lifestyle - Inspired Life ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/inspired-life/"));
            al.add(idx++, getNewsInfo("Lifestyle - Fashion ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/lifestyle/fashion/"));
            al.add(idx++, getNewsInfo("Lifestyle - KidsPost ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/lifestyle/kidspost/"));
            al.add(idx++, getNewsInfo("Lifestyle - On Parenting ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/lifestyle/on-parenting/"));
            al.add(idx++, getNewsInfo("Lifestyle - Reliable Source ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/blogs/reliable-source/"));
            al.add(idx++, getNewsInfo("Lifestyle - The Intersect ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/the-intersect/"));
            al.add(idx++, getNewsInfo("Lifestyle - Solo-ish",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/soloish/"));

            al.add(idx++, getNewsInfo("Entertainment - Books ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/entertainment/books/"));
            al.add(idx++, getNewsInfo("Entertainment - Comics ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/entertainment/comics/"));
            al.add(idx++, getNewsInfo("Entertainment - Comic Riffs ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/news/comic-riffs/"));
            al.add(idx++, getNewsInfo("Entertainment - Going Out Guide ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/goingoutguide/"));
            al.add(idx++, getNewsInfo("Entertainment - Horoscopes ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/entertainment/horoscopes/"));
            al.add(idx++, getNewsInfo("Entertainment - Movies ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/goingoutguide/movies/"));
            al.add(idx++, getNewsInfo("Entertainment - Museums ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/goingoutguide/museums/"));
            al.add(idx++, getNewsInfo("Entertainment - Music ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/goingoutguide/music/"));
            al.add(idx++, getNewsInfo("Entertainment - Theater and Dance ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/goingoutguide/theater-dance/"));
            al.add(idx++, getNewsInfo("Entertainment - TV ",CommConstants.news_washingtonePost + "_" + cIdx++,"http://www.washingtonpost.com/entertainment/tv/"));
            al.add(idx++, getNewsInfo("Entertainment - Restaurants ",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/goingoutguide/restaurants/"));
            al.add(idx++, getNewsInfo("Entertainment - Bars  Clubs",CommConstants.news_washingtonePost + "_" + cIdx++,"https://www.washingtonpost.com/goingoutguide/bars-clubs/"));
        }

        category = new String[al.size()];
        for ( int i = 0; i < al.size(); i++ ) {
            if ( "N".equals(kind) ) {
                category[i] = ((String[])al.get(i))[0];
            }else if ( "C".equals(kind) ) {
                category[i] = ((String[])al.get(i))[1];
            }else if ( "U".equals(kind) ) {
                category[i] = ((String[])al.get(i))[2];
            }
        }

        return category;
    }

    public static void getNewsCategoryNews(SQLiteDatabase db, String newsCode, String categoryCode, String url) {
        try {
            if ( newsCode.equals(CommConstants.news_KoreaJoongangDaily) ) {
                boolean isExistNews = false;
                for ( int page = 0; page < 2; page ++ ) {
                    Document doc = getDocument(url + (page > 0 ? "&pgi=" + (page + 1) : ""));
                    Elements es = doc.select("div#news_list div.bd ul li dl");
                    for (int i = 0; i < es.size(); i++) {
                        String newsTitle = "";
                        String newsUrl = "";
                        String newsDesc = "";

                        if (es.get(i).select("a.title_cr").size() > 0) {
                            newsTitle = es.get(i).select("a.title_cr").text();
                            newsUrl = "http://koreajoongangdaily.joins.com" + es.get(i).select("a.title_cr").attr("href");
                        }
                        if (es.get(i).select("a.read_cr").size() > 0) {
                            newsDesc = es.get(i).select("a.read_cr").text();
                        }

                        boolean exist = DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                        if (exist) {
                            isExistNews = true;
                            break;
                        }
                    }
                    if ( isExistNews ) {
                        break;
                    }
                }
            } else if ( newsCode.equals(CommConstants.news_TheChosunilbo)) {
                boolean isExistNews = false;
                for ( int page = 0; page < 2; page ++ ) {
                    Document doc = getDocument(url + (page > 0 ? "&pn=" + (page + 1) : ""));
                    Elements es = doc.select("div#list_area dl.list_item");
                    for (int i = 0; i < es.size(); i++) {
                        String newsTitle = "";
                        String newsUrl = "";
                        String newsDesc = "";

                        newsTitle = es.get(i).select("dt a").text();
                        newsUrl = "http://english.chosun.com" + es.get(i).select("dt a").attr("href");
                        newsDesc = es.get(i).select("dd.desc a").text();

                        boolean exist = DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                        if (exist) {
                            isExistNews = true;
                            break;
                        }
                    }
                    if ( isExistNews ) {
                        break;
                    }
                }
            } else if ( newsCode.equals(CommConstants.news_TheKoreaHerald)) {
                boolean isExistNews = false;
                for ( int page = 0; page < 2; page ++ ) {
                    Document doc = getDocument(url + (page > 0 ? "&pgi=" + (page + 1) : ""));
                    Elements es = doc.select("section.newslist dl dd");
                    for (int i = 0; i < es.size(); i++) {
                        String newsTitle = "";
                        String newsUrl = "";
                        String newsDesc = "";

                        if (es.get(i).select("details h3 a").size() > 0) {
                            newsTitle = es.get(i).select("details h3 a").text();
                            newsUrl = es.get(i).select("details h3 a").attr("href");
                        }
                        if (es.get(i).select("details p a").size() > 0) {
                            newsDesc = es.get(i).select("details p a").text();
                        }

                        boolean exist = DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                        if (exist) {
                            isExistNews = true;
                            break;
                        }
                    }
                    if ( isExistNews ) {
                        break;
                    }
                }
            } else if ( newsCode.equals(CommConstants.news_TheKoreaTimes)) {
                boolean isExistNews = false;
                for ( int page = 0; page < 2; page ++ ) {
                    Document doc;
                    if ( page > 0 ) {
                        doc = getDocument(url.substring(0, url.length() - 5) + "_" + (page + 1) + url.substring(url.length() - 5, url.length()));
                    } else {
                        doc = getDocument(url);
                    }

                    Elements es = doc.select("div.list_article_area");
                    for (int i = 0; i < es.size(); i++) {
                        String newsTitle = "";
                        String newsUrl = "";
                        String newsDesc = "";

                        if (es.get(i).select("div.list_article_headline a").size() > 0) {
                            newsTitle = es.get(i).select("div.list_article_headline a").text();
                            newsUrl = "http://www.koreatimes.co.kr" + es.get(i).select("div.list_article_headline a").attr("href");
                        }
                        if (es.get(i).select("div.list_article_lead a").size() > 0) {
                            newsDesc = es.get(i).select("div.list_article_lead a").text();
                        }

                        boolean exist = DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                        if (exist) {
                            isExistNews = true;
                            break;
                        }
                    }
                    if ( isExistNews ) {
                        break;
                    }
                }
            } else if ( newsCode.equals(CommConstants.news_reuters)) {
                Document doc = getDocument(url);
                String newsTitle = "";
                String newsUrl = "";
                String newsDesc = "";

                Elements es = doc.select("div.column1 div.moduleBody div.bigStory");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("h2 a").size() > 0) {
                        newsTitle = es.get(i).select("h2 a").text();
                        newsUrl = "http://www.reuters.com" + es.get(i).select("h2 a").attr("href");
                    }
                    if (es.get(i).select("p").size() > 0) {
                        newsDesc = es.get(i).select("p").text();
                    }

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }

                es = doc.select("div.column1 div.moduleBody div.topStory");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("h2 a").size() > 0) {
                        newsTitle = es.get(i).select("h2 a").text();
                        newsUrl = "http://www.reuters.com" + es.get(i).select("h2 a").attr("href");
                    }
                    if (es.get(i).select("p").size() > 0) {
                        newsDesc = es.get(i).select("p").text();
                    }

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }

                es = doc.select("div.column1 div.moduleBody div.feature");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("h2 a").size() > 0) {
                        newsTitle = es.get(i).select("h2 a").text();
                        newsUrl = "http://www.reuters.com" + es.get(i).select("h2 a").attr("href");
                    }
                    if (es.get(i).select("p").size() > 0) {
                        newsDesc = es.get(i).select("p").text();
                    }

                    if ( newsUrl.indexOf("video") > -1 || newsTitle.indexOf("More ") == 0 ) {
                        continue;
                    }

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }

                es = doc.select("div#moreSectionNews div.moduleBody div.topStory");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("h2 a").size() > 0) {
                        newsTitle = es.get(i).select("h2 a").text();
                        newsUrl = "http://www.reuters.com" + es.get(i).select("h2 a").attr("href");
                    }
                    if (es.get(i).select("p").size() > 0) {
                        newsDesc = es.get(i).select("p").text();
                    }

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }

                es = doc.select("div.column1 div.moduleBody ul li");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("a").size() > 0) {
                        newsTitle = es.get(i).select("a").text();
                        newsUrl = "http://www.reuters.com" + es.get(i).select("a").attr("href");
                    }
                    newsDesc = "";

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }

                es = doc.select("div.column2 div.moduleBody div.feature");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("h2 a").size() > 0) {
                        newsTitle = es.get(i).select("h2 a").text();
                        newsUrl = "http://www.reuters.com" + es.get(i).select("h2 a").attr("href");
                    }
                    if (es.get(i).select("p").size() > 0) {
                        newsDesc = es.get(i).select("p").text();
                    }

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }

                es = doc.select("div.column2 div.moduleBody ul li");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("a").size() > 0) {
                        newsTitle = es.get(i).select("a").text();
                        newsUrl = "http://www.reuters.com" + es.get(i).select("a").attr("href");
                    }
                    newsDesc = "";

                    if ( newsUrl.indexOf("video") > -1 || newsTitle.indexOf("More ") == 0 ) {
                        continue;
                    }

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }

                es = doc.select("div.column1 article.story");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("div.story-content a").size() > 0) {
                        newsTitle = es.get(i).select("div.story-content a").text();
                        newsUrl = "http://www.reuters.com" + es.get(i).select("div.story-content a").attr("href");
                    }
                    if (es.get(i).select("p").size() > 0) {
                        newsDesc = es.get(i).select("p").text();
                    }

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }
            } else if ( newsCode.equals(CommConstants.news_abcNews)) {
                Document doc = getDocument(url);
                String newsTitle = "";
                String newsUrl = "";
                String newsDesc = "";

                Elements es = doc.select("div.caption-wrapper");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("h1 a").size() > 0) {
                        newsTitle = es.get(i).select("h1 a").text();
                        newsUrl = es.get(i).select("h1 a").attr("href");
                    }
                    newsDesc = "";

                    //비디오 인경우
                    if ( newsUrl.indexOf("http") == -1 ) {
                        continue;
                    }

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }

                es = doc.select("article.headlines ul.headlines-ul li.headlines-li");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("h1 a").size() > 0) {
                        newsTitle = es.get(i).select("h1 a").text();
                        newsUrl = es.get(i).select("h1 a").attr("href");
                    }
                    newsDesc = "";

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }

                es = doc.select("article.ab-col div.tag-content ul li.headlines-li");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("h1 a").size() > 0) {
                        newsTitle = es.get(i).select("h1 a").text();
                        newsUrl = es.get(i).select("h1 a").attr("href");
                    }
                    if (es.get(i).select("h1 div.desc").size() > 0) {
                        newsDesc = es.get(i).select("h1 div.desc").text();
                    }

                    //비디오 인경우
                    if ( newsUrl.indexOf("http") == -1 ) {
                        continue;
                    }

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }
            } else if ( newsCode.equals(CommConstants.news_bbcNews)) {
                String newsTitle = "";
                String newsUrl = "";
                String newsDesc = "";

                try{
                    // XML 데이터를 읽어옴
                    URL urlObj = new URL(url);
                    InputStream in = urlObj.openStream();

                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();

                    // XmlPullParser에 XML 데이터와 인코딩 방식을 입력
                    parser.setInput(in, "euc-kr");

                    int eventType = parser.getEventType();
                    boolean isItemTag = false;
                    boolean isTagStart = false;
                    String tagName = "";
                    while ( eventType != XmlPullParser.END_DOCUMENT ) {
                        if ( eventType == XmlPullParser.START_TAG ) {
                            tagName = parser.getName();
                            if ( tagName.equals("item") ) {
                                isItemTag = true;
                            }
                            isTagStart = true;
                            dicLog(tagName + " start");
                        } else if ( eventType == XmlPullParser.TEXT && isItemTag && isTagStart ) {
                            dicLog(tagName + " text");
                            if(tagName.equals("title"))
                                newsTitle = parser.getText();

                            if(tagName.equals("link"))
                                newsUrl = parser.getText();

                            if(tagName.equals("description"))
                                newsDesc = parser.getText();
                            dicLog(newsTitle + " : " + newsUrl + " : " + newsDesc);
                        } else if(eventType == XmlPullParser.END_TAG){
                            tagName = parser.getName();
                            isTagStart = false;
                            dicLog(tagName + " end");
                            if(tagName.equals("item")){
                                DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);

                                newsTitle = "";
                                newsUrl = "";
                                newsDesc = "";
                                isItemTag = false;
                            }
                        }

                        eventType = parser.next();
                    }
                } catch(Exception e) {
                    dicLog(e.toString());
                }
            } else if ( newsCode.equals(CommConstants.news_cnn)) {
                Document doc = getDocument(url);
                String newsTitle = "";
                String newsUrl = "";
                String newsDesc = "";

                Elements es = doc.select("div.cd__content");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("h3 a").size() > 0) {
                        newsTitle = es.get(i).select("h3 a").text();
                        newsUrl = "http://edition.cnn.com" + es.get(i).select("h3 a").attr("href");
                    }
                    newsDesc = "";

                    //비디오 인경우
                    if ( newsUrl.indexOf("videos") > -1 ) {
                        continue;
                    }

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }
            } else if ( newsCode.equals(CommConstants.news_losangeles)) {
                Document doc = getDocument(url);
                String newsTitle = "";
                String newsUrl = "";
                String newsDesc = "";

                // Top 영역
                Elements es = doc.select("section.trb_outfit_primaryItem article");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("h2 a").size() > 0) {
                        newsTitle = es.get(i).select("h2 a").text();
                        newsUrl = "http://www.latimes.com" + es.get(i).select("h2 a").attr("href");
                    }
                    if (es.get(i).select("p.trb_outfit_primaryItem_article_content").size() > 0) {
                        newsDesc = es.get(i).select("p.trb_outfit_primaryItem_article_content").text();
                    }
                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }

                // 리스트 영역
                es = doc.select("section.trb_outfit_group ul li");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("h3 a").size() > 0) {
                        newsTitle = es.get(i).select("h3 a").text();
                        newsUrl = "http://www.latimes.com" + es.get(i).select("h3 a").attr("href");
                    }
                    if (es.get(i).select("p.trb_outfit_group_list_item_brief").size() > 0) {
                        newsDesc = es.get(i).select("p.trb_outfit_group_list_item_brief").text();
                    }

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }

                es = doc.select("div.trb_blogroll_post");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("div.trb_blogroll_post_title a").size() > 0) {
                        newsTitle = es.get(i).select("div.trb_blogroll_post_title a").text();
                        newsUrl = "http://www.latimes.com" + es.get(i).select("div.trb_blogroll_post_title a").attr("href");
                    }
                    if (es.get(i).select("div.trb_blogroll_post_description p").size() > 0) {
                        newsDesc = es.get(i).select("div.trb_blogroll_post_description p").text();
                    }

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }
            } else if ( newsCode.equals(CommConstants.news_newWorkTimes)) {
                Document doc = getDocument(url);
                String newsTitle = "";
                String newsUrl = "";
                String newsDesc = "";

                Elements es = doc.select("article.story");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("h2 a").size() > 0) {
                        newsTitle = es.get(i).select("h2 a").text();
                        newsUrl = es.get(i).select("h2 a").attr("href");
                    }
                    if (es.get(i).select("p.summary").size() > 0) {
                        newsDesc = es.get(i).select("p.summary").text();
                    }

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }

                es = doc.select("div.story-body");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("a").size() > 0) {
                        newsUrl = es.get(i).select("a").attr("href");
                    }
                    if (es.get(i).select("h2.headline").size() > 0) {
                        newsTitle = es.get(i).select("h2.headline").text();
                    }
                    if (es.get(i).select("p.summary").size() > 0) {
                        newsDesc = es.get(i).select("p.summary").text();
                    }

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }

                es = doc.select("div.story");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("h3 a").size() > 0) {
                        newsTitle = es.get(i).select("h3 a").text();
                        newsUrl = es.get(i).select("h3 a").attr("href");
                    }
                    if (es.get(i).select("p.summary").size() > 0) {
                        newsDesc = es.get(i).select("p.summary").text();
                    }

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }
            } else if ( newsCode.equals(CommConstants.news_washingtonePost)) {
                Document doc = getDocument(url);
                String newsTitle = "";
                String newsUrl = "";
                String newsDesc = "";

                Elements es = doc.select("div.story-list-story");
                for (int i = 0; i < es.size(); i++) {
                    if (es.get(i).select("div.story-headline h3 a").size() > 0) {
                        newsTitle = es.get(i).select("div.story-headline h3 a").text();
                        newsUrl = es.get(i).select("div.story-headline h3 a").attr("href");
                    }
                    if (es.get(i).select("div.story-description p").size() > 0) {
                        newsDesc = es.get(i).select("div.story-description p").text();
                    }

                    DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                }
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }
    }

    public static String getNewsContents(SQLiteDatabase db, String newsCode, int seq, String url) {
        String contents = DicDb.getNewsContents(db, seq);

        try {
            if ( contents == null  || "".equals(contents) ) {
                if ( newsCode.equals(CommConstants.news_KoreaJoongangDaily) ) {
                    Document doc = getDocument(url);
                    //DicUtils.dicLog(doc.html());

                    Elements es = doc.select("div#articlebody");
                    if ( es.size() > 0 ) {
                        contents = removeHtmlTagFromContents(es.get(0).html());

                        DicDb.updNewsContents(db, seq, contents);
                    }
                }else if ( newsCode.equals(CommConstants.news_TheChosunilbo)) {
                    Document doc = getDocument(url);
                    //DicUtils.dicLog(doc.html());

                    Elements es = doc.select("div.par");
                    for ( int i = 0; i < es.size(); i++ ) {
                        contents += removeHtmlTagFromContents(es.get(i).html()) + "\n";
                    }
                    DicDb.updNewsContents(db, seq, contents);
                }else if ( newsCode.equals(CommConstants.news_TheKoreaHerald)) {
                    Document doc = getDocument(url);
                    //DicUtils.dicLog(doc.html());

                    Elements es = doc.select("div#articleText");
                    if ( es.size() > 0 ) {
                        contents = removeHtmlTagFromContents(es.get(0).html());

                        DicDb.updNewsContents(db, seq, contents);
                    }
                }else if ( newsCode.equals(CommConstants.news_TheKoreaTimes)) {
                    Document doc = getDocument(url);
                    //DicUtils.dicLog(doc.html());

                    Elements es = doc.select("div#startts");
                    if ( es.size() > 0 ) {
                        contents = removeHtmlTagFromContents(es.get(0).html());

                        DicDb.updNewsContents(db, seq, contents);
                    }
                } else if ( newsCode.equals(CommConstants.news_reuters)) {
                    Document doc = getDocument(url);
                    //DicUtils.dicLog(doc.html());

                    Elements es = doc.select("div.ArticleBody_body_2ECha p");
                    for (int i = 0; i < es.size(); i++) {
                        contents += es.get(i).text() + "\n\n";
                    }

                    DicDb.updNewsContents(db, seq, removeHtmlTagFromContents(contents));
                } else if ( newsCode.equals(CommConstants.news_abcNews)) {
                    Document doc = getDocument(url);
                    //DicUtils.dicLog(doc.html());

                    Elements es = doc.select("div.article-body div.article-copy p");
                    for (int i = 0; i < es.size(); i++) {
                        contents += es.get(i).text() + "\n\n";
                    }

                    DicDb.updNewsContents(db, seq, removeHtmlTagFromContents(contents));
                } else if ( newsCode.equals(CommConstants.news_bbcNews)) {
                    Document doc = getDocument(url);
                    //DicUtils.dicLog(doc.html());

                    Elements es = doc.select("div.story-body div.story-body__inner p");
                    for (int i = 0; i < es.size(); i++) {
                        contents += es.get(i).text() + "\n\n";
                    }

                    DicDb.updNewsContents(db, seq, removeHtmlTagFromContents(contents));
                } else if ( newsCode.equals(CommConstants.news_cnn)) {
                    Document doc = getDocument(url);
                    //DicUtils.dicLog(doc.html());

                    Elements es = doc.select("div.l-container div p.zn-body__paragraph");
                    for (int i = 0; i < es.size(); i++) {
                        contents += es.get(i).text() + "\n\n";
                    }
                    es = doc.select("div.l-container div.zn-body__paragraph");
                    for (int i = 0; i < es.size(); i++) {
                        contents += es.get(i).text() + "\n\n";
                    }

                    DicDb.updNewsContents(db, seq, removeHtmlTagFromContents(contents));
                } else if ( newsCode.equals(CommConstants.news_losangeles)) {
                    Document doc = getDocument(url);
                    //DicUtils.dicLog(doc.html());

                    Elements es = doc.select("div.trb_ar_page p");
                    for (int i = 0; i < es.size(); i++) {
                        contents += es.get(i).text() + "\n\n";
                    }

                    DicDb.updNewsContents(db, seq, removeHtmlTagFromContents(contents));
                } else if ( newsCode.equals(CommConstants.news_newWorkTimes)) {
                    Document doc = getDocument(url);
                    //DicUtils.dicLog(doc.html());

                    Elements es = doc.select("div.story-body p");
                    for (int i = 0; i < es.size(); i++) {
                        contents += es.get(i).text() + "\n\n";
                    }

                    DicDb.updNewsContents(db, seq, removeHtmlTagFromContents(contents));
                } else if ( newsCode.equals(CommConstants.news_washingtonePost)) {
                    Document doc = getDocument(url);
                    //DicUtils.dicLog(doc.html());

                    Elements es = doc.select("div#article-body article p");
                    for (int i = 0; i < es.size(); i++) {
                        contents += es.get(i).text() + "\n\n";
                    }

                    DicDb.updNewsContents(db, seq, removeHtmlTagFromContents(contents));
                }
            }
       } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return contents;
    }

    public static String getQueryParam(String str) {
        return str.replaceAll("\"","`").replaceAll("'","`");
    }

    public static String removeHtmlTagFromContents(String str) {
        String temp = str.replaceAll("<br>", "\n").replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
        String[] tempArr = temp.split("\n");

        String contents = "";
        boolean isStart = false;
        for ( int i = 0; i < tempArr.length; i++ ) {
            if ( isStart == false ) {
                if ( "".equals(tempArr[i].trim()) ) {
                    continue;
                } else {
                    isStart = true;
                    contents += tempArr[i].trim() + "\n";
                }
            } else {
                contents += tempArr[i].trim() + "\n";
            }
        }

        return contents.replaceAll("&nbsp;","");
    }

    public static String[] getNewsInfo(String c, String n, String u) {
        String[] newsInfo = new String[3];
        newsInfo[0] = c;
        newsInfo[1] = n;
        newsInfo[2] = u;

        return newsInfo;
    }

    public static void setPreferences(Context mContext, String pref, String val) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(pref, val);
        editor.commit();
    }

    public static String getPreferences(Context mContext, String pref, String defaultVal) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String val = prefs.getString(pref, defaultVal);

        return val;
    }

    public static boolean equalPreferencesDate(Context mContext, String pref) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String date = prefs.getString(pref, "");
        dicLog(pref + " : " + date);

        if ( date.equals(getCurrentDate()) ) {
            return true;
        } else {
            setPreferences(mContext, pref, getCurrentDate());
            return false;
        }
    }

    public static void initNewsPreferences(Context mContext) {
        String[] news = getNews("C");
        for ( int n = 0; n < news.length; n++) {
            String[] newsCategory = getNewsCategory(news[n], "C");
            for ( int c = 0; c < newsCategory.length; c++ ) {
                setPreferences(mContext, news[n] + "_" + newsCategory[c], "-");
            }
        }
    }

    public static String[] getSentencesArray(String str) {
        ArrayList al = new ArrayList();
        Pattern re = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)", Pattern.MULTILINE | Pattern.COMMENTS);
        Matcher reMatcher = re.matcher(str);
        while (reMatcher.find()) {
            dicLog(reMatcher.group());
            al.add(reMatcher.group());
        }

        String[] rtn = new String[al.size()];
        for ( int i = 0; i < al.size(); i++ ) {
            rtn[i] = (String)al.get(i);
        }
        return rtn;
    }
}
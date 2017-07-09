package com.sleepingbear.ennews2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private MainCursorAdapter adapter;
    private int sNews = 0;
    private int sCategory = 0;
    private int sSeq = 0;
    private String sTitle = "";
    private String sUrl = "";

    private NewsTask task;
    private String taskKind = "";

    private static final int MY_PERMISSIONS_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] newCodes = DicUtils.getNews("C");
                final String[] newNames = DicUtils.getNews("N");

                final android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("뉴스 선택");
                dlg.setSingleChoiceItems(newNames, sNews, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        sNews = arg1;
                    }
                });
                dlg.setNegativeButton("취소", null);
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActionBar ab = getSupportActionBar();
                        ab.setTitle(newNames[sNews]);

                        changeSpinner(newCodes[sNews]);
                    }
                });
                dlg.show();
            }
        });

        System.out.println("=============================================== App Start ======================================================================");
        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        //DB가 새로 생성이 되었으면 이전 데이타를 DB에 넣고 Flag를 N 처리함
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if ( "Y".equals(prefs.getString("db_new", "N")) ) {
            DicUtils.dicLog("backup data import");

            DicUtils.readInfoFromFile(this, db, "");

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("db_new", "N");
            editor.commit();
        }

        checkPermission();

        findViewById(R.id.my_b_news_word).setOnClickListener(this);
        findViewById(R.id.my_b_voc).setOnClickListener(this);
        findViewById(R.id.my_b_voc_study).setOnClickListener(this);

        AdView av = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        av.loadAd(adRequest);

        String[] newNames = DicUtils.getNews("N");
        ActionBar ab = getSupportActionBar();
        ab.setTitle(newNames[sNews]);

        String[] newCodes = DicUtils.getNews("C");
        changeSpinner(newCodes[sNews]);
    }

    public void changeSpinner(String newsCode) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, DicUtils.getNewsCategory(newsCode, "N"));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) findViewById(R.id.my_s_category);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                sCategory = position;
                changeListView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        spinner.setSelection(0);
    }

    public void changeListView() {
        String newsCode = DicUtils.getNews("C")[sNews];
        String categoryCode = DicUtils.getNewsCategory(newsCode, "C")[sCategory];

        String insDate = DicUtils.getDelimiterDate(DicUtils.getCurrentDate(), ".");
        Cursor cursor = db.rawQuery(DicQuery.getNewsList(newsCode, categoryCode), null);
        if ( cursor.getCount() == 0 ) {
            taskKind = "NEWS_LIST";
            task = new NewsTask();
            task.execute();
        } else {
            cursor.moveToNext();
            if ( !insDate.equals(cursor.getString(cursor.getColumnIndexOrThrow("INS_DATE"))) ) {
                taskKind = "NEWS_LIST";
                task = new NewsTask();
                task.execute();
            } else {
                ListView listView = (ListView) findViewById(R.id.my_lv);
                adapter = new MainCursorAdapter(this, cursor, db, 0);
                listView.setAdapter(adapter);
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                listView.setOnItemClickListener(itemClickListener);
                listView.setOnItemLongClickListener(itemLongClickListener);
                listView.setSelection(0);
            }
        }
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            sSeq = position;

            Cursor cur = (Cursor) adapter.getItem(position);

            sTitle = cur.getString(cur.getColumnIndexOrThrow("TITLE"));
            sUrl = cur.getString(cur.getColumnIndexOrThrow("URL"));

            taskKind = "NEWS_CONTENTS";
            task = new NewsTask();
            task.execute();
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            sSeq = i;

            Cursor cur = (Cursor) adapter.getItem(i);

            sTitle = cur.getString(cur.getColumnIndexOrThrow("TITLE"));
            sUrl = cur.getString(cur.getColumnIndexOrThrow("URL"));

            taskKind = "NEWS_CONTENTS_LONG";
            task = new NewsTask();
            task.execute();

            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 상단 메뉴 구성
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {
            Intent msg = new Intent(Intent.ACTION_SEND);
            msg.addCategory(Intent.CATEGORY_DEFAULT);
            msg.putExtra(Intent.EXTRA_SUBJECT, "최고의 영어신문2 어플");
            msg.putExtra(Intent.EXTRA_TEXT, "영어.. 참 어렵죠? '최고의 영어신문2' 어플을 사용해 보세요. https://play.google.com/store/apps/details?id=com.sleepingbear.ennews2 ");
            msg.setType("text/plain");
            startActivity(Intent.createChooser(msg, "어플 공유"));

            return true;
        } else if (id == R.id.action_patch) {
            startActivity(new Intent(getApplication(), PatchActivity.class));

            return true;
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            Intent helpIntent = new Intent(getApplication(), HelpActivity.class);
            helpIntent.putExtras(bundle);
            startActivity(helpIntent);

            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(getApplication(), SettingsActivity.class));

            return true;
        } else if (id == R.id.action_no_ad) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.sleepingbear.ennews2")));
        } else if (id == R.id.action_refresh) {
            taskKind = "NEWS_LIST";
            task = new NewsTask();
            task.execute();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        DicUtils.dicLog("onClick");
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.my_b_news_word:
                Intent newClickWordIntent = new Intent(getApplication(), NewsClickWordActivity.class);
                newClickWordIntent.putExtras(bundle);
                startActivity(newClickWordIntent);

                break;

            case R.id.my_b_voc:
                startActivity(new Intent(getApplication(), VocabularyNoteActivity.class));
                break;
            case R.id.my_b_voc_study:
                startActivity(new Intent(getApplication(), StudyActivity.class));

                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        DicUtils.dicLog("onActivityResult : " + requestCode + " : " + resultCode);

        switch ( requestCode ) {
            case CommConstants.a_news :
                if ( resultCode == RESULT_OK && "Y".equals(data.getStringExtra("isChange")) ) {
                    changeListView();
                }

                break;
        }
    }

    public boolean checkPermission() {
        Log.d(CommConstants.tag, "checkPermission");
        boolean isCheck = false;
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
            Log.d(CommConstants.tag, "권한 없음");
            if ( ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {
                //Toast.makeText(this, "(중요)파일로 내보내기, 가져오기를 하기 위해서 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
            Log.d(CommConstants.tag, "2222");
        } else {
            Log.d(CommConstants.tag, "권한 있음");
            isCheck = true;
        }

        return isCheck;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(CommConstants.tag, "권한 허가");
                } else {
                    Log.d(CommConstants.tag, "권한 거부");
                    Toast.makeText(this, "파일 권한이 없기 때문에 파일 내보내기, 가져오기를 할 수 없습니다.\n만일 권한 팝업이 안열리면 '다시 묻지 않기'를 선택하셨기 때문입니다.\n어플을 지우고 다시 설치하셔야 합니다.", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    private long backKeyPressedTime = 0;
    @Override
    public void onBackPressed() {
        //종료 시점에 변경 사항을 기록한다.
        if ( "Y".equals(DicUtils.getDbChange(getApplicationContext())) ) {
            DicUtils.writeInfoToFile(this, db, "");
            DicUtils.clearDbChange(this);
        }

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "'뒤로'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();

            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
        }
    }

    private class NewsTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog pd;
        private String contents = "";

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(MainActivity.this);
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.show();
            pd.setContentView(R.layout.custom_progress);

            pd.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            pd.show();

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            if ( taskKind.equals("NEWS_LIST") ) {
                //기사 리스트를 읽어 온다.
                String newsCode = DicUtils.getNews("C")[sNews];
                String categoryCode = DicUtils.getNewsCategory(newsCode, "C")[sCategory];

                DicUtils.getNewsCategoryNews(db, newsCode, categoryCode, DicUtils.getNewsCategory(newsCode, "U")[sCategory]);
            } else if ( taskKind.equals("NEWS_CONTENTS") || taskKind.equals("NEWS_CONTENTS_LONG") ) {
                //기사를 읽어 온다.
                String newsCode = DicUtils.getNews("C")[sNews];
                String categoryCode = DicUtils.getNewsCategory(newsCode, "C")[sCategory];

                contents = DicUtils.getNewsContents(db, newsCode, sSeq, sUrl);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pd.dismiss();
            task = null;

            if ( taskKind.equals("NEWS_LIST") ) {
                changeListView();
            } else if ( taskKind.equals("NEWS_CONTENTS") ) {
                Cursor cur = (Cursor) adapter.getItem(sSeq);

                Bundle bundle = new Bundle();
                String newsCode = DicUtils.getNews("C")[sNews];
                bundle.putString("KIND", DicUtils.getNews("W")[sNews]);
                bundle.putString("CATEGORY", DicUtils.getNewsCategory(newsCode, "N")[sCategory]);
                bundle.putString("TITLE", cur.getString(cur.getColumnIndexOrThrow("TITLE")));
                bundle.putString("URL", cur.getString(cur.getColumnIndexOrThrow("URL")));
                bundle.putString("CONTENTS", contents);

                Intent helpIntent = new Intent(getApplication(), NewsViewActivity.class);
                helpIntent.putExtras(bundle);
                startActivity(helpIntent);
            } else if ( taskKind.equals("NEWS_CONTENTS_LONG") ) {
                Cursor cur = (Cursor) adapter.getItem(sSeq);

                Bundle bundle = new Bundle();
                bundle.putString("kind", DicUtils.getNews("W")[sNews]);
                bundle.putString("url", cur.getString(cur.getColumnIndexOrThrow("URL")));

                Intent helpIntent = new Intent(getApplication(), NewsHtmlViewActivity.class);
                helpIntent.putExtras(bundle);
                startActivity(helpIntent);
            }

            super.onPostExecute(result);
        }
    }
}


class MainCursorAdapter extends CursorAdapter {
    private SQLiteDatabase mDb;
    int fontSize = 0;

    public MainCursorAdapter(Context context, Cursor cursor, SQLiteDatabase db, int flags) {
        super(context, cursor, 0);
        mDb = db;

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_main_item, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.my_tv_title)).setText(cursor.getString(cursor.getColumnIndexOrThrow("TITLE")));
        ((TextView) view.findViewById(R.id.my_tv_date)).setText(cursor.getString(cursor.getColumnIndexOrThrow("INS_DATE")));
        ((TextView) view.findViewById(R.id.my_tv_desc)).setText(cursor.getString(cursor.getColumnIndexOrThrow("DESC")));

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_title)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_desc)).setTextSize(fontSize);
    }
}

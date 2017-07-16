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
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
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

                        DicUtils.setPreferences(getApplicationContext(), "sNews", Integer.toString(sNews));
                    }
                });
                dlg.setNegativeButton("취소", null);
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActionBar ab = getSupportActionBar();
                        ab.setTitle(newNames[sNews]);

                        changeSpinner(newCodes[sNews], 0);
                    }
                });
                dlg.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

        //5일 이전 데이타 삭제
        String delDate = DicUtils.getDelimiterDate(DicUtils.getAddDay(DicUtils.getCurrentDate(), -5), ".");
        DicDb.delOldNews(db, delDate);

        //이전 기록
        sNews = Integer.parseInt(DicUtils.getPreferences(getApplicationContext(), "sNews", "0"));
        sCategory = Integer.parseInt(DicUtils.getPreferences(getApplicationContext(), "sCategory", "0"));

        AdView av = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        av.loadAd(adRequest);

        String[] newNames = DicUtils.getNews("N");
        ActionBar ab = getSupportActionBar();
        ab.setTitle(newNames[sNews]);

        String[] newCodes = DicUtils.getNews("C");
        changeSpinner(newCodes[sNews], sCategory);

        // 사용법 Dialog...
        String appHintDialog = "20170711";
        int appHintDialogCount = prefs.getInt("appHintDialogCount", 3);
        //DicUtils.dicLog("appHintDialogCount : " + appHintDialogCount);
        boolean showDialog = false;
        if ( !appHintDialog.equals(prefs.getString("appHintDialog", "N")) ) {
            showDialog = true;

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("appHintDialog", appHintDialog);
            editor.putInt("appHintDialogCount", 2);
            appHintDialogCount = 2;
            editor.commit();
        } else if ( appHintDialogCount == 1 || appHintDialogCount == 2 || appHintDialogCount == 3 ) {
            showDialog = true;

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("appHintDialog", appHintDialog);
            editor.putInt("appHintDialogCount", --appHintDialogCount);
            editor.commit();
        }
        if ( showDialog ) {
            String msg = "1. 하단 리스트 버튼으로 뉴스를 선택하세요.\n";
            msg += "2. 카테고리를 선택한 후에 뉴스를 클릭하세요.\n";
            msg += "3. 뉴스를 길게 클릭하면 뉴스 사이트로 이동합니다.\n";
            msg += "4. 뉴스를 보면서 클릭한 단어는 '뉴스 클릭 단어'에서 확인 할 수 있습니다.\n";
            msg += "5. '뉴스 클릭 단어' 화면에서 단어를 단어장에 등록해서 단어 학습을 하세요.\n";

            new AlertDialog.Builder(this)
                    .setTitle("알림" + (appHintDialogCount >= 0 ? " - " + ++appHintDialogCount : ""))
                    .setMessage(msg)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
    }

    public void changeSpinner(String newsCode, int pos) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, DicUtils.getNewsCategory(newsCode, "N"));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) findViewById(R.id.my_s_category);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                sCategory = position;

                DicUtils.setPreferences(getApplicationContext(), "sCategory", Integer.toString(sCategory));

                String newCode = DicUtils.getNews("C")[sNews];
                if ( DicUtils.equalPreferencesDate(getApplicationContext(), newCode + "_" + DicUtils.getNewsCategory(newCode, "C")[sCategory]) ) {
                    changeListView();
                } else {
                    taskKind = "NEWS_LIST";
                    task = new NewsTask();
                    task.execute();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        spinner.setSelection(pos);
    }

    public void changeListView() {
        String newsCode = DicUtils.getNews("C")[sNews];
        String categoryCode = DicUtils.getNewsCategory(newsCode, "C")[sCategory];

        Cursor cursor = db.rawQuery(DicQuery.getNewsList(newsCode, categoryCode), null);
        ListView listView = (ListView) findViewById(R.id.my_lv);
        adapter = new MainCursorAdapter(this, cursor, db, 0);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(itemClickListener);
        listView.setOnItemLongClickListener(itemLongClickListener);
        listView.setSelection(0);
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

        if (id == R.id.action_refresh) {
            taskKind = "NEWS_LIST";
            task = new NewsTask();
            task.execute();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        DicUtils.dicLog("onActivityResult : " + requestCode + " : " + resultCode);

        switch ( requestCode ) {
            case CommConstants.a_setting :
                changeListView();

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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        //} else {
        //    super.onBackPressed();
        }

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_click_word) {
            startActivity(new Intent(getApplication(), NewsClickWordActivity.class));
        } else if (id == R.id.nav_voc) {
            startActivity(new Intent(getApplication(), VocabularyNoteActivity.class));
        } else if (id == R.id.nav_study) {
            startActivity(new Intent(getApplication(), StudyActivity.class));
        } else if (id == R.id.nav_patch) {
            startActivity(new Intent(getApplication(), PatchActivity.class));
        } else if (id == R.id.nav_help) {
            Bundle bundle = new Bundle();
            Intent helpIntent = new Intent(getApplication(), HelpActivity.class);
            helpIntent.putExtras(bundle);
            startActivity(helpIntent);
        } else if (id == R.id.nav_setting) {
            startActivityForResult(new Intent(getApplication(), SettingsActivity.class), CommConstants.a_setting);
        } else if (id == R.id.nav_share) {
            Intent msg = new Intent(Intent.ACTION_SEND);
            msg.addCategory(Intent.CATEGORY_DEFAULT);
            msg.putExtra(Intent.EXTRA_SUBJECT, "최고의 영어신문2 어플");
            msg.putExtra(Intent.EXTRA_TEXT, "영어.. 참 어렵죠? '최고의 영어신문2' 어플을 사용해 보세요. https://play.google.com/store/apps/details?id=com.sleepingbear.ennews2 ");
            msg.setType("text/plain");
            startActivity(Intent.createChooser(msg, "어플 공유"));
        } else if (id == R.id.nav_review) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
        } else if (id == R.id.nav_other_app) {
            String url ="http://blog.naver.com/limsm9449/221031416154";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } else if (id == R.id.nav_no_ad) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.sleepingbear.ennews2")));
        } else if (id == R.id.nav_mail) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
            intent.putExtra(Intent.EXTRA_TEXT, "어플관련 문제점을 적어 주세요.\n빠른 시간 안에 수정을 하겠습니다.\n감사합니다.");
            intent.setData(Uri.parse("mailto:limsm9449@gmail.com"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

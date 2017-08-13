package com.sleepingbear.ennews2;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class PatchActivity extends AppCompatActivity {
    private int fontSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patch);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        StringBuffer patch = new StringBuffer();
        patch.append("* 패치 내역" + CommConstants.sqlCR);
        patch.append("- 해외 8개 영어뉴스 추가" + CommConstants.sqlCR);
        patch.append("- 2017.07.20 : 최고의 영어신문 2 개발" + CommConstants.sqlCR);

        ((TextView) this.findViewById(R.id.my_c_patch_tv1)).setText(patch.toString());

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_font ) );
        ((TextView) this.findViewById(R.id.my_c_patch_tv1)).setTextSize(fontSize);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}

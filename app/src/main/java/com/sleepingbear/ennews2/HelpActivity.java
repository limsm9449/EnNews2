package com.sleepingbear.ennews2;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {
    private int fontSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = getSupportActionBar();
        //ab.setHomeButtonEnabled(true);
        //ab.setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        StringBuffer allSb = new StringBuffer();
        StringBuffer CurrentSb = new StringBuffer();
        StringBuffer tempSb = new StringBuffer();

        String screen = b.getString("SCREEN");
        if ( screen == null ) {
            screen = "";
        }
        String kind = b.getString("KIND");
        if ( kind == null ) {
            kind = "";
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 어플 사용팁" + CommConstants.sqlCR);
        tempSb.append("1. 하단 리스트 버튼으로 뉴스를 선택하세요." + CommConstants.sqlCR);
        tempSb.append("2. 카테고리를 선택한 후에 뉴스를 클릭하세요." + CommConstants.sqlCR);
        tempSb.append("3. 뉴스를 길게 클릭하면 뉴스 사이트로 이동합니다." + CommConstants.sqlCR);
        tempSb.append("4. 뉴스를 보면서 클릭한 단어는 '뉴스 클릭 단어'에서 확인 할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("5. '뉴스 클릭 단어' 화면에서 단어를 단어장에 등록해서 단어 학습을 하세요." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        allSb.append(tempSb.toString());

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 뉴스" + CommConstants.sqlCR);
        tempSb.append(" .오른쪽 하단의 리스트 버튼을 클릭해서 뉴스를 선택하세요. " + CommConstants.sqlCR);
        tempSb.append(" .뉴스를 선택하면 뉴스별 카테고리가 변경됩니다. " + CommConstants.sqlCR);
        tempSb.append(" .카테고리를 선택하면 관련 뉴스를 조회합니다." + CommConstants.sqlCR);
        tempSb.append(" .뉴스를 클릭하시면 뉴스 상세를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .뉴스를 길게 클릭하시면 사이트 기사를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_novelView) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 뉴스 상세" + CommConstants.sqlCR);
        tempSb.append("- 뉴스를 보면서 필요한 단어를 검색할 수 있는 기능이 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .뉴스를 보다가 모르는 단어를 클릭을 하면 하단에 클릭한 단어의 뜻이 보입니다. " + CommConstants.sqlCR);
        tempSb.append(" .클릭단어의 뜻이 없을경우 하단 오른쪽의 검색 버튼을 클릭하면 Naver,Daum에서 단어 검색을 할 수 있습니다. " + CommConstants.sqlCR);
        tempSb.append(" .하단 단어를 길게 클릭하시면 단어 상세를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .하단 단어 옆의 (+)를 클릭하시면 바로 단어장에 등록을 하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .뉴스의 단어를 길게 클릭하시면 단어보기, 단어검색(Naver,Daum), 번역, 문장보기, TTS, 전체TTS, 복사, 전체복사 기능을 사용하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .뉴스를 보면서 클릭한 단어는 '뉴스 클릭 단어' 화면에서 확인하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_novelView) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 뉴스 클릭 단어" + CommConstants.sqlCR);
        tempSb.append("- 뉴스를 보면서 클릭한 단어들에 대하여 관리하는 화면입니다." + CommConstants.sqlCR);
        tempSb.append(" .상단 수정 버튼(연필모양)를 클릭하시면 단어를 선택, 삭제, 단어장에 저장, 신규 단어장에 저장할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 클릭하시면 단어상세를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_newsClickWord) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단어장" + CommConstants.sqlCR);
        tempSb.append("- 단어장 목록을 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .하단의 + 버튼을 클릭해서 신규 단어장을 추가할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .기존 단어장을 길게 클릭하시면 수정, 추가, 삭제,  내보내기, 가져오기를 하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .단어장을 클릭하시면 등록된 단어를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_vocabularyNote) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단어장 상세" + CommConstants.sqlCR);
        tempSb.append("- 단어 목록 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .상단 수정 버튼능 클릭하면 단어장을 편집(삭제,복사,이동) 할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .상단 TTS 버튼을 클릭하면 단어,뜻을 들을 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_vocabularyNoteView) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단답 학습" + CommConstants.sqlCR);
        tempSb.append(" .단어를 클릭하시면 뜻을 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .별표를 클릭해서 암기여부를 표시합니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 길게 클릭하시면 단어 보기/전체 정답 보기를 선택하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_study1) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 4지선다 학습" + CommConstants.sqlCR);
        tempSb.append(" .별표를 클릭해서 암기여부를 표시합니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 길게 클릭하시면 정답 보기/ 단어 보기/전체 정답 보기를 선택하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_study2) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 카드형 학습" + CommConstants.sqlCR);
        tempSb.append("- 카드형 학습입니다." + CommConstants.sqlCR);
        tempSb.append(" .하단 Play 버튼을 클릭하시면 영어를 보여주고 잠시후에 뜻이 보여집니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_study3) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 카드형 OX 학습" + CommConstants.sqlCR);
        tempSb.append("- 카드형 OX 학습입니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_study4) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 카드형 4지선다 학습" + CommConstants.sqlCR);
        tempSb.append("- 카드형 4지선다 학습입니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_study5) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 카드형 4지선다 TTS 학습" + CommConstants.sqlCR);
        tempSb.append("- TTS를 이용하여 학습을 합니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_study6) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단어 상세" + CommConstants.sqlCR);
        tempSb.append("- 상단 콤보 메뉴를 선택하시면 네이버 사전, 다음 사전, 예제를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_wordView) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 문장 상세" + CommConstants.sqlCR);
        tempSb.append("- 문장의 발음 및 관련 단어를 조회하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 클릭하시면 단어 보기 및 등록할 단어장을 선택 하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 길게 클릭하시면 등록할 단어장을 선택할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .별표를 클릭하시면 Default 단어장에 추가 됩니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_sentenceView) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        if ( "ALL".equals(b.getString("SCREEN")) ) {
            ((TextView) this.findViewById(R.id.my_c_help_tv1)).setText(allSb.toString());
        } else {
            ((TextView) this.findViewById(R.id.my_c_help_tv1)).setText(CurrentSb.toString() + CommConstants.sqlCR + CommConstants.sqlCR + allSb.toString());
        }

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_font ) );
        ((TextView) this.findViewById(R.id.my_c_help_tv1)).setTextSize(fontSize);
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

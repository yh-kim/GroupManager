package com.enterpaper.groupmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    ListView lvMembers;
    FloatingActionButton fab;
    Toolbar mToolBar;
    MemberAdapter adapter;
    ArrayList<Member> arrMember = new ArrayList<>();
    AlertDialog mDialog;
    int count;
    int selectMember;

    @Override
    protected void onResume() {
        super.onResume();
        initializing();
    }

    private void initializing(){
        count = 0;
        arrMember.clear();
        new NetworkGetMemberList().execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 폰트 설정
        SetFont.setGlobalFont(this, getWindow().getDecorView());

        initializeToolbar();

        initializeLayout();

        setListener();
    }

    private void initializeToolbar() {
        //액션바 객체 생성
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        //액션바 설정
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        //액션바 숨김
        actionBar.hide();

        //툴바 설정
        mToolBar = (Toolbar) findViewById(R.id.main_toolbar);
        mToolBar.setContentInsetsAbsolute(0, 0);
    }

    private void initializeLayout(){
        lvMembers = (ListView)findViewById(R.id.lvMembers);
        fab = (FloatingActionButton)findViewById(R.id.fab);

        // adapter 생성
        adapter = new MemberAdapter(getApplicationContext(), R.layout.row_member, arrMember);

        // adapter 연결
        lvMembers.setAdapter(adapter);

    }

    private void setListener(){
        fab.attachToListView(lvMembers);
        fab.setColorNormal(Color.argb(0, 129, 22, 136));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 멤버 추가
                Intent itAddMember = new Intent(getApplicationContext(),AddMemberActivity.class);
                startActivity(itAddMember);
                overridePendingTransition(0,0);
            }
        });

        lvMembers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final CharSequence[] items = {"상태 수정", "멤버 삭제"};
                selectMember = position;

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);     // 여기서 this는 Activity의 this

                // 여기서 부터는 알림창의 속성 설정
                builder.setItems(items, new DialogInterface.OnClickListener() {    // 목록 클릭시 설정
                            public void onClick(DialogInterface dialog, int index) {
                                // int형으로 조건 지정
                                switch (index) {
                                    case 0:
                                        Intent itAdjstMember = new Intent(getApplicationContext(),AdjustMemberActivity.class);
                                        startActivityForResult(itAdjstMember, 1);
                                        overridePendingTransition(0,0);
                                        break;
                                    case 1:
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setTitle("삭제 확인")
                                                .setMessage("정말 삭제하시겠습니까?")        // 메세지 설정
                                                .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                                                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                                            // 확인 버튼 클릭시 설정
                                                            public void onClick(DialogInterface dialog_del, int whichButton) {
                                                                new NetworkDeleteMember().execute();
                                                            }
                                                        }
                                                )
                                                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                                            // 취소 버튼 클릭시 설정
                                                            public void onClick(DialogInterface dialog_del, int whichButton) {
                                                                dialog_del.cancel();
                                                            }
                                                        }
                                                );
                                        AlertDialog dialog_del = builder.create();    // 알림창 객체 생성
                                        dialog_del.show();    // 알림창 띄우기
                                        break;
                                    default:
                                        dialog.cancel();
                                        break;
                                }
                            }
                        }
                );

                AlertDialog dialog = builder.create();    // 알림창 객체 생성
                dialog.show();    // 알림창 띄우기
                return false;
            }
        });
    }

    // member list HTTP연결 Thread 생성 클래스
    class NetworkGetMemberList extends AsyncTask<String, String, Integer> {
        private String err_msg = "Network error.";

        // JSON에서 받아오는 객체
        private JSONObject jObjects;

        // AsyncTask 실행되는거
        @Override
        protected Integer doInBackground(String... params) {
            return processing();
        }


        // AsyncTask 실행완료 후에 구동 (Data를 받은것을 Activity에 갱신하는 작업을 하면돼)
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            // 지금 코드에서는 result가 0이면 정상적인 상황
            if (result == 0) {

                // JSON에서 받은 객체를 가지고 List에 뿌려줘야해
                // jObject에서 데이터를 뽑아내자
                try {
                    // 가져오는 값의 개수를 가져옴
                    count = jObjects.getInt("cnt");

                    JSONArray ret_arr = jObjects.getJSONArray("ret");
                    for (int index = 0; index < ret_arr.length(); index++) {
                        JSONObject obj_boothIdeas = ret_arr.getJSONObject(index);

                        int id = obj_boothIdeas.getInt("Id");
                        String name = obj_boothIdeas.getString("Name");
                        String date = obj_boothIdeas.getString("Date");
                        String department = obj_boothIdeas.getString("Department");
                        String introduction = obj_boothIdeas.getString("Introduction");

                        if(introduction.length() >10){
                            introduction = introduction.substring(0,10) +"..";
                        }

                        // Item 객체로 만들어야함
                        Member member = new Member(id,name,department,introduction);

                        // Item 객체를 ArrayList에 넣는다
                        arrMember.add(0,member);
                    }


                    // Adapter에게 데이터를 넣었으니 갱신하라고 알려줌
                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return;
            }
            // Error 상황
            else {
                Toast.makeText(getApplicationContext(), "Error",
                        Toast.LENGTH_SHORT).show();
            }
        }

        private Integer processing() {
            try {
                HttpClient http_client = new DefaultHttpClient();
                // 요청한 후 7초 이내에 오지 않으면 timeout 발생하므로 빠져나옴
                http_client.getParams().setParameter("http.connection.timeout",
                        7000);

                // data를 Post방식으로 보냄
                HttpPost http_post = null;

                List<NameValuePair> name_value = new ArrayList<NameValuePair>();

                http_post = new HttpPost(
                        "http://54.199.176.234/api/gravity_get_members.php");

                UrlEncodedFormEntity entityRequest = new UrlEncodedFormEntity(
                        name_value, "UTF-8");
                http_post.setEntity(entityRequest);

                // 실행
                HttpResponse response = http_client.execute(http_post);

                // 받는 부분
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                response.getEntity().getContent(), "UTF-8"), 8);
                StringBuilder builder = new StringBuilder();
                for (String line = null; (line = reader.readLine()) != null; ) {
                    builder.append(line).append("\n");
                }

                // 우리가 사용하는 결과
                jObjects = new JSONObject(builder.toString());

                // err가 0이면 정상적인 처리
                // err가 0이 아닐시 오류발생
                if (jObjects.getInt("err") > 0) {
                    return jObjects.getInt("err");
                }
            } catch (Exception e) {
                // 오류발생시
                Log.i(err_msg, e.toString());
                return 100;
            }
            return 0;
        }
    }



    // member delete HTTP연결 Thread 생성 클래스
    class NetworkDeleteMember extends AsyncTask<String, String, Integer> {
        private String err_msg = "Network error.";

        // JSON에서 받아오는 객체
        private JSONObject jObjects;

        // AsyncTask 실행되는거
        @Override
        protected Integer doInBackground(String... params) {
            return processing();
        }

        // AsyncTask 실행완료 후에 구동 (Data를 받은것을 Activity에 갱신하는 작업을 하면돼)
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            // 지금 코드에서는 result가 0이면 정상적인 상황
            if (result == 0) {
                arrMember.remove(selectMember);
                // Adapter에게 데이터를 넣었으니 갱신하라고 알려줌
                adapter.notifyDataSetChanged();

                return;
            }
            // Error 상황
            else {
                Toast.makeText(getApplicationContext(), "Error",
                        Toast.LENGTH_SHORT).show();
            }
        }

        private Integer processing() {
            try {
                HttpClient http_client = new DefaultHttpClient();
                // 요청한 후 7초 이내에 오지 않으면 timeout 발생하므로 빠져나옴
                http_client.getParams().setParameter("http.connection.timeout",
                        7000);

                // data를 Post방식으로 보냄
                HttpPost http_post = null;

                List<NameValuePair> name_value = new ArrayList<NameValuePair>();

                http_post = new HttpPost(
                        "http://54.199.176.234/api/gravity_delete_member.php");

                // data를 담음
                name_value.add(new BasicNameValuePair("id", arrMember.get(selectMember).getId() + ""));

                UrlEncodedFormEntity entityRequest = new UrlEncodedFormEntity(
                        name_value, "UTF-8");
                http_post.setEntity(entityRequest);

                // 실행
                HttpResponse response = http_client.execute(http_post);

                // 받는 부분
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                response.getEntity().getContent(), "UTF-8"), 8);
                StringBuilder builder = new StringBuilder();
                for (String line = null; (line = reader.readLine()) != null; ) {
                    builder.append(line).append("\n");
                }

                // 우리가 사용하는 결과
                jObjects = new JSONObject(builder.toString());

                // err가 0이면 정상적인 처리
                // err가 0이 아닐시 오류발생
                if (jObjects.getInt("err") > 0) {
                    return jObjects.getInt("err");
                }
            } catch (Exception e) {
                // 오류발생시
                Log.i(err_msg, e.toString());
                return 100;
            }
            return 0;
        }
    }
}

package com.enterpaper.groupmanager;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Created by Kim on 2015-12-23.
 */
public class SearchMemberActivity extends Activity {
    EditText editSearch;
    TextView tvSearchCount;
    ImageView btnSearch;
    ListView lvSearchMembers;
    MemberAdapter adapter;
    ArrayList<Member> arrMember = new ArrayList<>();
    String keyword;
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 폰트 설정
        SetFont.setGlobalFont(this, getWindow().getDecorView());

        initializeLayout();

        setListener();
    }

    private void initializeLayout(){
        editSearch = (EditText)findViewById(R.id.editSearch);
        btnSearch = (ImageView)findViewById(R.id.btnSearch);
        lvSearchMembers = (ListView)findViewById(R.id.lvSearchMembers);
        tvSearchCount = (TextView)findViewById(R.id.tvSearchCount);

        // adapter 생성
        adapter = new MemberAdapter(getApplicationContext(), R.layout.row_member, arrMember);

        // adapter 연결
        lvSearchMembers.setAdapter(adapter);

    }

    private void setListener(){
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editSearch.getText().toString().length() == 0){
                    Toast.makeText(getApplicationContext(),"검색어를 입력해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                arrMember.clear();
                adapter.notifyDataSetChanged();
                tvSearchCount.setText("");

                keyword = editSearch.getText().toString().trim();

                new NetworkSearchMembers().execute();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,0);
    }

    // member add HTTP연결 Thread 생성 클래스
    class NetworkSearchMembers extends AsyncTask<String, String, Integer> {
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

                    if(count == 0){
                        Toast.makeText(getApplicationContext(),"검색결과가 없습니다",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    tvSearchCount.setText("일치항목(" + count +")");

                    JSONArray ret_arr = jObjects.getJSONArray("ret");
                    for (int index = 0; index < ret_arr.length(); index++) {
                        JSONObject obj_boothIdeas = ret_arr.getJSONObject(index);

                        int id = obj_boothIdeas.getInt("id");
                        String name = obj_boothIdeas.getString("name");
                        String date = obj_boothIdeas.getString("date");
                        String department = obj_boothIdeas.getString("department");
                        String introduction = obj_boothIdeas.getString("introduction");

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
                        "http://54.199.176.234/api/gravity_search_members.php");

                // data를 담음
                name_value.add(new BasicNameValuePair("keyword", keyword));

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

package com.enterpaper.groupmanager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim on 2015-12-23.
 */
public class AdjustMemberActivity extends Activity {
    RadioButton rbAdjustDeveloper, rbAdjustDesigner, rbAdjustPlanner;
    RadioGroup adjustRadioGroup;
    Button btnAdjustMember, btnAdjustMemberCancel;
    Member member;
    int originId;
    EditText adjustId, adjustName, adjustIntroduction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust_member);

        // 폰트 설정
        SetFont.setGlobalFont(this, getWindow().getDecorView());

        initializeLayout();

        // intent에 담은 데이터 가져오기
        getIntentData();

        setListener();
    }

    private void getIntentData(){
        Intent itReceive = getIntent();
        originId = itReceive.getExtras().getInt("id");
        String name = itReceive.getExtras().getString("name");
        String department = itReceive.getExtras().getString("department");
        String introduction = itReceive.getExtras().getString("introduction");
        member = new Member(originId, name, department, introduction);

        adjustId.setText(originId+"");
        adjustName.setText(name);
        adjustIntroduction.setText(introduction);
        if(department.equals("디자인")){
            rbAdjustDesigner.setChecked(true);
        }else if(department.equals("기획")){
            rbAdjustPlanner.setChecked(true);
        }else{
            rbAdjustDeveloper.setChecked(true);
        }
    }

   private void initializeLayout(){
        rbAdjustDeveloper = (RadioButton)findViewById(R.id.rbAdjustDeveloper);
        rbAdjustDesigner = (RadioButton)findViewById(R.id.rbAdjustDesigner);
        rbAdjustPlanner = (RadioButton)findViewById(R.id.rbAdjustPlanner);
        adjustRadioGroup = (RadioGroup)findViewById(R.id.adjustRadioGroup);
        btnAdjustMember = (Button)findViewById(R.id.btnAdjustMember);
        btnAdjustMemberCancel = (Button)findViewById(R.id.btnAdjustMemberCancel);
        adjustId = (EditText)findViewById(R.id.adjustId);
        adjustName = (EditText)findViewById(R.id.adjustName);
        adjustIntroduction = (EditText)findViewById(R.id.adjustIntroduction);

    }

    private void setListener(){
        btnAdjustMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton selectRadioButton = (RadioButton) findViewById(adjustRadioGroup.getCheckedRadioButtonId());
                String idText = adjustId.getText().toString().trim();
                String name = adjustName.getText().toString().trim();
                String department = selectRadioButton.getText().toString();
                String introduction = adjustIntroduction.getText().toString().trim();

                if (idText.equals("") || name.equals("") || introduction.equals("")) {
                    Toast.makeText(getApplication(), "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (idText.length() != 8) {
                    Toast.makeText(getApplication(), "올바른 학번을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                int id = Integer.valueOf(idText);
                member.setId(id);
                member.setName(name);
                member.setDepartment(department);
                member.setIntroduction(introduction);

                new NetworkAdjustMember().execute();
            }
        });

        btnAdjustMemberCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,0);
    }

    // member add HTTP연결 Thread 생성 클래스
    class NetworkAdjustMember extends AsyncTask<String, String, Integer> {
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
                finish();
                return;
            }
            // 이미 있는 학번
            else if(result == 3){
                Toast.makeText(getApplicationContext(),"이미 등록되어 있는 학번입니다", Toast.LENGTH_SHORT).show();
                adjustId.setTextColor(Color.rgb(221, 67, 58));
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
                        "http://54.199.176.234/api/gravity_adjust_member.php");

                // data를 담음
                name_value.add(new BasicNameValuePair("user_id", originId + ""));
                name_value.add(new BasicNameValuePair("id", member.getId() + ""));
                name_value.add(new BasicNameValuePair("name", member.getName() + ""));
                name_value.add(new BasicNameValuePair("department", member.getDepartment() + ""));
                name_value.add(new BasicNameValuePair("introduction", member.getIntroduction() + ""));

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

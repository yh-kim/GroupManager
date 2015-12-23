package com.enterpaper.groupmanager;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kim on 2015-12-22.
 */
public class MemberAdapter extends ArrayAdapter<Member> {//LayoutInflater -> XML을 동적으로 만들 때 필요
    private LayoutInflater inflater = null;
    //Context -> Activity Class의 객체
    private Context context = null;

    public MemberAdapter(Context context, int resource, ArrayList<Member> objects) {
        super(context, resource, objects);

        //context는 함수를 호출한 activiy
        //resource는 row_xxx.xml 의 정보
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }


    //ArrayList에 저장되어있는 데이터를 fragment에 넣는 method
    //List 하나마다 getView가 한번 실행된다
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //  aq = new AQuery(convertView);
        //position -> List번호
        ViewHolder holder;

        //XML 파일이 비어있는 상태라면
        if (convertView == null) {
            //layout 설정
            convertView = inflater.inflate(R.layout.row_member, null);
            //TextView 폰트 지정
            SetFont.setGlobalFont(context, convertView);

            holder = new ViewHolder();

            //row에 있는 정보들을 holder로 가져옴
            holder.img = (ImageView)convertView.findViewById(R.id.memberImage);
            holder.tvMemberName = (TextView)convertView.findViewById(R.id.tvMemberName);
            holder.tvMemberId = (TextView)convertView.findViewById(R.id.tvMemberId);
            holder.tvMemberIntroduction = (TextView)convertView.findViewById(R.id.tvMemberIntroduction);


            convertView.setTag(holder);
        }


        holder = (ViewHolder) convertView.getTag();

        Member member = getItem(position);

        int id = member.getId();

        String name = member.getName();
        String department = member.getDepartment();
        String introduction = member.getIntroduction();

        String inputId = "(" + id + ")";
        if(department.equals("기획자")){
            holder.img.setImageResource(R.drawable.planner);
        }else if(department.equals("디자인")){
            holder.img.setImageResource(R.drawable.designer);
        }else{
            holder.img.setImageResource(R.drawable.developer);
        }


        holder.tvMemberName.setText(name);
        holder.tvMemberId.setText(inputId);
        holder.tvMemberIntroduction.setText(introduction);

        return convertView;
    }

    class ViewHolder {
        ImageView img;
        TextView tvMemberName,tvMemberId,tvMemberIntroduction;
    }

}

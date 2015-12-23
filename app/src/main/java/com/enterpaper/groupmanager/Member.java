package com.enterpaper.groupmanager;

/**
 * Created by Kim on 2015-12-22.
 */
public class Member {
    private int id;
    private String name;
    private String department;  // 개발, 기획, 디자인
    private String introduction;

    public Member(int id, String name, String department, String introduction){
        this.id = id;
        this.name = name;
        this.department = department;
        this.introduction = introduction;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
}

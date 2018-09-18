package com.akhris.pregnytalk.contract;

public class Child {
    private String name;
    private String sex;
    private Long birthDateMillis;

    public static final String SEX_MALE = "male";
    public static final String SEX_FEMALE = "female";

    public Child() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Long getBirthDateMillis() {
        return birthDateMillis;
    }

    public void setBirthDateMillis(Long birthDateMillis) {
        this.birthDateMillis = birthDateMillis;
    }
}
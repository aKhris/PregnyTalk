package com.akhris.pregnytalk.contract;

/**
 * Class representing Child in Firebase Realtime Database
 */
public class Child{
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

    /**
     * Comparing with another Child object
     * @param o - another Child object to compare
     * @return  true - if all the fields are equal
     *          false - otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Child child = (Child) o;
        return name.equals(child.name) &&
                sex.equals(child.sex) &&
                birthDateMillis.equals(child.birthDateMillis);
    }
}

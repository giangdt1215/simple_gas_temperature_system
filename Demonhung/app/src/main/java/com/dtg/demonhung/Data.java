package com.dtg.demonhung;

/**
 * Created by giang on 10/05/2017.
 */

public class Data {

    private float nhietDo;
    private float doAm;
    private float co;
    private float smoke;
    private float lpg;

    public Data() {
    }

    public Data(float nhietDo, float doAm, float co, float smoke, float lpg) {
        this.nhietDo = nhietDo;
        this.doAm = doAm;
        this.co = co;
        this.smoke = smoke;
        this.lpg = lpg;
    }

    public float getNhietDo() {
        return nhietDo;
    }

    public void setNhietDo(float nhietDo) {
        this.nhietDo = nhietDo;
    }

    public float getDoAm() {
        return doAm;
    }

    public void setDoAm(float doAm) {
        this.doAm = doAm;
    }

    public float getCo() {
        return co;
    }

    public void setCo(float co) {
        this.co = co;
    }

    public float getSmoke() {
        return smoke;
    }

    public void setSmoke(float smoke) {
        this.smoke = smoke;
    }

    public float getLpg() {
        return lpg;
    }

    public void setLpg(float lpg) {
        this.lpg = lpg;
    }
}

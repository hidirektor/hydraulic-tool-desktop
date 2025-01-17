package com.hidirektor.dashboard.utils.Model.Hydraulic;

public class Kabin {

    public String tankName;
    public String kabinName;

    public String gecisOlculeri;
    public String kabinOlculeri;
    public String tankOlculeri;

    public int kabinHacim;

    public int kabinGecisX;
    public int kabinGecisY;
    public int kabinGecisH;

    public int kabinDisX;
    public int kabinDisY;
    public int kabinDisH;

    public int tankDisX;
    public int tankDisY;
    public int tankDisH;

    public String kabinKodu;
    public String yagTankiKodu;
    public String malzemeAdi;

    public Kabin(String tankName, String kabinName, int kabinHacim, int kabinGecisX, int kabinGecisY, int kabinGecisH, int kabinDisX, int kabinDisY, int kabinDisH, int tankDisX, int tankDisY, int tankDisH, String kabinKodu, String yagTankiKodu, String malzemeAdi) {
        this.tankName = tankName;
        this.kabinName = kabinName;
        this.kabinHacim = kabinHacim;
        this.kabinGecisX = kabinGecisX;
        this.kabinGecisY = kabinGecisY;
        this.kabinGecisH = kabinGecisH;
        this.kabinDisX = kabinDisX;
        this.kabinDisY = kabinDisY;
        this.kabinDisH = kabinDisH;
        this.tankDisX = tankDisX;
        this.tankDisY = tankDisY;
        this.tankDisH = tankDisH;
        this.kabinKodu = kabinKodu;
        this.yagTankiKodu = yagTankiKodu;
        this.malzemeAdi = malzemeAdi;

        this.gecisOlculeri = kabinGecisX + "x" + kabinGecisY + "x" + kabinGecisH;
        this.kabinOlculeri = kabinDisX + "x" + kabinDisY + "x" + kabinDisH;
        this.tankOlculeri = tankDisX + "x" + tankDisY + "x" + tankDisH;
    }

    public String getTankName() {
        return tankName;
    }

    public void setTankName(String tankName) {
        this.tankName = tankName;
    }

    public String getKabinName() {
        return kabinName;
    }

    public void setKabinName(String kabinName) {
        this.kabinName = kabinName;
    }

    public String getGecisOlculeri() {
        return gecisOlculeri;
    }

    public void setGecisOlculeri(String gecisOlculeri) {
        this.gecisOlculeri = gecisOlculeri;
    }

    public String getKabinOlculeri() {
        return kabinOlculeri;
    }

    public void setKabinOlculeri(String kabinOlculeri) {
        this.kabinOlculeri = kabinOlculeri;
    }

    public int getKabinHacim() {
        return kabinHacim;
    }

    public void setKabinHacim(int kabinHacim) {
        this.kabinHacim = kabinHacim;
    }

    public String getTankOlculeri() {
        return tankOlculeri;
    }

    public void setTankOlculeri(String tankOlculeri) {
        this.tankOlculeri = tankOlculeri;
    }

    public int getKabinGecisX() {
        return kabinGecisX;
    }

    public void setKabinGecisX(int kabinGecisX) {
        this.kabinGecisX = kabinGecisX;
    }

    public int getKabinGecisY() {
        return kabinGecisY;
    }

    public void setKabinGecisY(int kabinGecisY) {
        this.kabinGecisY = kabinGecisY;
    }

    public int getKabinGecisH() {
        return kabinGecisH;
    }

    public void setKabinGecisH(int kabinGecisH) {
        this.kabinGecisH = kabinGecisH;
    }

    public int getKabinDisX() {
        return kabinDisX;
    }

    public void setKabinDisX(int kabinDisX) {
        this.kabinDisX = kabinDisX;
    }

    public int getKabinDisY() {
        return kabinDisY;
    }

    public void setKabinDisY(int kabinDisY) {
        this.kabinDisY = kabinDisY;
    }

    public int getKabinDisH() {
        return kabinDisH;
    }

    public void setKabinDisH(int kabinDisH) {
        this.kabinDisH = kabinDisH;
    }

    public int getTankDisX() {
        return tankDisX;
    }

    public void setTankDisX(int tankDisX) {
        this.tankDisX = tankDisX;
    }

    public int getTankDisY() {
        return tankDisY;
    }

    public void setTankDisY(int tankDisY) {
        this.tankDisY = tankDisY;
    }

    public int getTankDisH() {
        return tankDisH;
    }

    public void setTankDisH(int tankDisH) {
        this.tankDisH = tankDisH;
    }

    public String getKabinKodu() {
        return kabinKodu;
    }

    public void setKabinKodu(String kabinKodu) {
        this.kabinKodu = kabinKodu;
    }

    public String getYagTankiKodu() {
        return yagTankiKodu;
    }

    public void setYagTankiKodu(String yagTankiKodu) {
        this.yagTankiKodu = yagTankiKodu;
    }

    public String getMalzemeAdi() {
        return malzemeAdi;
    }

    public void setMalzemeAdi(String malzemeAdi) {
        this.malzemeAdi = malzemeAdi;
    }
}

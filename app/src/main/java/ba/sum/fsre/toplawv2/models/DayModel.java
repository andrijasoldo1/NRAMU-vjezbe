package ba.sum.fsre.toplawv2.models;

import java.util.List;

public class DayModel {
    public int day;
    public boolean isInCurrentMonth;
    public List<Integer> dots; // možeš koristiti boje, prioritet, broj događaja itd.

    public DayModel(int day, boolean isInCurrentMonth, List<Integer> dots) {
        this.day = day;
        this.isInCurrentMonth = isInCurrentMonth;
        this.dots = dots;
    }
}
package org.akaza.openclinica.control.submit;
import java.util.List;
public class Schedule {
    private String filepath;
    private List<List<Object>> schedule_week_and_date;

    // getters y setters
    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public List<List<Object>> getSchedule_week_and_date() {
        return schedule_week_and_date;
    }

    public void setSchedule_week_and_date(List<List<Object>> schedule_week_and_date) {
        this.schedule_week_and_date = schedule_week_and_date;
    }
}

package org.akaza.openclinica.control.submit;
import com.fasterxml.jackson.annotation.JsonProperty;
public class SuccessResponse {
    @JsonProperty(value = "id", required = false)
    private int id;
    @JsonProperty(value = "pid", required = false)
    private String pid;
    @JsonProperty(value = "success", required = false)
    private String success;
    @JsonProperty(value = "libreclinica_id", required = false)
    private String libreclinicaId;
    @JsonProperty(value = "schedule", required = false)
    private Schedule schedule;
    @JsonProperty(value = "regimen_strata", required = false)
    private RegimenStrataResponse regimen;
    @JsonProperty(value = "arm", required = false)
    private String arm;

    public SuccessResponse() {
    }

    public SuccessResponse(String pid, String success, String libreclinicaId, Schedule schedule, int id) {
        this.pid = pid;
        this.success = success;
        this.libreclinicaId = libreclinicaId;
        this.schedule = schedule;
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }
    public String getLibreclinca_id() {
        return libreclinicaId;
    }
    public void setLibreclinca_id(String libreclincaId) {
        this.libreclinicaId = libreclincaId;
    }
    public Schedule getSchedule() {
        return schedule;
    }
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setRegimen(RegimenStrataResponse regimen) {
        this.regimen = regimen;
    }
    public RegimenStrataResponse getRegimen() {
        return regimen;
    }

    public String getArm() {
        return arm;
    }

    public void setArm(String arm) {
        this.arm = arm;
    }
}

package dev.ctrlspace.bootcamp_2025_03.model.dto;

import java.util.List;

public class UserProfileSettingsDTO {
    private String introduction;
    private String nickname;
    private String job;
    private List<String> traits;
    private String notes;

    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getJob() { return job; }
    public void setJob(String job) { this.job = job; }

    public List<String> getTraits() { return traits; }
    public void setTraits(List<String> traits) { this.traits = traits; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

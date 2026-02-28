package com.example.recipemasterpro.models;

public class Step {
    private int stepNumber;
    private String instruction;
    private String videoUrl;
    private String imageUrl;
    private int duration;

    public Step() {}

    public Step(int stepNumber, String instruction) {
        this.stepNumber = stepNumber;
        this.instruction = instruction;
    }

    public int getStepNumber() { return stepNumber; }
    public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }

    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
}
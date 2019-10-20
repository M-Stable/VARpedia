package VARpedia;

import java.io.Serializable;

public class Creation implements Serializable {
    private String name;
    private int confidenceRating;
    private String viewTime;

    public Creation(String name, int confidenceRating, String viewCount) {
        this.name = name;
        this.confidenceRating = confidenceRating;
        this.viewTime = viewCount;
    }

    public String getName() {
        return name;
    }

    public int getConfidenceRating() {
        return confidenceRating;
    }

    public void setConfidenceRating(int confidenceRating) {
        this.confidenceRating = confidenceRating;
    }

    public String getViewTime() {
        return viewTime;
    }

    public void setViewTime(String viewCount) {
        this.viewTime = viewCount;
    }
}

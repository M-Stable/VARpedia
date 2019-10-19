package VARpedia;

public class Creation {
    private String name;
    private int confidenceRating;
    private int viewCount;

    public Creation(String name, int confidenceRating, int viewCount) {
        this.name = name;
        this.confidenceRating = confidenceRating;
        this.viewCount = viewCount;
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

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
}

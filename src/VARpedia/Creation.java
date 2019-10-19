package VARpedia;

public class Creation {
    private String name;
    private int confidenceRating;
    private int viewCount;

    public Creation(String _name, int _confidenceRating, int _viewCount) {
        this.name = _name;
        this.confidenceRating = _confidenceRating;
        this.viewCount = _viewCount;
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

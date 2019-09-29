package WikiSpeak;

import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

public class VideoCreationTask extends Task<String> {

    private List<File> images;
    private Integer numImages;
    private Double audioDuration;
    private String creationName;
    private String searchTerm;

    public VideoCreationTask(List<File> images, Double audioDuration, String creationName, String searchTerm) {
        this.images = images;
        this.numImages = images.size();
        this.audioDuration = audioDuration;
        this.creationName = creationName;
        this.searchTerm = searchTerm;
    }

    @Override
    protected String call() throws Exception {
        double imageFrameRate = 1 / (audioDuration / numImages);
        String filter2 = "-vf \"drawtext=fontfile=./myfont.ttf:fontsize=100: fontcolor=red:x=(w-text_w)/2:y=(h-text_h)/2:text='" + searchTerm + "'\" ";
        String filter = "-vf \"scale=1280:720:force_original_aspect_ratio=decrease,pad=1280:720:(ow-iw)/2:(oh-ih)/2\"";

        String input = "cat images/*.jpg | ffmpeg -f image2pipe -framerate " + imageFrameRate + " -i - -i creations/merged.wav -c:v libx264 -pix_fmt yuv420p  ";

        String command = input + filter + " -r 25 -max_muxing_queue_size 1024 -y creations/out.mp4";
        String command2 = "ffmpeg -i creations/out.mp4 " + filter2 + "'creations/" + creationName + ".mp4'";

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        Process process = pb.start();

        process.waitFor();

        ProcessBuilder pb2 = new ProcessBuilder("bash", "-c", command2);
        Process process2 = pb2.start();

        process2.waitFor();

        return null;
    }
}

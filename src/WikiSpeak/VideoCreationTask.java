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

    public VideoCreationTask(List<File> images, Double audioDuration, String creationName) {
        this.images = images;
        this.numImages = images.size();
        this.audioDuration = audioDuration;
        this.creationName = creationName;
    }

    @Override
    protected String call() throws Exception {
        String output = "";
        double imageFrameRate = 1 / (audioDuration / numImages);
        String filter = "-vf \"scale=-2:270, drawtext=fontfile=./myfont.ttf:fontsize=50: fontcolor=red:x=trunc((w-text_w)/2):y=trunc((h-text_h)/2):text='" + creationName + "\"";

        String input = "cat images/*.jpg | ffmpeg -f image2pipe -framerate " + imageFrameRate + " -i - -i creations/merged.wav -c:v libx264 -pix_fmt yuv420p  ";

        String command = input + filter + " -r 25 -max_muxing_queue_size 1024 -y creations/" + creationName + ".mp4";

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        Process process = pb.start();

        BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        int exitStatus = process.waitFor();

        if (exitStatus == 0) {
            output = stdout.readLine();
        } else {
            String line;
            while ((line = stderr.readLine()) != null) {
                System.err.println(line);
            }
        }
        return output;
    }
}

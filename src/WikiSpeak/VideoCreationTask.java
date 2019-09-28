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

        String command = "ffmpeg -framerate " + imageFrameRate + " -i images/" + creationName + "%01d.jpg -vf \"scale=-2:270, drawtext=fontfile=./myfont.ttf:fontsize=50: fontcolor=red:x=trunc((w-text_w)/2):y=trunc((h-text_h)/2):text='" + creationName + "\" -r 25 video/video.mp4";

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        Process process = pb.start();

        BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        System.out.println(command);
        int exitStatus = process.waitFor();
        System.out.println("done");

        if (exitStatus == 0) {
            String command2 = "ffmpeg -i video/video.mp4 -i creations/merged.wav -map 0:v -map 1:a creations/" + creationName + ".mp4";
            ProcessBuilder pb2 = new ProcessBuilder("bash", "-c", command2);
            Process process2 = pb2.start();

            BufferedReader stdout2 = new BufferedReader(new InputStreamReader(process2.getInputStream()));
            BufferedReader stderr2 = new BufferedReader(new InputStreamReader(process2.getErrorStream()));

            System.out.println(command2);
            int exitStatus2 = process2.waitFor();
            System.out.println("done2");
            if (exitStatus == 0) {
                output = stdout2.readLine();
            } else {
                String line;
                while ((line = stderr2.readLine()) != null) {
                    System.err.println(line);
                }
            }
        } else {
            String line;
            while ((line = stderr.readLine()) != null) {
                System.err.println(line);
            }
        }
        return output;
    }
}

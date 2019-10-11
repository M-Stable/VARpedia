package VARpedia;

import javafx.concurrent.Task;

import java.io.File;
import java.util.List;

public class VideoCreationTask extends Task<String> {

    private List<File> images;
    private Integer numImages;
    private Double audioDuration;
    private String creationName;
    private String searchTerm;
    private String music;

    public VideoCreationTask(List<File> images, Double audioDuration, String creationName, String searchTerm, String music) {
        this.images = images;
        this.numImages = images.size();
        this.audioDuration = audioDuration;
        this.creationName = creationName;
        this.searchTerm = searchTerm;
        this.music = music;
    }

    @Override
    protected String call() throws Exception {

        /*
          Calculate the needed framerate required for all images to take an even amount of time in the full video
         */
        double imageFrameRate = 1 / (audioDuration / numImages);

        /*
          Setup required for bash commands needed to create a video from images and an audio file, and then add text over it
         */
        String input = "cat images/*.jpg | ffmpeg -f image2pipe -framerate " + imageFrameRate + " -i - -i creations/merged.wav -c:v libx264 -pix_fmt yuv420p  ";

        String filter = "-vf \"scale=1280:720:force_original_aspect_ratio=decrease,pad=1280:720:(ow-iw)/2:(oh-ih)/2\"";
        String filter2 = "-vf \"drawtext=fontfile=./myfont.ttf:fontsize=100: fontcolor=red:x=(w-text_w)/2:y=(h-text_h)/2:text='" + searchTerm + "'\" ";

        String command = input + filter + " -r 25 -max_muxing_queue_size 1024 -y creations/out.mp4";
        String command2 = "ffmpeg -i creations/out.mp4 " + filter2 + " -y 'creations/" + creationName + ".mp4'";

        /*
          Run the bash commands one after the other
         */
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        Process process = pb.start();

        process.waitFor();

        ProcessBuilder pb2 = new ProcessBuilder("bash", "-c", command2);
        Process process2 = pb2.start();

        process2.waitFor();

        if(music != null && music != "None") {
            String command3 = "ffmpeg -i 'music/" + music + ".mp3' -i 'creations/"+ creationName + ".mp4' -filter_complex \"[0:0]volume=0.1[a1]; [1:1]volume=1[a2]; [a1][a2]amerge\" -y creations/out.mp3";
            String command4 = "ffmpeg -i creations/out.mp3 -i creations/out.mp4 -y 'creations/" + creationName + ".mp4'";

            ProcessBuilder pb3 = new ProcessBuilder("bash", "-c", command3);
            Process process3 = pb3.start();

            process3.waitFor();

            ProcessBuilder pb4 = new ProcessBuilder("bash", "-c", command4);
            Process process4 = pb4.start();

            process4.waitFor();
        }

        return null;
    }
}

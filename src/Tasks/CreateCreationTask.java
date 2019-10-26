package Tasks;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class CreateCreationTask extends Task {

    private ObservableList<String> audioCreationList;
    private List<File> images;
    private Integer numImages;
    private String creationName;
    private String searchTerm;
    private String music;

    public CreateCreationTask(ObservableList<String> audioCreationList, List<File> images, String creationName, String searchTerm, String music) {
        this.audioCreationList = audioCreationList;
        this.images = images;
        this.numImages = images.size();
        this.creationName = creationName;
        this.searchTerm = searchTerm;
        this.music = music;
    }

    @Override
    protected Object call() throws Exception {
        mergeAudio();
        createVideo(getAudioDuration());
        return null;
    }

    public int mergeAudio() throws Exception {
         /*
        Setup the ffmpeg command to merge all the given audio files
         */
        int count = 0;
        String command = "ffmpeg ";
        for(String creation1 : audioCreationList) {
            String creation = "audioCreation/" + creation1 + ".wav";
            command += "-i '" + creation + "' ";
            count++;
        }
        command += "-filter_complex '";
        for (int i = 0; i<count; i++) {
            command += "[" + i + ":0]";
        }
        command += "concat=n=" + count + ":v=0:a=1[out]' -map '[out]' creations/merged.wav";

        /*
        Run the ffmpeg command
         */
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        Process process = pb.start();
        int exitStatus = process.waitFor();
        if (exitStatus == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    public void createVideo(Double audioDuration) throws Exception {
         /*
          Calculate the needed framerate required for all images to take an even amount of time in the full video
         */
        double imageFrameRate = 1 / (audioDuration / numImages);

        /*
          Setup required for bash commands needed to create a video from images and an audio file, and then add text over it
         */
        String input = "cat images/*.jpg | ffmpeg -f image2pipe -framerate " + imageFrameRate + " -i - -i creations/merged.wav -c:v libx264 -pix_fmt yuv420p  ";

        String filter = "-vf \"scale=720:480:force_original_aspect_ratio=decrease,pad=720:480:(ow-iw)/2:(oh-ih)/2\"";
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
            String command4 = "ffmpeg -i creations/out.mp3 -i 'creations/" + creationName + ".mp4' -y creations/temp.mp4";

            ProcessBuilder pb3 = new ProcessBuilder("bash", "-c", command3);
            Process process3 = pb3.start();

            process3.waitFor();

            ProcessBuilder pb4 = new ProcessBuilder("bash", "-c", command4);
            Process process4 = pb4.start();

            process4.waitFor();

            File creation = new File("creations/temp.mp4");
            File noMusicCreation = new File("creations/" + creationName + ".mp4");
            creation.renameTo(noMusicCreation);
        }
    }

    private double getAudioDuration(){
        File audioFile = new File("creations/merged.wav");
        double audioDuration = 0;
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat audioFormat = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            audioDuration = frames / audioFormat.getFrameRate();
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
        return audioDuration;
    }
}

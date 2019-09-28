package WikiSpeak;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.File;
import java.util.Arrays;

public class MergeAudio extends Task<Integer> {

    private ObservableList<String> audioCreationList;

    public MergeAudio(ObservableList<String>  audioCreationList) {
        this.audioCreationList = audioCreationList;
    }

    @Override
    protected Integer call() throws Exception {

        int count = 0;
        String command = "ffmpeg ";
        for(String creation1 : audioCreationList) {
            System.out.println(creation1);
            String creation = "audioCreation/" + creation1 + ".wav";
            System.out.println(creation);
            command += "-i '" + creation + "' ";
            count++;
        }
        System.out.println(command);
        command += "-filter_complex '";
        for (int i = 0; i<count; i++) {
            command += "[" + i + ":0]";
        }
        command += "concat=n=" + count + ":v=0:a=1[out]' -map '[out]' creations/merged.wav";

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        Process process = pb.start();
        int exitStatus = process.waitFor();
        if (exitStatus == 0) {
            return 1;
        }

        return null;
    }
}

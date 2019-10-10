package VARpedia;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class MergeAudio extends Task<Integer> {

    private ObservableList<String> audioCreationList;

    public MergeAudio(ObservableList<String>  audioCreationList) {
        this.audioCreationList = audioCreationList;
    }

    @Override
    protected Integer call() throws Exception {

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
            return 1;
        }

        return null;
    }
}

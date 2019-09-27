package WikiSpeak;

import javafx.concurrent.Task;

import java.io.File;

public class MergeAudio extends Task<Integer> {

    private File audioDir = new File("audioCreation/");

    @Override
    protected Integer call() throws Exception {
        int count = 0;
        String command = "ffmpeg ";
        File[] creations = audioDir.listFiles();
        for(File creation : creations) {
            if(creation.getName().contains(".wav")) {
                command += "-i " + creation + " ";
                count++;
            }
        }
        command += "-filter_complex '";
        for (int i = 0; i<count; i++) {
            command += "[" + i + ":0]";
        }
        command += "concat=n=" + count + ":v=0:a=1[out]' -map '[out]' creations/merged.wav";
        System.out.println(command);
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        Process process = pb.start();
        int exitStatus = process.waitFor();
        if (exitStatus == 0) {
            return 1;
        }

        return null;
    }
}

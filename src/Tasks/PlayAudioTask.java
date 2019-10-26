package Tasks;

import javafx.concurrent.Task;

import java.io.IOException;

public class PlayAudioTask extends Task<String> {

    private String file;
    Process process;

    public PlayAudioTask(String file) {
        this.file = file;
    }

    @Override
    protected String call() throws Exception {
        String command = "ffplay -nodisp -autoexit " + file ;
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        try {
            process = pb.start();
            if (process.waitFor() == 0) {
                return "done";
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return command;
    }

    public void stopAudio() {
        process.destroy();
    }
}

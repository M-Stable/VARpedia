package WikiSpeak;

import javafx.concurrent.Task;


public class AudioTask extends Task<String> {

    private String text;

    public AudioTask (String text) {
        this.text = text;
    }

    @Override
    protected String call() throws Exception {
        String command = "cat " + text + " | text2wave -o output.wav";
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        try {
            pb.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "yes";
    }
}

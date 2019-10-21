package Tasks;

import javafx.concurrent.Task;

public class PreviewAudioTask extends Task<String> {

    private String comboBoxValue;
    private String highlightedText;
    private Process process;

    public PreviewAudioTask(String comboBoxValue, String highlightedText) {
        this.comboBoxValue = comboBoxValue;
        this.highlightedText = highlightedText;
    }

    @Override
    protected String call() throws Exception {
        /*
        Setup the audio creation command depending on which speech synthesiser was selected
         */
        String command = "";
        if (comboBoxValue.equals("Deep Voice")) {
            command = "echo \"" + highlightedText + "\" | festival --tts";
        } else if (comboBoxValue.equals("Light Voice")) {
            command = "espeak \"" + highlightedText + "\"";
        }

        /*
        Run the audio creation command and check if it was successful
         */
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        try {
            process = pb.start();
            int exitStatus = process.waitFor();
            if (exitStatus == 0) {
                return "done";
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void stopAudio() {
        process.destroy();
    }
}

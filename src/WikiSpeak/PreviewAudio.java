package WikiSpeak;

import javafx.concurrent.Task;

public class PreviewAudio extends Task<String> {

    private String comboBoxValue;
    private String highlightedText;

    public PreviewAudio(String comboBoxValue, String highlightedText) {
        this.comboBoxValue = comboBoxValue;
        this.highlightedText = highlightedText;
    }

    @Override
    protected String call() throws Exception {
        String command = "";
        if (comboBoxValue.equals("Festival")) {
            command = "echo \"" + highlightedText + "\" | festival --tts";
        } else if (comboBoxValue.equals("eSpeak")) {
            command = "espeak \"" + highlightedText + "\"";

        }
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        try {
            Process process = pb.start();
            int exitStatus = process.waitFor();
            if (exitStatus == 0) {
                return null;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

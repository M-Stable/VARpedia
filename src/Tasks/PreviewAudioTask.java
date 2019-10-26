package Tasks;

import javafx.concurrent.Task;

public class PreviewAudioTask extends Task<String> {

    private String synthesiser;
    private String highlightedText;
    String output = "";

    public PreviewAudioTask(String synthesiser, String highlightedText) {
        this.synthesiser = synthesiser;
        this.highlightedText = highlightedText;
    }

    @Override
    protected String call() throws Exception {
        String command = "";
        if (synthesiser.equals("Deep Voice")) {
            command = "echo \"" + highlightedText + "\" | text2wave -o './audioCreation/audioTemp.wav'";
        } else if (synthesiser.equals("Light Voice")) {
            command = "espeak \"" + highlightedText + "\" -w './audioCreation/audioTemp.wav'";
        }

        /*
        Run the audio creation command and check if it was successful
         */
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        try {
            Process process = pb.start();
            int exitStatus = process.waitFor();
            if (exitStatus == 0) {
                output ="yes";
            } else {
                output = "no";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }
}

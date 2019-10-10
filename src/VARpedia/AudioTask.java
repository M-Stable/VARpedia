package VARpedia;

import javafx.concurrent.Task;


public class AudioTask extends Task<String> {

    private String text;
    private String synthesiser;
    private String fileName;

    public AudioTask (String text, String synthesiser, String fileName) {
        this.text = text;
        this.synthesiser = synthesiser;
        this.fileName = fileName;
    }

    @Override
    protected String call() throws Exception {
        String output = "";

        /*
        Setup the audio creation command depending on which speech synthesiser was selected
         */
        String command = "";
        if (synthesiser.equals("Festival")) {
            command = "echo \"" + text + "\" | text2wave -o './audio/" + fileName + "_" + synthesiser + ".wav'";
        } else if (synthesiser.equals("eSpeak")) {
            command = "espeak \"" + text + "\" -w './audio/" + fileName + "_" + synthesiser + ".wav'";
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

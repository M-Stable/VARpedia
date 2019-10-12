package VARpedia;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.io.File;


public class AudioTask extends Task<String> {

    private String text;
    private String synthesiser;
    private String fileName;
    private int festivalCount = 0;
    private int espeakCount = 0;
    private boolean exists = true;

    public AudioTask (String text, String synthesiser, String fileName) {
        this.text = text;
        this.synthesiser = synthesiser;
        this.fileName = fileName;
    }

    @Override
    protected String call() throws Exception {
        String output = "";

        while (exists) {
            if (synthesiser.equals("Festival")) {
                File tmpDir1 = new File("audioCreation/" + fileName + "_" + synthesiser + "_" + festivalCount + ".wav");

                if (tmpDir1.exists()) {
                    exists = true;
                } else {
                    exists = false;
                }

                if (exists) {
                    festivalCount++;
                }
            } else if (synthesiser.equals("eSpeak")) {
                File tmpDir1 = new File("audioCreation/" + fileName + "_" + synthesiser + "_" + espeakCount + ".wav");

                if (tmpDir1.exists()) {
                    exists = true;
                } else {
                    exists = false;
                }

                if (exists) {
                    espeakCount++;
                }
            }
        }

        /*
        Setup the audio creation command depending on which speech synthesiser was selected
         */
        String command = "";
        if (synthesiser.equals("Festival")) {
            command = "echo \"" + text + "\" | text2wave -o './audioCreation/" + fileName + "_" + synthesiser + "_" + festivalCount + ".wav'";
        } else if (synthesiser.equals("eSpeak")) {
            command = "espeak \"" + text + "\" -w './audioCreation/" + fileName + "_" + synthesiser + "_" + espeakCount + ".wav'";
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

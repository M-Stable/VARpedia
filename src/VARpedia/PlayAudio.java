package VARpedia;

import java.io.IOException;

public class PlayAudio extends Thread{

    private String file;

    public PlayAudio(String file) {
        this.file = file;
    }

    public void run(){
        String command = "ffplay -nodisp " + file ;
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        try {
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

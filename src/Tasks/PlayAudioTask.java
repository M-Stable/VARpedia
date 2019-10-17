package Tasks;

import java.io.IOException;

public class PlayAudioTask extends Thread{

    private String file;

    public PlayAudioTask(String file) {
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

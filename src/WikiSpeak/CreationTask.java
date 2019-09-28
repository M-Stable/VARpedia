package WikiSpeak;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.*;
import javafx.concurrent.Task;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

public class CreationTask extends Task<Integer> {

    private File audioDir = new File("audioCreation/");
    private Integer numImages;
    private String creationName;

    public CreationTask(Integer numImages, String creationName) {
        this.numImages = numImages;
        this.creationName = creationName;
    }

    @Override
    protected Integer call() throws Exception {
        if(mergeAudio() == 1) {
            return 1;
        } else if(getFlickrImages() == 1) {
            return 2;
        } else  if(createVideo() == 1) {
            return 3;
        } else {
            return 0;
        }
    }

    public String getAPIKey(String specifier) throws IOException {
        File config = new File("flickr-api-keys.txt");
        BufferedReader reader = new BufferedReader(new FileReader(config));

        String line;

        while((line = reader.readLine()) != null) {
            if(line.trim().startsWith(specifier)) {
                reader.close();
                // System.out.println(line.substring(line.indexOf("=")+1).trim());
                return line.substring(line.indexOf("=")+1).trim();
            }
        }
        reader.close();
        return null;
    }

    private Integer mergeAudio() throws Exception{
        File[] creations = audioDir.listFiles();
        Arrays.sort(creations, (f1, f2) -> f1.compareTo(f2));
        int count = 0;
        String command = "ffmpeg ";
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

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        Process process = pb.start();
        int exitStatus = process.waitFor();
        if (exitStatus == 0) {
            return 1;
        }
        return null;

    }

    private Integer getFlickrImages() throws IOException, FlickrException {
        String apiKey = getAPIKey("apiKey");
        String sharedSecret = getAPIKey("sharedSecret");
        Flickr flickr = new Flickr(apiKey, sharedSecret, new REST());
        PhotosInterface photos = flickr.getPhotosInterface();
        SearchParameters params = new SearchParameters();
        params.setSort(SearchParameters.RELEVANCE);
        params.setMedia("photos");
        params.setText(creationName);

        PhotoList<Photo> results = photos.search(params, numImages, 0);

        if(results.size() == 0) {
            return 1;
        }

        int imageID = 0;
        for(Photo photo : results) {
            try {
                BufferedImage image = photos.getImage(photo, Size.LARGE);
                String filename = creationName.trim()+imageID+".jpg";
                File outputFile = new File("images/", filename);
                //images.add(outputFile);
                ImageIO.write(image, "jpg", outputFile);
                imageID++;
            } catch (FlickrException flickrException) {

            }
        }

        return 0;

    }

    private Integer createVideo() throws Exception {

        File audioFile = new File("creations/merged.wav");
        double audioDuration = 0;
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat audioFormat = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            audioDuration = frames / audioFormat.getFrameRate();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        double imageFrameRate = 1 / (audioDuration / numImages);

        String command = "ffmpeg -framerate " + imageFrameRate + " -i images/" + creationName + "%01d.jpg -vf \"scale=-2:270, drawtext=fontfile=./myfont.ttf:fontsize=50: fontcolor=red:x=trunc((w-text_w)/2):y=trunc((h-text_h)/2):text='" + creationName + "\" -r 25 video/video.mp4";

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        Process process = pb.start();

        BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        int exitStatus = process.waitFor();

        if (exitStatus == 0) {
            String command2 = "ffmpeg -i video/video.mp4 -i creations/merged.wav -map 0:v -map 1:a creations/" + creationName + ".mp4";
            ProcessBuilder pb2 = new ProcessBuilder("bash", "-c", command2);
            Process process2 = pb2.start();

            BufferedReader stdout2 = new BufferedReader(new InputStreamReader(process2.getInputStream()));
            BufferedReader stderr2 = new BufferedReader(new InputStreamReader(process2.getErrorStream()));

            int exitStatus2 = process2.waitFor();
            if (exitStatus == 0) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }


    /*CreationTask creationTask = new CreationTask((Integer) spinner.getValue(), textCreationName.getText());
                executorService.submit(creationTask);
                creationTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent workerStateEvent) {
                        cleanUp();
                        initialiseTable();
                        progressBar.setVisible(false);
                    }
                });*/

}

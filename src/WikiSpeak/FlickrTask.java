package WikiSpeak;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.*;
import javafx.concurrent.Task;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FlickrTask extends Task<String> {

    private Integer numImages;
    private String query;
    private List<File> images;


    public FlickrTask(Integer numImages, String query) {
        this.numImages = numImages;
        this.query = query;
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

    @Override
    protected String call() throws Exception {
        String apiKey = getAPIKey("apiKey");
        String sharedSecret = getAPIKey("sharedSecret");
        Flickr flickr = new Flickr(apiKey, sharedSecret, new REST());
        PhotosInterface photos = flickr.getPhotosInterface();
        SearchParameters params = new SearchParameters();
        params.setSort(SearchParameters.RELEVANCE);
        params.setMedia("photos");
        params.setText(query);

        PhotoList<Photo> results = photos.search(params, numImages, 0);

        images = new ArrayList<File>();

        if(results.size() == 0) {
            return "fail";
        }

        int imageID = 0;
        for(Photo photo : results) {
            try {
                BufferedImage image = photos.getImage(photo, Size.LARGE);

             /*   if(image.getWidth()%2!=0) {
                    photo.setOriginalWidth(photo.getOriginalWidth() + 1);
                    image = photos.getImage(photo, Size.LARGE);
                } else  if(image.getHeight()%2!=0) {
                    photo.setOriginalHeight(photo.getOriginalHeight() + 1);
                    image = photos.getImage(photo, Size.LARGE);
                }*/
                String filename = query.trim()+imageID+".jpg";
                File outputFile = new File("images/", filename);
                images.add(outputFile);
                ImageIO.write(image, "jpg", outputFile);
                imageID++;
            } catch (FlickrException flickrException) {

            }
        }

        return "yes";
    }

    public List<File> getImages() {
        return images;
    }
}

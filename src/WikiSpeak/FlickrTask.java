package WikiSpeak;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.*;
import javafx.concurrent.Task;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class FlickrTask extends Task<String> {

    private Integer numImages;
    private String query;


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
        System.out.println("Retrieving " + results.size() + " results");

        for(Photo photo : results) {
            try {
                BufferedImage image = photos.getImage(photo, Size.LARGE);
                String filename = query.trim().replace(' ', '-')+"-"+System.currentTimeMillis()+"-"+photo.getId()+".jpg";
                File outputFile = new File("images/", filename);
                ImageIO.write(image, "jpg", outputFile);
            } catch (FlickrException flickrException) {

            }
        }

        return "yes";
    }
}

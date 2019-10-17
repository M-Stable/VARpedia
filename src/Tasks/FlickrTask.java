package Tasks;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.*;
import javafx.concurrent.Task;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    /*
    Get Flickr API key and shared secret from file
     */
    public String getAPIKey(String specifier) throws IOException {
        File config = new File("flickr-api-keys.txt");
        BufferedReader reader = new BufferedReader(new FileReader(config));

        String line;

        while((line = reader.readLine()) != null) {
            if(line.trim().startsWith(specifier)) {
                reader.close();
                return line.substring(line.indexOf("=")+1).trim();
            }
        }
        reader.close();
        return null;
    }

    @Override
    protected String call() throws Exception {
        /*
        Get api key and shared secret
         */
        String apiKey = getAPIKey("apiKey");
        String sharedSecret = getAPIKey("sharedSecret");

        /*
        Setup connection with Flickr as well as image search specifiers
         */
        Flickr flickr = new Flickr(apiKey, sharedSecret, new REST());
        PhotosInterface photos = flickr.getPhotosInterface();
        SearchParameters params = new SearchParameters();
        params.setSort(SearchParameters.RELEVANCE);
        params.setMedia("photos");
        params.setText(query);

        /*
        Retrieve the desired images from Flickr and check if no images were retrieved
         */
        PhotoList<Photo> results = photos.search(params, numImages, 0);

        if(results.size() == 0) {
            return "fail";
        }

        /*
        Attempt to add each image to an an images array and write the image to file
         */
        images = new ArrayList<File>();
        int imageID = 0;
        for(Photo photo : results) {
            try {
                BufferedImage image = photos.getImage(photo, Size.LARGE);
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

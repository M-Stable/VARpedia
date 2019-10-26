package Tasks;

import javafx.concurrent.Task;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class PdfOpen extends Task<String> {

    @Override
    protected String call() throws Exception {
        if (Desktop.isDesktopSupported()) {
            try {
                File myFile = new File("UserManual.pdf");
                Desktop.getDesktop().open(myFile);
            } catch (Exception ignored) {
                // no application registered for PDFs
            }
        }
        return "done";
    }
}

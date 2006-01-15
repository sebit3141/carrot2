package carrot2.demo.swing;

import java.awt.BorderLayout;
import java.io.*;
import java.net.URL;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdesktop.jdic.browser.*;

/**
 * An implementation of {@link carrot2.demo.swing.HtmlDisplay} using
 * JDIC project's native component.
 * 
 * @author Dawid Weiss
 */
public class HtmlDisplayWithJDIC extends HtmlDisplay {
    private final static Logger log = Logger.getLogger(HtmlDisplayWithJDIC.class);
    private final WebBrowser browser;

    /**
     * A map of {@link URL} to {@link File} objects of temporary
     * files and their URLs saved by us and opened by JDIC. We use
     * this map to clean up after we're done with a particular HTML
     * file.
     */
    private final HashMap savedFiles = new HashMap();

    private final class InternalWebBrowserListener implements WebBrowserListener {
        public void downloadStarted(WebBrowserEvent event) {
        }

        public void downloadCompleted(WebBrowserEvent event) {
        }

        public void downloadProgress(WebBrowserEvent event) {
        }

        public void downloadError(WebBrowserEvent event) {
        }

        public void documentCompleted(WebBrowserEvent event) {
            synchronized (HtmlDisplayWithJDIC.this) {
                final URL url = browser.getURL();
                final File file = (File) savedFiles.get(url);
                if (file != null) {
                    if (file.delete() == false) {
                        log.warn("Could not delete temporary file :" + file);
                    }
                }
            }
        }

        public void titleChange(WebBrowserEvent event) {
        }

        public void statusTextChange(WebBrowserEvent event) {
        }
    }
    
    public HtmlDisplayWithJDIC() {
        this.browser = new WebBrowser();
        this.browser.setFocusable(false);
        
        this.setLayout(new BorderLayout());
        this.add(browser, BorderLayout.CENTER);

        this.browser.addWebBrowserListener(new InternalWebBrowserListener());
    }

    public void setContent(String htmlContent) {
        synchronized (this) {
            try {
                // BUGFIX: We dump the HTML to an external file because setContent
                // method does not convert (or detect) characters properly.
                final File tempFile = File.createTempFile("c2tmphtml", "html");
                // We try to do a cleanup manually, but instruct the JVM to delete
                // this file if we can't do it.
                tempFile.deleteOnExit();
                final FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(htmlContent.getBytes("UTF-8"));
                fos.close();

                final URL tempFileURL = tempFile.toURL();
                this.savedFiles.put(tempFileURL, tempFile);
                this.browser.setURL(tempFileURL);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Cannot create temporary file for HTML.");
            }
        }
    }
}
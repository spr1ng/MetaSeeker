/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eye.seeker;

import com.db4o.ObjectContainer;
import com.db4o.query.Predicate;
import eye.DBManagerEyeImpl;
import eye.ImageFactory;
import eye.models.Image;
import java.util.List;
import org.apache.log4j.Logger;
import static eye.net.InetUtils.*;

/**
 *
 * @author spr1ng
 * @version $Id: Seeker.java 29 2010-07-01 05:36:23Z spr1ng $
 */
public class Seeker {

    private static final Logger LOG = Logger.getLogger(Seeker.class);
    private static DBManagerEyeImpl dbm = new DBManagerEyeImpl();
    private ObjectContainer clientDB;

    public static void main(String[] args) throws InterruptedException{
        if (!hasInetConnection()) {
            LOG.error("Inet is unreachable! =(");
            System.exit(1);
        }
        Seeker seeker = new Seeker();
        while (true) {
            seeker.clientDB = dbm.getContainer();
            try {
                seeker.seekMetaData();
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                seeker.clientDB.close();
            }
            
        }
    }

    public void seekMetaData(){
        ImageFactory factory = new ImageFactory();
        //Ищем картинки без метаданных
        List<Image> images = clientDB.query(new Predicate<Image>() {
            @Override
            public boolean match(Image image) {
                return image.getPoints() == null;
            }
        });

        int idx = 0, size;
        size = images.size();
        LOG.info("new images found: " + size);
        for (Image image : images) {
            idx++;
            try {
                factory.produceMetaImage(image);
                clientDB.store(image);
                clientDB.commit();
                LOG.info("[" + idx + "/" + size +"] ---> stored: " + image.getUrl());
            } catch (Exception ex) {
                clientDB.delete(image);
                clientDB.commit();
                LOG.info("[" + idx+ "/" + size +"] ---> deleted: " + image.getUrl()
                       + " " + ex.getMessage());
            }
        }
    }

}

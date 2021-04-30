package Objects;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

public class ImageManipulation {

    public static String guestLink = "https://www.pngitem.com/pimgs/m/279-2799324_transparent-guest-png-become-a-member-svg-icon.png";
    public static String guestFormat = "png";

    public static void downloadImage(String link, String format, String destination){

        try{
            URL url = new URL(link);
            BufferedImage img = ImageIO.read(url);
            ImageIO.write(img, format, new File(destination));

        }catch (Exception e){
            System.out.println("Error in downloading");
        }
    }

    public static void convertFormat(String location, String format, String output){

        try{
            FileInputStream fileInputStream = new FileInputStream(new File(location));

            BufferedImage img = ImageIO.read(fileInputStream);
            BufferedImage imgOut = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

            for(int i=0; i<img.getWidth(); i++)
                for(int j=0; j<img.getHeight(); j++)
                    imgOut.setRGB(i, j, img.getRGB(i,j));

            ImageIO.write(imgOut, format, new File(output));

        }catch (Exception e){
            System.out.println("Error in converting format " + e);
        }
    }

    public static String imageToString(String path){

        String ret = "";

        try{
            File f = new File(path);

            FileInputStream fileInputStream = new FileInputStream(f);
            byte[] b = new byte[(int) f.length()];
            fileInputStream.read(b);

            ret = new String(Base64.getEncoder().encodeToString(b));
            fileInputStream.close();

        }catch (Exception e){
            System.out.println("Exception in imageToString " + e);
        }

        return ret;
    }

    public static BufferedImage stringToImage(String data){

        BufferedImage ret = null;

        try{
            byte[] b = Base64.getDecoder().decode(data);
            InputStream inputStream = new ByteArrayInputStream(b);
            ret = ImageIO.read(inputStream);

            inputStream.close();

        }catch (Exception e){
            System.out.println("Exception in stringToImage " + e);
        }

        return ret;
    }

}

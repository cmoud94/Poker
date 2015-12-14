/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package poker.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class Utils {

    public static byte[] getObjectAsBytes(Object object) {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;

        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);

            oos.reset();
            oos.writeObject(object);
            oos.flush();

            byte[] bytes = baos.toByteArray();

            oos.close();
            baos.close();

            return (bytes != null) ? bytes : null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Object getBytesAsObject(byte[] bytes) {
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;

        try {
            bais = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bais);

            Object object = ois.readObject();

            ois.close();
            bais.close();

            return (object != null) ? object : null;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    /*public static byte[] concatByteArrays(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }*/

    public static BufferedImage loadImage(Object parent, String pathToImage) {
        BufferedImage bufferedImage = null;
        ImageIcon imageIcon = null;

        try {
            InputStream inputStream = parent.getClass().getResourceAsStream(pathToImage);
            bufferedImage = ImageIO.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bufferedImage;
    }

    public static BufferedImage getSubImage(BufferedImage bufferedImage, int x, int y, int width, int height) {
        return bufferedImage.getSubimage(x, y, width, height);
    }

    public static ImageIcon getScaledImageAsImageIcon(BufferedImage bufferedImage, int width, int height) {
        return new ImageIcon(bufferedImage.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH));
    }

}

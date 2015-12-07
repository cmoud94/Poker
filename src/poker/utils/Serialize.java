package poker.utils;

import java.io.*;

/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

public class Serialize {

    public static byte[] getObjectAsBytes(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;

        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.flush();
            oos.close();
            baos.close();

            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    public static Object getBytesAsObject(byte[] bytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = null;

        try {
            ois = new ObjectInputStream(bais);
            ois.close();
            bais.close();

            return ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

}

package poker.client.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

public class Panel extends JPanel {

    private final int posX;

    private final int posY;

    private final int width;

    private final int height;

    private final JPanel jPanel;

    private final Insets insets;

    public Panel(int x, int y, int width, int height) {
        this.posX = x;
        this.posY = y;
        this.width = width;
        this.height = height;
        this.jPanel = new JPanel(null);
        this.jPanel.setBounds(x, y, width, height);
        this.insets = jPanel.getInsets();
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public JPanel getjPanel() {
        return jPanel;
    }

    public Insets getInsets() {
        return insets;
    }
}

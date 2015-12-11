/**
 * Copyright (C) 2015 Marek Kouřil <marek.kouril.st@vsb.cz>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 */

package poker.server;

public class RunServer {

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");

        if (args.length != 4) {
            System.out.println("Usage: Server [port] [numberOfPlayers] [bigBlind] [startingMoney]");
            System.exit(0);
        } else {
            int port, numOfPlayers, bigBlind, startingMoney;

            port = Integer.parseInt(args[0]);
            numOfPlayers = Integer.parseInt(args[1]);
            bigBlind = Integer.parseInt(args[2]);
            startingMoney = Integer.parseInt(args[3]);

            Server server = new Server(port, numOfPlayers, bigBlind, startingMoney);
            Thread thread = new Thread(server, "serverLoop");

            server.startServer();

            thread.start();

            server.consoleLoop();
        }
    }

}

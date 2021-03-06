/**
 * This file is part of Clither.
 *
 * Clither is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Clither is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Clither.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.clitherproject.clither.server.net;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import org.clitherproject.clither.server.net.packet.Packet;
import org.clitherproject.clither.server.net.packet.inbound.PacketInInitial;
import org.clitherproject.clither.server.net.packet.inbound.PacketInUpdateSnake;
import org.clitherproject.clither.server.net.throwable.UnhandledPacketException;
import org.clitherproject.clither.server.world.PlayerImpl;

@SuppressWarnings("unused")
public class PlayerConnection {

    static Logger log = Logger.getGlobal();
    private final Channel channel;
    private final Map<Integer, MousePosition> cellMousePositions = new HashMap<>();
    private boolean individualMovementEnabled = false;
    private MousePosition globalMousePosition;
    private ConnectionState state = ConnectionState.AUTHENTICATE;

    public PlayerConnection(PlayerImpl player, Channel channel) {
        this.channel = channel;
    }

    public SocketAddress getRemoteAddress() {
        return channel.remoteAddress();
    }

    public void sendPacket(Packet packet) {
        channel.writeAndFlush(packet);
    }

    public void handle(Packet packet) {
        if (packet instanceof PacketInInitial) {
            handle((PacketInInitial) packet);
        } else if (packet instanceof PacketInUpdateSnake) {
            handle((PacketInUpdateSnake) packet);
        } else {
            throw new UnhandledPacketException("Unhandled packet: " + packet);
        }
    }

    public void handle(PacketInInitial packet) {
    	checkConnected();
    	
    }
    
    public void handle(PacketInUpdateSnake packet) {
    	checkConnected();
    }

    public boolean isIndividualMovementEnabled() {
        return individualMovementEnabled;
    }

    public MousePosition getGlobalMousePosition() {
        return globalMousePosition;
    }

    public MousePosition getCellMousePosition(int id) {
        return cellMousePositions.get(id);
    }

    private void checkConnected() {
        Preconditions.checkState(state == ConnectionState.CONNECTED, "Connection is not in CONNECTED state!");
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.channel);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlayerConnection other = (PlayerConnection) obj;
        if (!Objects.equals(this.channel, other.channel)) {
            return false;
        }
        return true;
    }

    private enum ConnectionState {

        AUTHENTICATE, RESET, CONNECTED;
    }

    public static class MousePosition {

        private final double x;
        private final double y;

        public MousePosition(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }
}

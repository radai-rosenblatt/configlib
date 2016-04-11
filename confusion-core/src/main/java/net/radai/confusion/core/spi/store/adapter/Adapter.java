/*
 * This file is part of Confusion.
 *
 * Confusion is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Confusion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Confusion.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.radai.confusion.core.spi.store.adapter;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Created by Radai Rosenblatt
 */
public interface Adapter {

    Adapter BASE64 = new Adapter() {
        @Override
        public String toText(byte[] binary) {
            if (binary == null) {
                return  null;
            }
            return Base64.getEncoder().encodeToString(binary);
        }

        @Override
        public byte[] toBinary(String text) {
            if (text == null) {
                return null;
            }
            return Base64.getDecoder().decode(text);
        }

        @Override
        public String toString() {
            return "base64";
        }
    };

    Adapter UTF8 = new Adapter() {
        Charset charset = Charset.forName("UTF-8");

        @Override
        public String toText(byte[] binary) {
            if (binary == null) {
                return  null;
            }
            return new String(binary, charset);
        }

        @Override
        public byte[] toBinary(String text) {
            if (text == null) {
                return null;
            }
            return text.getBytes(charset);
        }

        @Override
        public String toString() {
            return "utf8";
        }
    };

    String toText(byte[] binary);
    byte[] toBinary(String text);
}

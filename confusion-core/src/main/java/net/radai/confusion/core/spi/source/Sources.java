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

package net.radai.confusion.core.spi.source;

import net.radai.confusion.core.spi.PayloadType;
import net.radai.confusion.core.spi.codec.BinaryCodec;
import net.radai.confusion.core.spi.codec.Codec;
import net.radai.confusion.core.spi.codec.TextCodec;
import net.radai.confusion.core.spi.store.BinaryStore;
import net.radai.confusion.core.spi.store.Store;
import net.radai.confusion.core.spi.store.TextStore;
import net.radai.confusion.core.spi.store.adapter.Adapter;
import net.radai.confusion.core.spi.store.adapter.BinaryToTextStore;
import net.radai.confusion.core.spi.store.adapter.TextToBinaryStore;

/**
 * Created by Radai Rosenblatt
 */
public class Sources {

    public static <T> Source<T> from(Class<T> beanClass, Store store, Codec codec) {
        if (beanClass == null || store == null || codec == null) {
            throw new IllegalArgumentException();
        }
        PayloadType storeType = store.getPayloadType();
        PayloadType codecType = codec.getPayloadType();
        if (storeType == codecType) {
            switch (codecType) {
                case BINARY:
                    return new BinarySource<>(beanClass, (BinaryStore) store, (BinaryCodec) codec);
                case TEXT:
                    return new TextSource<>(beanClass, (TextStore) store, (TextCodec) codec);
                default:
                    throw new IllegalStateException("unhandled " + storeType);
            }
        }
        //adapters to the rescue
        switch (codecType) {
            case BINARY:
                //binary codec over a text store - use base64 to put binary into a text store
                return new BinarySource<>(
                        beanClass,
                        new TextToBinaryStore((TextStore) store, Adapter.BASE64),
                        (BinaryCodec) codec
                );
            case TEXT:
                //text codec over a binary store - encode text as utf8
                return new TextSource<>(
                        beanClass,
                        new BinaryToTextStore((BinaryStore) store, Adapter.UTF8),
                        (TextCodec) codec
                );
            default:
                throw new IllegalStateException("unhandled " + storeType);
        }
    }
}

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

package net.radai.confusion.core.jaxb;

import net.radai.confusion.core.spi.codec.TextCodec;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by Radai Rosenblatt
 */
public class JaxbCodec implements TextCodec {
    @Override
    public <T> T parse(Class<T> beanClass, String from) {
        if (from == null) {
            return null;
        }
        try {
            JAXBContext context = JAXBContext.newInstance(beanClass);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Object unmarshalled = unmarshaller.unmarshal(new StringReader(from));
            //noinspection unchecked
            return (T) unmarshalled;
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public <T> String serialize(T beanInstance) {
        if (beanInstance == null) {
            return null;
        }
        try {
            JAXBContext context = JAXBContext.newInstance(beanInstance.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter writer = new StringWriter();
            marshaller.marshal(beanInstance, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

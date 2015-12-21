/*
 * This file is part of ConfigLib.
 *
 * ConfigLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ConfigLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ConfigLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.radai.beanz;

import net.radai.beanz.api.Bean;
import net.radai.beanz.api.Codec;
import net.radai.beanz.api.Property;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Radai Rosenblatt
 */
public class BeanzTest {

    @Test
    public void testSimpleBeanParsing() throws Exception {
        Bean bean = Beanz.analyze(SimpleBean.class);

        UUID uuid = UUID.randomUUID();
        SimpleBean instance = new SimpleBean();
        Assert.assertNull(instance.f3);
        Property f3 = bean.getProperty("f3");
        f3.set(instance, uuid);
        Assert.assertTrue(instance.f3 == uuid);
        String uuidString = uuid.toString();
        Codec codec = f3.getCodec();
        UUID decoded = (UUID) codec.decode(uuidString);
        Assert.assertTrue(decoded != uuid);
        Assert.assertEquals(decoded, uuid);

        Property f7 = bean.getProperty("f7");
        Assert.assertNull(f7.getCodec()); //unparsable
    }

    public static class SimpleBean {
        private int f1;
        private String f2;
        private UUID f3;
        private long[] f4;
        private List<Integer> f5;
        private Map<UUID, Boolean> f6;
        private ParsableBean f7;
        private Set<UnparsableBean> f8;

        public String getF2() {
            return f2;
        }

        public void setF3(UUID f3) {
            this.f3 = f3;
        }

        public long[] getF4() {
            return f4;
        }

        public void setF4(long[] f4) {
            this.f4 = f4;
        }

        public List<Integer> getF5() {
            return f5;
        }

        public void setF5(List<Integer> f5) {
            this.f5 = f5;
        }

        public Map<UUID, Boolean> getF6() {
            return f6;
        }

        public void setF6(Map<UUID, Boolean> f6) {
            this.f6 = f6;
        }

        public ParsableBean getF7() {
            return f7;
        }

        public void setF7(ParsableBean f7) {
            this.f7 = f7;
        }

        public Set<UnparsableBean> getF8() {
            return f8;
        }

        public void setF8(Set<UnparsableBean> f8) {
            this.f8 = f8;
        }
    }

    public static class ParsableBean {
        private int f1;

        public ParsableBean(int f1) {
            this.f1 = f1;
        }

        public int getF1() {
            return f1;
        }

        public void setF1(int f1) {
            this.f1 = f1;
        }

        @Override
        public String toString() {
            return String.valueOf(f1);
        }

        public ParsableBean valueOf(String str) {
            return new ParsableBean(Integer.parseInt(str));
        }
    }

    public static class UnparsableBean {
        private String a;
        private int b;

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }
    }
}

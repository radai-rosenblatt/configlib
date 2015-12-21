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

package net.radai.configlib.core.runtime;

import net.radai.beanz.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Created by Radai Rosenblatt
 */
public class BeanClassAnalyzerTest {

//    @Test
//    public void testPropertyResolution() throws Exception {
//        Property prop0 = Beanz.resolve(TestClass1.class, "prop0");
//        Assert.assertTrue(prop0 instanceof MethodProperty);
//        Assert.assertTrue(prop0.getValueType().equals(String.class));
//        Assert.assertTrue(prop0.isReadable());
//        Assert.assertTrue(prop0.isWritable());
//        Property prop1 = Beanz.resolve(TestClass1.class, "prop1");
//        Assert.assertTrue(prop1 instanceof MethodProperty);
//        Assert.assertTrue(prop1.isReadable());
//        Assert.assertTrue(prop1.isWritable());
//        Property prop2 = Beanz.resolve(TestClass1.class, "prop2");
//        Assert.assertTrue(prop2 instanceof CompositeProperty);
//        Assert.assertTrue(prop2.isReadable());
//        Assert.assertTrue(prop2.isWritable());
//        Property prop3 = Beanz.resolve(TestClass1.class, "prop3");
//        Assert.assertTrue(prop3 instanceof CompositeProperty);
//        Assert.assertTrue(prop3.isReadable());
//        Assert.assertTrue(prop3.isWritable());
//        Property prop4 = Beanz.resolve(TestClass1.class, "prop4");
//        Assert.assertTrue(prop4 instanceof FieldProperty);
//        Assert.assertTrue(prop4.isReadable());
//        Assert.assertTrue(prop4.isWritable());
//        Property prop5 = Beanz.resolve(TestClass1.class, "prop5");
//        Assert.assertNull(prop5);
//        Property prop6 = Beanz.resolve(TestClass1.class, "prop6");
//        Assert.assertTrue(prop6 instanceof MethodProperty);
//        Assert.assertTrue(prop6.isReadable());
//        Assert.assertFalse(prop6.isWritable());
//        Property prop7 = Beanz.resolve(TestClass1.class, "prop7");
//        Assert.assertTrue(prop7 instanceof MethodProperty);
//        Assert.assertFalse(prop7.isReadable());
//        Assert.assertTrue(prop7.isWritable());
//    }
//
//    @Test
//    public void testBooleanPropertyResolution() throws Exception {
//        Property b1 = Beanz.resolve(TestClass2.class, "b1");
//        Assert.assertNotNull(b1);
//        Assert.assertTrue(b1.isReadable());
//        Assert.assertTrue(b1.isWritable());
//        Property b2 = Beanz.resolve(TestClass2.class, "b2");
//        Assert.assertNotNull(b2);
//        Assert.assertTrue(b2.isReadable());
//        Assert.assertTrue(b2.isWritable());
//        Property b3 = Beanz.resolve(TestClass2.class, "b3");
//        Assert.assertNotNull(b3);
//        Assert.assertTrue(b3.isReadable());
//        Assert.assertTrue(b3.isWritable());
//        Property b4 = Beanz.resolve(TestClass2.class, "b4");
//        Assert.assertNotNull(b4);
//        Assert.assertTrue(b4.isReadable());
//        Assert.assertTrue(b4.isWritable());
//    }
//
//    @Test
//    public void testAnalyze() throws Exception {
//        Bean desc = Beanz.analyze(TestClass3.class);
//        Assert.assertEquals(5, desc.getProperties().size());
//        Assert.assertEquals(1, desc.getSections().size());
//    }

    private static class TestClass1 {
        private String prop1;
        private String prop2;
        private String prop3;
        private String prop4;

        public String getProp0() {
            return null;
        }

        public void setProp0(String prop1) {
            //nop
        }

        public String getProp1() {
            return prop1;
        }

        public void setProp1(String prop1) {
            this.prop1 = prop1;
        }

        public String getProp2() {
            return prop2;
        }

        public void setProp3(String prop3) {
            this.prop3 = prop3;
        }

        public String getProp6() {
            return null;
        }

        public void setProp7(String prop7) {

        }
    }

    private static class TestClass2 {
        private boolean b1;
        private boolean b2;
        private Boolean b3;
        private Boolean b4;

        public boolean isB1() {
            return b1;
        }

        public void setB1(boolean b1) {
            this.b1 = b1;
        }

        public boolean getB2() {
            return b2;
        }

        public void setB2(boolean b2) {
            this.b2 = b2;
        }

        public Boolean isB3() {
            return b3;
        }

        public void setB3(Boolean b3) {
            this.b3 = b3;
        }

        public Boolean getB4() {
            return b4;
        }

        public void setB4(Boolean b4) {
            this.b4 = b4;
        }
    }

    private static class TestClass3 <T extends String> {
        private List<String> f1;
        private String[] f2;
        private Map<Integer, String> f3;
        private String f4;
        private T[] f5;
        private List f6;

        public List<String> getF1() {
            return f1;
        }

        public void setF1(List<String> f1) {
            this.f1 = f1;
        }

        public String[] getF2() {
            return f2;
        }

        public void setF2(String[] f2) {
            this.f2 = f2;
        }

        public Map<Integer, String> getF3() {
            return f3;
        }

        public void setF3(Map<Integer, String> f3) {
            this.f3 = f3;
        }

        public String getF4() {
            return f4;
        }

        public void setF4(String f4) {
            this.f4 = f4;
        }

        public T[] getF5() {
            return f5;
        }

        public void setF5(T[] f5) {
            this.f5 = f5;
        }

        public List getF6() {
            return f6;
        }

        public void setF6(List f6) {
            this.f6 = f6;
        }
    }
}

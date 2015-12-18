package net.radai.configlib.core.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * @author Radai Rosenblatt
 */
public class FormatUtil {
    public static String prettyPrint(Type type) {
        if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            return prettyPrint(genericArrayType.getGenericComponentType()) + "[]";
        }
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            throw new UnsupportedOperationException(); //TODO - finish this
        }
        if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) type;
            return typeVariable.getName();
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            StringBuilder sb = new StringBuilder();
            sb.append(prettyPrint(parameterizedType.getRawType()));
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments != null && typeArguments.length > 0) {
                sb.append("<");
                for (Type typeArgument : typeArguments) {
                    sb.append(prettyPrint(typeArgument)).append(", ");
                }
                sb.delete(sb.length()-2, sb.length()); // last ", "
                sb.append(">");
            }
            return sb.toString();
        }
        Class clazz = (Class) type;
        if (clazz.isArray()) {
            return prettyPrint(clazz.getComponentType()) + "[]";
        }
        return clazz.getSimpleName();
    }
}

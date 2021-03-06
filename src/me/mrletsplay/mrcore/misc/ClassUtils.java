package me.mrletsplay.mrcore.misc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassUtils {

	private static final List<Class<?>> PRIMITIVE_TYPE_CLASSES = Collections.unmodifiableList(Arrays.asList(
				void.class, byte.class, short.class, int.class, long.class, float.class, double.class, char.class, boolean.class
			));
	
	public static boolean isPrimitiveTypeClass(Class<?> clazz) {
		return PRIMITIVE_TYPE_CLASSES.contains(clazz);
	}
	
	public static Class<?> getArrayBaseClass(Class<?> arrayClass) {
		Class<?> cl = arrayClass;
		while(cl.isArray()) cl = cl.getComponentType();
		return cl;
	}
	
	public static int getArrayDimensions(Class<?> arrayClass) {
		int d = 0;
		while(arrayClass.isArray()) d++;
		return d;
	}
	
	public static Set<Field> getFields(Class<?> clz) {
		Set<Field> fs = new HashSet<>();
		Class<?> cls = clz;
		while(!cls.equals(Object.class)) {
			fs.addAll(Arrays.asList(cls.getDeclaredFields()));
			cls = cls.getSuperclass();
			if(cls == null) break;
		}
		return fs;
	}
	
	public static Set<Method> getMethods(Class<?> clz) {
		Set<Method> fs = new HashSet<>();
		Class<?> cls = clz;
		while(!cls.equals(Object.class)) {
			fs.addAll(Arrays.asList(cls.getDeclaredMethods()));
			cls = cls.getSuperclass();
			if(cls == null) break;
		}
		return fs;
	}
	
	public static Method getDeclaredMethodRecursively(Class<?> clz, String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
		Class<?> cls = clz;
		while(true) {
			try {
				return cls.getDeclaredMethod(name, parameterTypes);
			}catch(NoSuchMethodException ignored) {}
			if(cls.equals(Object.class)) break;
			cls = cls.getSuperclass();
			if(cls == null) break;
		}
		throw new NoSuchMethodException("Couldn't find method \"" + name + "\" in class " + clz.getName() + " or any of its superclasses");
	}
	
}

package tech.seedhk.utils;

import java.lang.reflect.Method;

public class ReflectTest {
	
	
	public static void main(String[] args) throws Exception {
		
		Class clazz=Class.forName("tech.seedhk.bean.ByteBuffer");
		Method[] methods=clazz.getDeclaredMethods();
		for(Method m:methods){
			System.out.println("方法名："+m.getName());
			System.out.println("修饰符"+m.getModifiers()+",返回值"+m.getReturnType().getName());
			Class[] param=m.getParameterTypes();
			for(Class c:param){
				System.out.print("参数："+c.getName()+"  ");
				System.out.println();
			}
		}
	}

}

package tech.seedhk.utils;

import java.lang.reflect.Field;
import sun.misc.Unsafe; 

public class UnSafeUtil {
	
	private static Unsafe unsafe;

	@SuppressWarnings("restriction")
	public static void getUnsafe() {
		if(unsafe==null) {
			try {
				Field field = Unsafe.class.getDeclaredField("theUnsafe");
				field.setAccessible(true);  
	            unsafe = (Unsafe) field.get(null);  
			} catch (NoSuchFieldException | SecurityException 
					| IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Long allocateMemory(Object obj) {
		
		Object[] objs=new Object[] {obj};
		
		int offset=unsafe.arrayBaseOffset(Object[].class);
		int addressSize = unsafe.addressSize(); 
		System.out.println(offset);
		System.out.println(addressSize);
		
		return null;
	}
	
	@SuppressWarnings({ "restriction", "deprecation" })
	public static void main(String[] args) throws Exception {
		getUnsafe();
		long address=unsafe.allocateMemory(4);
		System.out.println(address);
		unsafe.putInt(address, (byte)101);
		int b=unsafe.getInt(address);
		System.out.println(b);
		
		int b1=(int) unsafe.getObject(address, 4);
		System.out.println(b1);
	}
	
	

}

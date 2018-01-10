package tech.seedhk.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProxyObject implements Serializable{
	
	private static ProxyObject object;
	
		
	private static final long serialVersionUID = 1L;
	public  Class<?> className;	//类名
	//private  Class<?> returnType;	//返回类型
	public  String methodName;	//方法名
	public  Class<?>[] classs;		//参数列表
	public Object[] objects;		//参数值
	
	private ProxyObject(){
		
	}
	
	
	public static ProxyObject newInstance(){
		if(null==object)
			object=new ProxyObject();
		return object;
	}
	
	/**
	 *sender端调用该方法执行某个方法
	 * @param classname
	 * @param methodname
	 * @param objects
	 * @param clazz
	 * @return
	 */
	public static Object invoke(Class<?> classname,String methodname,Object[] objects,Class<?>... clazz){
		/*
		 * 思路：
		 * 暂不支持数组作为参数
		 * clazz类型必须是装箱类型
		 */
		
		Class<?> c;
		Method m;
		Object ret = null;
		Class<?>[] paramTypes;
		try {
			c=classname;
			m=c.getMethod(methodname, clazz);
			paramTypes=m.getParameterTypes();
/*			for(Class<?> s:paramTypes){
				String type=s.getName();
				System.out.println(type);
			}*/
			Object object=c.newInstance();
			ret=m.invoke(object, objects);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} 
		
		return ret;
	}
	
	/**
	 * geter端调用该方法传递某个方法
	 * @param classname
	 * @param methodname
	 * @param objects
	 * @param clazz
	 */
	public void getMethod(String classname,String methodname,Object[] objects,Class<?>... clazz) {

		Class<?> c;
		Method method;
		//Class<?> retType;
		try {
			c = Class.forName(classname);
			method=c.getMethod(methodname, clazz);
			//retType=method.getReturnType();
			className=c;
			//returnType=retType;
			methodName=method.getName();
			classs=clazz;
			this.objects=objects;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
	
	public static byte[] bean2byte(Object obj){
		
		byte[] b=null;
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			ObjectOutputStream oos=new ObjectOutputStream(baos);
			oos.writeObject(obj);
			b = baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return b;
	}
	
	/**
	 * 将一个对象转换成ByteBuffer，无需flip
	 * @param obj
	 * @return
	 */
	public static ByteBuffer bean2Buffer(Object obj){
		
		byte[] b=null;
		String[] strs=null;
		ByteBuffer buffer=null;
		if(obj instanceof String[]){
			strs=(String[])obj;
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<strs.length;i++){
			sb.append(strs[i]);	
			}
			buffer=ByteBuffer.wrap(sb.toString().getBytes());
		}else{
			try {
				ByteArrayOutputStream baos=new ByteArrayOutputStream();
				ObjectOutputStream oos=new ObjectOutputStream(baos);
				oos.writeObject(obj);
				b = baos.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
			}
			buffer=ByteBuffer.wrap(b);
		}
		return buffer;
	}
	
	public static Object byte2Bean(byte[] b){
		Object o=null;
		try {
			ByteArrayInputStream bais=new ByteArrayInputStream(b);
			ObjectInputStream ios=new ObjectInputStream(bais);
			o=ios.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return o;
	}
	
	public static Object buffer2Bean(ByteBuffer buffer){
		
		Object o=null;
		
		if(buffer.hasArray()){
			byte[] buf=buffer.array();
			try {
				ByteArrayInputStream bais=new ByteArrayInputStream(buf);
				ObjectInputStream ios=new ObjectInputStream(bais);
				o=ios.readObject();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return o;
		
	}
		


	@Override
	public String toString() {
		return "ProxyObject : className="+className.getName()+" , methodName="+
				methodName+" , classs="+listParam(classs);
	}
	
	private String listParam(Class<?>[] classs){
		StringBuilder sb=new StringBuilder();
		if(null!=classs && classs.length>0)
			for(Class<?> s:classs){
				sb.append(s.getName()).append(" ") ;
			}
		else
			sb.append("null");
		sb.append("\n");
		return sb.toString();
	}


	public static void main(String[] args) throws Exception {

		ProxyObject po1=new ProxyObject();
		po1.className=Object.class;
		po1.methodName="test";
		po1.classs=new Class[]{String.class};
		po1.objects=new Object[]{4,5,6};
		
		byte[] b=bean2byte(po1);
		
		ProxyObject po=(ProxyObject) byte2Bean(b);
		
		System.out.println(po.toString());
		
	}
	
	
	public List<Map<String,Object>> test(String param1,Integer param2,char param3){
		int[] j=new int[]{Integer.parseInt(param1),param2,(int)param3};
		List<Map<String,Object>> list=new ArrayList<>();
		for(int i=0;i<3;i++){
			Map<String,Object> map=new HashMap<>();
			map.put(""+i, j[i]);
			list.add(map);
		}
		return list;
	}


	public Class<?> getClassName() {
		return className;
	}


	public void setClassName(Class<?> className) {
		this.className = className;
	}


	public String getMethodName() {
		return methodName;
	}


	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}


	public Class<?>[] getClasss() {
		return classs;
	}


	public void setClasss(Class<?>[] classs) {
		this.classs = classs;
	}


	public Object[] getObjects() {
		return objects;
	}


	public void setObjects(Object[] objects) {
		this.objects = objects;
	}
	
	
	
	
	
	
	

}

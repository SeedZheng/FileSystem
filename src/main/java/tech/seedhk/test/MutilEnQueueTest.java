package tech.seedhk.test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

public class MutilEnQueueTest {
	
	public static void main(String[] args) throws Exception {
		Integer a1=500;
		Integer a2=501;
		CAS(500,a1,a2);
		System.out.println(a1);
	}
	


	
	/**
	 * CAS算法
	 * @param expV 期望值
	 * @param oldV 老值
	 * @param newV 新值
	 */
	public static boolean CAS(Object expV,Object oldV,Object newV) {
		if(expV.equals(oldV)) {
			oldV=newV;
			return true;
		}
		return false;
	}

}

class MyQueue{
	
	Object value;// 值
	MyQueue next;//下一个节点
	MyQueue tail;//尾节点
}

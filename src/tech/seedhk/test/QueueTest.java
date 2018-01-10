package tech.seedhk.test;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueTest implements Runnable{
	
	Queue<String> queue=new ConcurrentLinkedQueue<>();
	
	public static void main(String[] args) {
		
		QueueTest qt=new QueueTest();
		QueueTest qt1=new QueueTest();
		QueueTest qt2=new QueueTest();
		Thread t1=new Thread(qt);
		Thread t2=new Thread(qt1);
		Thread t3=new Thread(qt2);
		System.out.println(t1.toString());
		System.out.println(t2.toString());
		System.out.println(t3.toString());
		t1.start();
		t2.start();
		t3.start();
		
	}

	@Override
	public void run() {
		//先尝试取出数据
		String str=queue.poll();
		System.out.println(str);
		//再往里放数据
		queue.add(Thread.currentThread().getName());
		
	}

}


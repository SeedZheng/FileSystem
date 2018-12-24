package tech.seedhk.test;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 模拟线程池的方式访问一个ConcurrentLinkedQueue，测试不同线程访问的是否是同一个队列
 * @author Seed
 * 2017年12月8日 上午10:55:15
 */
public class QueueTest2  implements Runnable{
	
	Executor boss;
	Executor worker;
	
	Queue<String> queue =new ConcurrentLinkedQueue<>();
	/**
	 * 初始化线程
	 * @param boss
	 * @param worker
	 */
	public QueueTest2() {
		initBoss(Executors.newCachedThreadPool());
		initWorker( Executors.newCachedThreadPool());
	}
	
	private void initWorker(Executor worker2) {
		this.worker = worker2;
		System.out.println(this.toString());
		this.worker.execute(this);
	}

	private void initBoss(Executor boss2) {
		this.boss = boss2;
		System.out.println(this.toString());
		this.boss.execute(this);
	}

	public void setName(String threadName){
		Thread.currentThread().setName(threadName);
	}
	
	
	@Override
	public void run() {
		
		String str=queue.poll();
		System.out.println(Thread.currentThread().getName()+":"+str);
		queue.add(Thread.currentThread().getName());
		
	}
	
	public static void main(String[] args) {
		QueueTest2 qt2=new QueueTest2();
		
	}
	
	
	

}

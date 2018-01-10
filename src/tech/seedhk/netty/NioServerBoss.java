package tech.seedhk.netty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import tech.seedhk.bean.ProxyObject;
import tech.seedhk.buffer.BodyBuffer;
import tech.seedhk.buffer.BodyProcess;
import tech.seedhk.buffer.DataBuffer;
import tech.seedhk.utils.Log;

public class NioServerBoss implements Runnable{
	
	
	public static void main(String[] args) {
		//new NioServerBoss(Executors.newCachedThreadPool(),"boss",selector,8888);
	}
	
	private Executor boss;
	private Executor worker=Executors.newFixedThreadPool(2);
	private static Logger log=Log.getInstance(NioServerBoss.class);
	
	private String threadName;
	private static Selector selector;
	private int port;

	public NioServerBoss(Executor boss,String threadName,Selector selector,int port) {
		this.boss = boss;
		this.threadName=threadName;
		this.selector=selector;
		this.port=port;
		initBoss();
	}

	private void initBoss() {
		Thread.currentThread().setName(threadName+" "+Thread.currentThread().getId());
		try {
			this.selector=Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//获得一个ServerSocket通道
		ServerSocketChannel ssc;
		try {
			ssc = ServerSocketChannel.open();
			//设置成为非阻塞
			ssc.configureBlocking(false);
			
			//将该通道的绑定ServerSocket到某个端口上(类似之前的ServerSocket监听某个端口)
			ssc.socket().bind(new InetSocketAddress(port));
			
			ssc.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		boss.execute(this);
		log.info(Thread.currentThread().getName()+"启动成功");
		
	}

	@Override
	public void run() {
		
		while(true){
			try {
				int n=selector.select();
				
				if(n<=0)
					continue;
				//接收请求，并启动一个worker线程来接手该连接
				
				Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
				while(iterator.hasNext()){
					SelectionKey key=iterator.next();
					iterator.remove();
					ServerSocketChannel server = (ServerSocketChannel) key.channel();
		    		// 新客户端
		    		SocketChannel channel = server.accept();
		    		// 设置为非阻塞
		    		channel.configureBlocking(false);
		    		// 获取一个worker
		    		worker.execute(new Worker(channel));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	class Worker implements Runnable{
		
		private Selector selector;
		private SocketChannel channel;
		
		
		public Worker(SocketChannel c) throws Exception{
			Thread.currentThread().setName("worker "+Thread.currentThread().getId());
			this.selector = Selector.open();	//重新打开一个selector，让worker线程和boss线程各司其职
			this.channel=c;
			channel.register(selector, SelectionKey.OP_READ);
			log.info(Thread.currentThread().getName()+"启动成功，等待client端连接");
		}

		@Override
		public void run() {
			boolean isStop=false;
			
			try {
				while(!isStop){
					int n=this.selector.select();
					if(n<1)
						continue;//退出此次循环
					
					log.info(Thread.currentThread().getName()+"接收到一个client端请求");
					
					Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
					
					while(iterator.hasNext()){
						SelectionKey key=iterator.next();
						iterator.remove();
						
						// 得到事件发生的Socket通道
						SocketChannel channel = (SocketChannel) key.channel();
						
						// 数据总长度
						//long ret = 0;
						//boolean failure = true;
						//ByteBuffer head=ByteBuffer.allocate(26);
			        	//ByteBuffer body=ByteBuffer.allocate(1024);
			        	//ByteBuffer[] buffers=new ByteBuffer[]{head,body};
						DataBuffer data_buf=new DataBuffer();
						long body_size=data_buf.getHead(channel);
						if(body_size<0){
							key.cancel();
							log.info("客户端断开连接");
							isStop=true;
							break;	//该线程退出
						}
						
						//读取数据
/*						try {
							ret = channel.read(buffers);
							failure = false;
						} catch (Exception e) {
							// ignore
						}
						//判断是否连接已断开
						if (ret <= 0 || failure) {
						if (ret <= 0) {
							key.cancel();
							log.info("客户端断开连接");
							isStop=true;
							break;	//该线程退出
				        }*/
						else{
				        	BodyBuffer body_buf=data_buf.getBody((int)body_size, channel);
				        	BodyProcess.processServer(body_buf, channel);
				        	/*
				        	 * 数据发送接收协议：
				        	 * buffer1：数据类型(大小为4个字节)
				        	 * 		rpc:RPC执行方法
				        	 * 		text:文本消息
				        	 * 		file:文件
				        	 * buffer2:数据内容
				        	 */
				        	/*String type=new String(getHead(head).array()).trim();
				        	log.info(type);
				        	
				        	ByteBuffer ret1 = null;
				        	
				        	if(type.equals("text")){
				        		String retStr=new String(body.array()).trim();
					        	 log.info("收到数据:" + retStr);
					        	 head.flip();
					        	 body=ByteBuffer.wrap(("收到数据:" + retStr).getBytes());
				        	}
				        	
				        	if(type.equals("getFile")){
				        		String filePath=new String(body.array()).trim();
				        		String suffix=filePath.substring(filePath.lastIndexOf("."));
				        		ret1=getFile(filePath);
				        		//head=ByteBuffer.wrap(("file:"+suffix).getBytes());//WRAP方法后无需flip
				        		head=prepareHead("file:"+suffix);
				        		body.clear();
				        		body=ret1;
				        		//body.flip();
				        	}
				        	if(type.equals("rpc")){
				        		ret1=rpc(body);
				        		body.clear();
				        		head.flip();
				        		head=prepareHead(head);
				        		body=ret1;
				        		//body.flip();
				        	}
				        	
				        	 buffers=new ByteBuffer[]{head,body};
				     		//回写数据
				     		//ByteBuffer outBuffer = ByteBuffer.wrap(("收到"+retStr).getBytes());
				     		channel.write(buffers);// 将消息回送给客户端
*/				        }
					}
					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			log.info(Thread.currentThread().getName()+"，退出");
			
		}
		
	}
	
	private ByteBuffer rpc(ByteBuffer buffer) {
		//RPC调用
		ProxyObject po=(ProxyObject) ProxyObject.buffer2Bean(buffer);
		Object object=null;
		if(po!=null){
			object=ProxyObject.invoke(po.getClassName(), po.getMethodName(), po.getObjects(), String.class);
		}
		buffer.clear();
		buffer=ProxyObject.bean2Buffer(object);
		return buffer;
	}
	
	private ByteBuffer getFile(String filePath) throws Exception{
		
		File file=new File(filePath);
		ByteBuffer ret=null;
		if(!file.exists()){
			ret=ByteBuffer.wrap("该文件不存在".getBytes());
			return ret;
		}
		if(file.isDirectory()){
			ret=ByteBuffer.wrap("该文件是目录".getBytes());
			return ret;
		}

		RandomAccessFile raf=new RandomAccessFile(file, "r");
		
		FileChannel fc=raf.getChannel();
		 ret=ByteBuffer.allocate((int) raf.length());
		fc.read(ret);
		raf.close();
		fc.close();
		ret.flip();//转换读写模式
		return ret;
	}
	
	private ByteBuffer getHead(ByteBuffer buffer){
		
		byte[] b=buffer.array();
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<b.length-2;i++){
			sb.append((char)b[i]);
		}
		ByteBuffer head=ByteBuffer.wrap(sb.toString().trim().getBytes());
		return head;
	}
	
	
	private ByteBuffer prepareHead(String data){
		
		ByteBuffer head=ByteBuffer.allocate(26);//0-25
		
		byte[] b=data.trim().getBytes();
		head.put(b);
		if(head.position()>=24) System.err.println("缓冲区大小溢出");
		else{
			head.position(24);
			head.limit(head.capacity());
			head.put((byte)'$');
			head.put((byte)'&');
		}
		head.flip();
		return head;
	}
	
	private ByteBuffer prepareHead(ByteBuffer data){
		
		ByteBuffer head=ByteBuffer.allocate(26);//0-25
		
		while(data.hasRemaining())
			head.put(data.get());
		
		if(head.position()>=24) System.err.println("缓冲区大小溢出");
		else{
			head.position(24);
			head.limit(head.capacity());
			head.put((byte)'$');
			head.put((byte)'&');
		}
		head.flip();
		return head;
	}
	
	
}

package tech.seedhk.nio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.Selector;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.seedhk.bean.ByteBuffers;
import tech.seedhk.bean.Message;
import tech.seedhk.netty.NioServerBoss;


/**
 * 服务端，即被控端，基于NIO
 * @author Seed
 * 2017年12月6日 下午2:32:56
 */
public class Server {
	
	private Selector selector;
	private static Logger log=LoggerFactory.getLogger(Server.class);
	
	public static void main(String[] args) throws Exception {
		String host="127.0.0.1";
		int port=8888;
		//先连接中继器，注册成功后启动Server端
		Server server=new Server();
		if(args.length>0){
			host=args[0];
			port=Integer.parseInt(args[1]);
		}
		server.resister(host, port);
	}
	
	
	private  void resister(String host,int port) throws Exception{
		Socket s=new Socket(host, port);
		log.info("连接中继器成功");
		InputStream is=new DataInputStream(s.getInputStream());
		OutputStream os=new DataOutputStream(s.getOutputStream());

		//ByteBuffers buffer=new ByteBuffers();
		//buffer.write(os, "server");
		ByteBuffers.sendMsg(os, Message.REGCLIENT);
		Message messge=ByteBuffers.readMsg(is);

		if(Message.SUCCESS.equals(messge.getMessageType())){
			log.info("注册成功，启动sever");
			is.close();
			os.close();
			s.close();
			log.info(s.isClosed()+"");
			new NioServerBoss(Executors.newCachedThreadPool(), "boss", selector, port);
		}
	}
	

	
	
	//-------------------以下代码无法实现线程池+NIO-----------------
	
/*private static ExecutorService service=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);
	
	private void initServer(int port) throws Exception{
		ServerSocketChannel ssChannel=ServerSocketChannel.open();
		
		ssChannel.configureBlocking(false);
		
		//ssChannel.socket().setReuseAddress(true);//允许在前一个连接TIME_WAIT状态时绑定新的端口
		ssChannel.socket().bind(new InetSocketAddress(port));
		
		//获得一个通道管理器
		this.selector=Selector.open();
		
		ssChannel.register(selector, SelectionKey.OP_ACCEPT);
	}
	
	private void listen()throws Exception{
		log.info("服务器启动成功，进入监听状态");
		
		while(true){
			int n=selector.select();
			if(n==0)
				continue;
			Iterator iterator = selector.selectedKeys().iterator();
			while(iterator.hasNext()){
				SelectionKey key=(SelectionKey) iterator.next();
				iterator.remove();
				if(key.isAcceptable()){
					try {
						ServerSocketChannel ssc=(ServerSocketChannel) key.channel();
						SocketChannel sc=ssc.accept();
						sc.configureBlocking(false);
						
						sc.write(ByteBuffer.wrap("connection success".getBytes()));
						
						sc.register(selector, SelectionKey.OP_READ);//如果是已经注册过的事件，比如accept，再次注册会报错
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if(key.isReadable()){
					service.execute(new Process(key));
				}
			}
			
		}
	}
	
	class Process implements Runnable{

		private SelectionKey key;

		public Process(SelectionKey key) {
			this.key=key;
		}

		@Override
		public void run() {
			log.info(Thread.currentThread().getName()+" 开始执行任务");
			if(key.isAcceptable()){
				try {
					ServerSocketChannel ssc=(ServerSocketChannel) key.channel();
					SocketChannel sc=ssc.accept();
					sc.configureBlocking(false);
					
					sc.write(ByteBuffer.wrap("connection success".getBytes()));
					
					sc.register(selector, SelectionKey.OP_READ);//如果是已经注册过的事件，比如accept，再次注册会报错
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			//if(key.isReadable()){
				try {
					read(key);
				} catch (Exception e) {
					e.printStackTrace();
				}
			//}
			
		}

		private void read(SelectionKey key2) throws Exception {
			SocketChannel sc=(SocketChannel) key2.channel();
			
			ByteBuffer buffer=ByteBuffer.allocate(1024);
			
			sc.read(buffer);
			buffer.flip();
			
			//rpc(buffer);
			
			sc.write(buffer);
			log.info("命令调用完成");
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
		
	}*/

}

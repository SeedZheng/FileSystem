package tech.seedhk.nio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import tech.seedhk.bean.ByteBuffer;
import tech.seedhk.utils.Log;

public class Repeater {
	
	static Logger log=Log.getInstance(Repeater.class);
	
	private static ExecutorService threadPool=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private Map<String, String> map=new HashMap<>();
	
	public static void main(String[] args) throws Exception {
		int port=8888;
		if(args.length>0)
			port=Integer.parseInt(args[0]);
		Repeater r=new Repeater();
		r.initServer(port);
	}
	/*
	 *break的作用是跳出当前循环块（for、while、do while）或程序块（switch）。在循环块中的作用是跳出当前正在循环的循环体。
	 *在程序块中的作用是中断和下一个case条件的比较。
	 *continue用于结束循环体中其后语句的执行，并跳回循环程序块的开头执行下一次循环，而不是立刻循环体。
	 * 
	 */
	
	@SuppressWarnings("resource")
	private void initServer(int port)throws Exception{
		ServerSocket ss=new ServerSocket(port);
		log.info("repeater starting");
		while(true){
			Socket s = ss.accept();
			log.info("当前socket的hashcode："+s.hashCode());
			String ip=s.getInetAddress().getHostAddress();
			log.info("ip："+ip);
			if(map.get("server")!=null){
				if(ip.equals(map.get("server"))){
					threadPool.execute(new Sender(s));
					continue;
				}
			}
			if(map.get("client")!=null){
				if(ip.equals(map.get("client"))){
					threadPool.execute(new Geter(s));
					continue;
				}
			}
			threadPool.execute(new Register(s));
		}
	}
	
	/**
	 * server端(被控端) 、client端(控制端)的注册过程
	 * @author Seed
	 * 2017年12月6日 下午3:09:24
	 */
	class Register implements Runnable{
		
		private Socket socket;
		public Register(Socket s) {
			this.socket=s;
		}
		
		InputStream is;
		OutputStream os;

		@Override
		public void run() {
			try {
				is=new DataInputStream(socket.getInputStream());
				os=new DataOutputStream(socket.getOutputStream());
				
				byte[] data=ByteBuffer.read(is);
				String ip=socket.getInetAddress().getHostAddress();
				String type=new String(data,"utf-8");
				if("client".equals(type))
					map.put("client", ip);
				if("server".equals(type))
					map.put("server", ip);
				
				ByteBuffer buffer=new ByteBuffer();
				buffer.write(os, "success");
				
				os.close();
				is.close();
				socket.close();
				
				log.info( type+"注册完毕");
					
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	/**
	 * 控制端，从这里拿到被控端的IP
	 * @author Seed
	 * 2017年12月6日 下午2:26:21
	 */
	class Geter implements Runnable{
		
		private Socket socket;

		public Geter(Socket s){
			this.socket=s;
		}
		
		//InputStream is;
		OutputStream os;

		@Override
		public void run() {
			try {
				//is=new DataInputStream(socket.getInputStream());
				os=new DataOutputStream(socket.getOutputStream());
				ByteBuffer buffer=new ByteBuffer();
				
				if(map.get("server")==null)
					buffer.write(os, "no ip");
				else
					buffer.write(os, "getip:"+map.get("server"));
				
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				try {
					//延时3秒再关闭
					Thread.sleep(3000);
					os.close();
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
	
	
	/**
	 * 注册成为被控端
	 * @author Seed
	 * 2017年12月6日 下午2:23:18
	 */
	class Sender implements Runnable{
		
		private Socket socket;
		
		public Sender(Socket s){
			this.socket=s;
		}

		InputStream is;
		OutputStream os;
		
		@Override
		public void run() {
			//如果当前已经有被控端了，这里应该替换原来的
			
			try {
				is=new DataInputStream(socket.getInputStream());
				os=new DataOutputStream(socket.getOutputStream());
				
				byte[] data=ByteBuffer.read(is);
				String ip=socket.getInetAddress().getHostAddress();
				String type=new String(data,"utf-8");
				if("client".equals(type))
					map.put("client", ip);
				if("server".equals(type))
					map.put("server", ip);
				
				ByteBuffer buffer=new ByteBuffer();
				buffer.write(os, "success");
				
				os.close();
				is.close();
				//socket.close();
				
				log.info( type+"注册完毕");
					
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
}

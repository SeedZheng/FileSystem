package tech.seedhk.nio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.log4j.Logger;

import tech.seedhk.bean.ProxyObject;
import tech.seedhk.buffer.BodyBuffer;
import tech.seedhk.buffer.BodyProcess;
import tech.seedhk.buffer.DataBuffer;
import tech.seedhk.utils.Log;

/**
 * 控制端，基于NIO,单线程足矣
 * @author Seed
 * 2017年12月6日 下午5:09:09
 */
public class Client {
	
	private  Selector selector;
	private boolean isGet=false;
	private static Logger log=Log.getInstance(Client.class);
	
	public static void main(String[] args) throws Exception {
		String host="127.0.0.1";
		int port=8888;
		Client client=new Client();
		if(args.length>0){
			host=args[0];
			port=Integer.parseInt(args[1]);
		}
		client.register(host, port);
	}
	
	public void register(String host,int port) throws Exception{

		Socket s;
		
		while(!isGet){
			 s=new Socket(host,port);
			 log.info("连接中继器成功");
			InputStream is=new DataInputStream(s.getInputStream());
			OutputStream os=new DataOutputStream(s.getOutputStream());

			tech.seedhk.bean.ByteBuffer buffer=new tech.seedhk.bean.ByteBuffer();
			buffer.write(os, "client");
			log.info(is.available());
			byte[] data = tech.seedhk.bean.ByteBuffer.read(is);
			String ret=new String(data,"utf-8");
			log.info(ret);
			is.close();
			os.close();
			s.close();
			if(ret.contains("getip")){
				String ip=ret.substring(ret.indexOf(":")+1);
				log.info("已获取到IP地址，地址是: "+ip);
				is.close();
				os.close();
				s.close();
				log.info(s.isClosed());
				initClient(ip,port);
				isGet=true;
			}	
		}
	}

	private void initClient(String ip,int port) throws Exception {
		
		//获得一个socket通道
		SocketChannel sc=SocketChannel.open();
		
		sc.configureBlocking(false);
		
		this.selector=Selector.open();
		
		//执行完这步后，服务器端的select连接阻塞状态会解除,如果通道的阻塞模式为false，此方法会立即返回
		boolean isDone=sc.connect(new InetSocketAddress(ip,port));
		log.info(isDone);
		
		sc.register(selector, SelectionKey.OP_CONNECT);
		
		listen();
	}
	
	public void listen() throws Exception{
		
		while(true){
			selector.select();
			Iterator iterator=this.selector.selectedKeys().iterator();
			while(iterator.hasNext()){
				SelectionKey key=(SelectionKey) iterator.next();
				
				iterator.remove();
				
				if(key.isConnectable()){
					SocketChannel channel=(SocketChannel) key.channel();
					
					//如果正在连接，则完成该连接
					if(channel.isConnectionPending())
						channel.finishConnect();
					
					channel.configureBlocking(false);
					
					BodyBuffer body=new BodyBuffer();
					body.setCmd("text");
					body.setContent("向服务端发送了一条消息");
					body.setReady(true);
					
					DataBuffer db=new DataBuffer();
					db.setBody(body);
					db.sendHead(channel);
					db.sendBody(channel);
					
					//channel.write(ByteBuffer.wrap("向服务端发送了一条消息".getBytes()));
					//channel.write(writeData("text","向服务端发送了一条消息"));
					
					channel.register(this.selector, SelectionKey.OP_READ);
				}else if(key.isReadable()){
					read(key);
				}
			}
			
		}
		
	}


	private void read(SelectionKey key) throws Exception {
		SocketChannel sChannel=(SocketChannel) key.channel();
		
		DataBuffer data_buf=new DataBuffer();
		long body_size=data_buf.getHead(sChannel);
		BodyBuffer body_buf=data_buf.getBody((int)body_size, sChannel);
		BodyProcess.processClient(body_buf, sChannel);
		
		/*ByteBuffer[] buffers=new ByteBuffer[]{ByteBuffer.allocate(26),ByteBuffer.allocate(1024*1024*20)};
		long ret=-2;
		while(ret!=-1 && ret!=0){
			ret=sChannel.read(buffers);
			log.info(ret);
		}
		getData(buffers);
		
		Scanner scan=new Scanner(System.in);
		
		log.info("请输入文件类型，text、rpc、getFile");
		String head=scan.nextLine().trim();
		log.info("请输入路径：");
		String body=scan.nextLine().trim();
		ByteBuffer[] buffer=writeData(head, body);
		sChannel.write(buffer);*/
		//scan.close();
		
		
	}
	
	private ByteBuffer[] writeData(String h,String b){
		
		//byte[] by=new byte[10];
		//byte[] data=h.trim().getBytes();
		//for(int i=0;i<by.length;i++){
		//	if(i<data.length)
		//			by[i]=data[i];
		//	else
		//		by[i]='\0';
		//}
		ByteBuffer head = prepareHead(h);
		ByteBuffer body =null;
		if("rpc".equals(h)){
			ProxyObject po=ProxyObject.newInstance();
			po.getMethod("tech.seedhk.utils.FileUtils", "showDire", new Object[]{b.trim()}, String.class);
			body = ByteBuffer.wrap(ProxyObject.bean2byte(po));
		}else if("getFile".equals(h)){
			body = ByteBuffer.wrap(b.trim().getBytes());
		}else{
			body = ByteBuffer.wrap(b.trim().getBytes());	
		}
		
		//head.position(0);
		//body.position(0);
		
		return new ByteBuffer[]{head,body};
	}
	
	private void getData(ByteBuffer[] buffers) throws Exception{
		
		ByteBuffer h=buffers[0];
		h=getHead(h);
		ByteBuffer b=buffers[1];
		int bodyPosition=b.position();
		
		String type=new String(h.array()).trim();
		log.info("收到的数据类型为: "+type);
		
		if(type.equals("text") || type.equals("rpc")){
			String msg=new String(b.array()).trim();
			log.info("数据内容："+msg);
		}
		
		if(type.contains("file")){
			String suffix=type.substring(type.indexOf(":")+1);
			byte[] data=b.array();
			String basicPath=System.getProperty("user.dir");
			File file=new File(basicPath+File.separator+"file"+suffix);
			FileOutputStream fos=new FileOutputStream(file);
			fos.write(data,0,bodyPosition);
			fos.flush();
			fos.close();
			log.info("文件输出完毕");
		}
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

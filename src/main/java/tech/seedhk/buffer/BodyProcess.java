package tech.seedhk.buffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import org.apache.log4j.Logger;

import tech.seedhk.bean.ProxyObject;
import tech.seedhk.utils.Log;

public class BodyProcess {
	
	private static Logger log=Log.getInstance(BodyProcess.class);
	
	
	public static void  processClient(final BodyBuffer body,SocketChannel channel) throws IOException{
		
		if(body==null){
			System.out.println("body is null");
			return;
		}
		
		String cmd=body.getCmd();
		Object content=body.getContent();
		ByteBuffer attach=null;
		String attachName=null;
		if(body.isHasAttach()){
			attach=body.getAttach();
			attachName=body.getAttachName();
		}
		//log.info("content 的文件类型是："+content.getClass().getName());
		
		log.info("收到的命令为："+cmd);
		
		if(cmd.equals("text") || cmd.equals("rpc"))
			System.out.println("数据内容："+(String)content);
		
		if(cmd.contains("file") && attach!=null && attachName !=null){
			String basicPath=System.getProperty("user.dir");
			File file=new File(basicPath+File.separator+attachName);
			FileOutputStream fos=new FileOutputStream(file);
			fos.write(attach.array(),0,attach.limit());
			fos.flush();
			fos.close();
			System.out.println("文件输出完毕");
		}
		
		Scanner scan=new Scanner(System.in);
		
		System.out.println("请输入文件类型，text、rpc、getFile");
		String head_str=scan.nextLine().trim();
		System.out.println("请输入路径：");
		String body_str=scan.nextLine().trim();
		BodyBuffer body_buff=writeData(head_str,body_str);
		
		DataBuffer data=new DataBuffer();
		data.setBody(body_buff);
		data.sendHead(channel);
		data.sendBody(channel);
		
		log.info("processClient方法结束");
	}
	
	public static void processServer(final BodyBuffer body,SocketChannel channel) throws Exception{
		
		if(body==null){
			System.out.println("body is null");
			return;
		}
		
		String cmd=body.getCmd();
		Object content=body.getContent();
		ByteBuffer attach=null;
		String attachName=null;
		if(body.isHasAttach()){
			attach=body.getAttach();
			attachName=body.getAttachName();
		}
		//log.info("content 的文件类型是："+content.getClass());
		
		log.info("收到的命令为："+cmd);
		
		BodyBuffer ret_buf=new BodyBuffer();
		
		if("rpc".equals(cmd)){
			ret_buf.setCmd("rpc");
			if(body.isHasAttach())
				ret_buf.setContent(new String(rpc(attach).array()));
			else
				log.error("attach为空");
		}
		if("getFile".equals(cmd)){
			
			ret_buf.setCmd("file");
			String filePath=(String) content;
			
			ret_buf.setAttach(getFile(filePath));
			ret_buf.setAttachName(filePath.substring(filePath.lastIndexOf("/")+1));
			ret_buf.setHasAttach(true);
		}
		if("text".equals(cmd)){
			log.info("收到数据:" + (String) content);
			ret_buf.setCmd("text");
			ret_buf.setContent("收到数据:" + (String) content);
		}
		ret_buf.setReady(true);
		
		DataBuffer data=new DataBuffer();
		data.setBody(ret_buf);
		data.sendHead(channel);
		data.sendBody(channel);
		
		log.info("processServer方法结束");
		
	}
	
	
	private static ByteBuffer rpc(ByteBuffer buffer) {
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
	
	private static ByteBuffer getFile(String filePath) throws Exception{
		
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
	
	
	private static BodyBuffer writeData(String h,String b){
		
		log.info("本次欲发送的命令为： "+h+"，本次欲发送的内容为： "+b);

		BodyBuffer body_buff=new BodyBuffer();
		body_buff.setCmd(h);
		
		if("rpc".equals(h)){
			ProxyObject po=ProxyObject.newInstance();
			po.getMethod("tech.seedhk.utils.FileUtils", "showDire", new Object[]{b.trim()}, String.class);
			body_buff.setAttach(ProxyObject.bean2byte(po));
			body_buff.setAttachName("showDire");
			body_buff.setHasAttach(true);
		}else if("getFile".equals(h)){
			body_buff.setContent(b);
		}else{
			body_buff.setContent(b);
		}
		
		return body_buff;
	}

}
